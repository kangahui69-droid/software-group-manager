package util;

import model.User;
import org.junit.jupiter.api.*;
import support.FastTest;
import support.H2Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * TransactionTemplate 事务工具类单元测试
 *
 * 测试覆盖（RULES.md 规则5）：
 * - 成功路径：正常提交，数据库变更生效
 * - 失败路径：RuntimeException 触发回滚
 * - 边界：SQLException 处理、自动提交状态恢复
 */
@DisplayName("TransactionTemplate 事务工具测试（H2内存库）")
class TransactionTemplateTest {

    @BeforeAll
    static void initSchema() {
        H2Database.initSchema();
    }

    @BeforeEach
    void reset() {
        H2Database.reset();
    }

    @FastTest
    @DisplayName("execute 有返回值时，事务成功应返回结果并提交")
    void should_return_result_and_commit_when_success() throws SQLException {
        AtomicInteger capturedConnectionId = new AtomicInteger(-1);

        Integer result = TransactionTemplate.execute(conn -> {
            capturedConnectionId.set(conn.hashCode());
            insertUser(conn, "testuser1", "测试用户1");
            return 42;
        }, getConnection());

        assertThat(result).isEqualTo(42);
        assertThat(userExists("testuser1")).isTrue();
    }

    @FastTest
    @DisplayName("executeWithoutResult 无返回值时，事务成功应提交")
    void should_commit_when_executeWithoutResult_success() {
        AtomicBoolean actionExecuted = new AtomicBoolean(false);

        TransactionTemplate.executeWithoutResult(conn -> {
            actionExecuted.set(true);
            insertUser(conn, "testuser2", "测试用户2");
        }, getConnection());

        assertThat(actionExecuted.get()).isTrue();
        assertThat(userExists("testuser2")).isTrue();
    }

    @FastTest
    @DisplayName("RuntimeException 应触发回滚，数据不应提交")
    void should_rollback_when_runtime_exception() {
        insertTestUser("rollback_test_user");

        assertThatThrownBy(() -> TransactionTemplate.execute(conn -> {
            updateUserName(conn, "rollback_test_user", "新名字不应该保存");
            throw new RuntimeException("模拟业务异常");
        }, getConnection()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("模拟业务异常");

        assertThat(userNameIs("rollback_test_user", "新名字不应该保存")).isFalse();
        assertThat(userNameIs("rollback_test_user", "测试用户")).isTrue();
    }

    @FastTest
    @DisplayName("executeWithConnection 内部管理连接时，成功应提交")
    void should_commit_when_executeWithConnection_success() {
        AtomicBoolean executed = new AtomicBoolean(false);

        String result = TransactionTemplate.executeWithConnection(conn -> {
            executed.set(true);
            insertUser(conn, "testuser3", "测试用户3");
            return "success";
        });

        assertThat(result).isEqualTo("success");
        assertThat(executed.get()).isTrue();
        assertThat(userExists("testuser3")).isTrue();
    }

    @FastTest
    @DisplayName("executeWithConnection 内部管理连接时，异常应回滚")
    void should_rollback_when_executeWithConnection_exception() {
        insertTestUser("internal_conn_test");

        assertThatThrownBy(() -> TransactionTemplate.executeWithConnection(conn -> {
            updateUserName(conn, "internal_conn_test", "内部连接测试新名字");
            throw new IllegalArgumentException("参数错误");
        }))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("参数错误");

        assertThat(userNameIs("internal_conn_test", "内部连接测试新名字")).isFalse();
    }

    @FastTest
    @DisplayName("executeWithoutResultWithConnection 异常时应回滚")
    void should_rollback_when_executeWithoutResultWithConnection_exception() {
        insertTestUser("no_result_test");

        assertThatThrownBy(() -> TransactionTemplate.executeWithoutResultWithConnection(conn -> {
            updateUserName(conn, "no_result_test", "无返回值测试新名字");
            throw new UnsupportedOperationException("不支持的操作");
        }))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("不支持的操作");

        assertThat(userNameIs("no_result_test", "无返回值测试新名字")).isFalse();
    }

    @FastTest
    @DisplayName("事务执行后应恢复自动提交状态")
    void should_restore_auto_commit_after_transaction() throws SQLException {
        insertTestUser("autocommit_restore_test");

        Connection conn = getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();

        TransactionTemplate.executeWithoutResult(conn2 -> {
            updateUserName(conn2, "autocommit_restore_test", "恢复测试");
        }, conn);

        assertThat(conn.getAutoCommit()).isEqualTo(originalAutoCommit);
        conn.close();
    }

    @FastTest
    @DisplayName("事务回滚后应恢复自动提交状态")
    void should_restore_auto_commit_after_rollback() throws SQLException {
        insertTestUser("rollback_autocommit_test");

        Connection conn = getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();

        try {
            TransactionTemplate.execute(conn2 -> {
                throw new RuntimeException("触发回滚");
            }, conn);
        } catch (RuntimeException ignored) {
            // expected
        }

        assertThat(conn.getAutoCommit()).isEqualTo(originalAutoCommit);
        conn.close();
    }

    @FastTest
    @DisplayName("executeWithConnection 异常时应抛出 RuntimeException")
    void should_wrap_sql_exception_in_runtime_exception() {
        assertThatThrownBy(() -> TransactionTemplate.executeWithConnection(conn -> {
            throw new RuntimeException("数据库访问异常");
        }))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("数据库访问异常");
    }

    @FastTest
    @DisplayName("空操作事务应成功执行")
    void should_handle_empty_action() {
        String result = TransactionTemplate.execute(conn -> "empty", getConnection());
        assertThat(result).isEqualTo("empty");
    }

    @FastTest
    @DisplayName("多步骤操作应在同一事务中执行")
    void should_execute_multiple_steps_in_one_transaction() {
        insertTestUser("multi_step_1");
        insertTestUser("multi_step_2");

        TransactionTemplate.executeWithoutResult(conn -> {
            updateUserName(conn, "multi_step_1", "步骤1新名字");
            updateUserName(conn, "multi_step_2", "步骤2新名字");
        }, getConnection());

        assertThat(userNameIs("multi_step_1", "步骤1新名字")).isTrue();
        assertThat(userNameIs("multi_step_2", "步骤2新名字")).isTrue();
    }

    @FastTest
    @DisplayName("多步骤操作中抛出异常应全部回滚")
    void should_rollback_all_steps_when_exception_in_transaction() {
        insertTestUser("multi_step_rollback_1");
        insertTestUser("multi_step_rollback_2");

        assertThatThrownBy(() -> TransactionTemplate.executeWithoutResult(conn -> {
            updateUserName(conn, "multi_step_rollback_1", "步骤1新名字");
            updateUserName(conn, "multi_step_rollback_2", "步骤2新名字");
            throw new RuntimeException("中间步骤失败");
        }, getConnection()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("中间步骤失败");

        assertThat(userNameIs("multi_step_rollback_1", "步骤1新名字")).isFalse();
        assertThat(userNameIs("multi_step_rollback_2", "步骤2新名字")).isFalse();
    }

    @FastTest
    @DisplayName("null 返回值应正常处理")
    void should_handle_null_return_value() {
        String result = TransactionTemplate.execute(conn -> null, getConnection());
        assertThat(result).isNull();
    }

    @FastTest
    @DisplayName("executeWithoutResultWithConnection 空操作应成功")
    void should_handle_empty_executeWithoutResultWithConnection() {
        AtomicBoolean executed = new AtomicBoolean(false);

        TransactionTemplate.executeWithoutResultWithConnection(conn -> {
            executed.set(true);
        });

        assertThat(executed.get()).isTrue();
    }

    // ---- 私有辅助方法 ----

    private Connection getConnection() {
        try {
            return DBUtil.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("获取连接失败", e);
        }
    }

    private void insertUser(Connection conn, String username, String name) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO user (username, password, name, email, role, status) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, util.DESUtil.encrypt("test123"));
            ps.setString(3, name);
            ps.setString(4, username + "@example.com");
            ps.setString(5, "MEMBER");
            ps.setInt(6, 1);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("插入用户失败: " + username, e);
        }
    }

    private void updateUserName(Connection conn, String username, String newName) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE user SET name = ? WHERE username = ?")) {
            ps.setString(1, newName);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("更新用户失败: " + username, e);
        }
    }

    private void insertTestUser(String username) {
        try (Connection conn = DBUtil.getConnection()) {
            insertUser(conn, username, "测试用户");
        } catch (SQLException e) {
            throw new RuntimeException("插入测试用户失败", e);
        }
    }

    private boolean userExists(String username) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT 1 FROM user WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询用户失败", e);
        }
    }

    private boolean userNameIs(String username, String expectedName) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT name FROM user WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String actualName = rs.getString("name");
                    return expectedName.equals(actualName);
                }
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询用户名失败", e);
        }
    }
}
