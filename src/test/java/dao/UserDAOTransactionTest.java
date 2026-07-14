package dao;

import model.User;
import org.junit.jupiter.api.*;
import support.FastTest;
import support.H2Database;
import util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;

/**
 * 3.8 DAO层事务重载规范测试 - UserDAO
 *
 * 验证UserDAO写操作（insert/update/updateStatus/delete）的事务重载模式：
 * - 有参版本 (Connection conn) 接收外部连接，不自己关闭连接
 * - 无参版本内部获取连接后调有参版本，保持原有行为不变
 * - SQLException在有参版本中包装为RuntimeException
 */
@DisplayName("UserDAO 事务重载测试")
class UserDAOTransactionTest {

    private UserDAO userDAO = new UserDAO();

    @BeforeAll
    static void initSchema() {
        H2Database.initSchema();
    }

    @BeforeEach
    void reset() {
        H2Database.reset();
    }

    private User createTestUser() {
        User user = new User();
        user.setUsername("testuser_" + System.currentTimeMillis());
        user.setPassword("password123");
        user.setName("测试用户");
        user.setEmail("test_" + System.currentTimeMillis() + "@example.com");
        user.setPhone("13800138000");
        user.setRole("MEMBER");
        user.setStatus(1);
        return user;
    }

    // ==================== insert 事务重载 ====================

    @FastTest
    @DisplayName("insert(User, Connection) 有参版本应能插入用户并设置生成的主键ID")
    void should_insert_user_with_connection_and_set_generated_id() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            User user = createTestUser();

            boolean result = userDAO.insert(user, conn);

            assertThat(result).isTrue();
            assertThat(user.getId()).isGreaterThan(0);
            User found = userDAO.findById(user.getId());
            assertThat(found).isNotNull();
            assertThat(found.getUsername()).isEqualTo(user.getUsername());
        }
    }

    @FastTest
    @DisplayName("insert(User, Connection) 连接不关闭，应由调用方管理")
    void should_not_close_connection_in_insert_connection_version() throws SQLException {
        Connection conn = DBUtil.getConnection();
        User user = createTestUser();
        user.setUsername("noclose_user");

        userDAO.insert(user, conn);

        assertThat(conn.isClosed()).isFalse();
        conn.close();
    }

    @FastTest
    @DisplayName("insert(User, Connection) SQL异常应抛出RuntimeException")
    void should_throw_runtime_exception_when_insert_with_connection_fails() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            User user = createTestUser();
            user.setUsername(null); // 导致SQL错误

            assertThatThrownBy(() -> userDAO.insert(user, conn))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("插入用户");
        }
    }

    @FastTest
    @DisplayName("insert(User) 无参版本应能正常插入用户")
    void should_insert_user_without_connection() {
        User user = createTestUser();
        user.setUsername("noconn_user");

        boolean result = userDAO.insert(user);

        assertThat(result).isTrue();
        assertThat(user.getId()).isGreaterThan(0);
    }

    // ==================== update 事务重载 ====================

    @FastTest
    @DisplayName("update(User, Connection) 有参版本应能更新用户信息")
    void should_update_user_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            User user = createTestUser();
            userDAO.insert(user);

            user.setName("更新后的名称");
            user.setEmail("updated@example.com");

            boolean result = userDAO.update(user, conn);

            assertThat(result).isTrue();
            User updated = userDAO.findById(user.getId());
            assertThat(updated.getName()).isEqualTo("更新后的名称");
        }
    }

    @FastTest
    @DisplayName("update(User, Connection) SQL异常应抛出RuntimeException")
    void should_throw_runtime_exception_when_update_with_connection_fails() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            User user = createTestUser();
            user.setId(99999); // 不存在的用户

            assertThatThrownBy(() -> userDAO.update(user, conn))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("更新用户");
        }
    }

    @FastTest
    @DisplayName("update(User) 无参版本应能正常更新用户")
    void should_update_user_without_connection() {
        User user = createTestUser();
        userDAO.insert(user);

        user.setName("无连接更新");
        boolean result = userDAO.update(user);

        assertThat(result).isTrue();
        User updated = userDAO.findById(user.getId());
        assertThat(updated.getName()).isEqualTo("无连接更新");
    }

    // ==================== updateStatus 事务重载 ====================

    @FastTest
    @DisplayName("updateStatus(Integer, Integer, Connection) 有参版本应能更新用户状态")
    void should_update_status_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            User user = createTestUser();
            userDAO.insert(user);

            boolean result = userDAO.updateStatus(user.getId(), 0, conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("updateStatus(Integer, Integer) 无参版本应能正常更新状态")
    void should_update_status_without_connection() {
        User user = createTestUser();
        userDAO.insert(user);

        boolean result = userDAO.updateStatus(user.getId(), 0);

        assertThat(result).isTrue();
    }

    // ==================== delete 事务重载 ====================

    @FastTest
    @DisplayName("delete(Integer, Connection) 有参版本应能删除用户")
    void should_delete_user_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            User user = createTestUser();
            userDAO.insert(user);

            boolean result = userDAO.delete(user.getId(), conn);

            assertThat(result).isTrue();
            User found = userDAO.findById(user.getId());
            assertThat(found).isNull();
        }
    }

    @FastTest
    @DisplayName("delete(Integer, Connection) SQL异常应抛出RuntimeException")
    void should_throw_runtime_exception_when_delete_with_connection_fails() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            assertThatThrownBy(() -> userDAO.delete(99999, conn))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("删除用户");
        }
    }

    @FastTest
    @DisplayName("delete(Integer) 无参版本应能正常删除用户")
    void should_delete_user_without_connection() {
        User user = createTestUser();
        userDAO.insert(user);

        boolean result = userDAO.delete(user.getId());

        assertThat(result).isTrue();
    }

    // ==================== 事务边界测试 ====================

    @FastTest
    @DisplayName("有参版本传入null连接应抛出NullPointerException")
    void should_throw_null_pointer_when_connection_is_null() {
        User user = createTestUser();

        assertThatThrownBy(() -> userDAO.insert(user, null))
                .isInstanceOf(NullPointerException.class);
    }

    @FastTest
    @DisplayName("updateStatus with null connection should throw NullPointerException")
    void should_throw_null_pointer_when_update_status_with_null_connection() {
        assertThatThrownBy(() -> userDAO.updateStatus(1, 0, null))
                .isInstanceOf(NullPointerException.class);
    }

    @FastTest
    @DisplayName("update with null connection should throw NullPointerException")
    void should_throw_null_pointer_when_update_with_null_connection() {
        User user = createTestUser();
        user.setId(1);
        assertThatThrownBy(() -> userDAO.update(user, null))
                .isInstanceOf(NullPointerException.class);
    }

    @FastTest
    @DisplayName("delete with null connection should throw NullPointerException")
    void should_throw_null_pointer_when_delete_with_null_connection() {
        assertThatThrownBy(() -> userDAO.delete(1, null))
                .isInstanceOf(NullPointerException.class);
    }

    @FastTest
    @DisplayName("事务场景：连续多个写操作使用同一连接应成功提交")
    void should_support_transaction_with_multiple_user_operations() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                User user1 = createTestUser();
                user1.setUsername("trans_user1");
                user1.setEmail("trans1@example.com");
                userDAO.insert(user1, conn);

                User user2 = createTestUser();
                user2.setUsername("trans_user2");
                user2.setEmail("trans2@example.com");
                userDAO.insert(user2, conn);

                user1.setName("更新后用户1");
                userDAO.update(user1, conn);

                conn.commit();
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

            User found = userDAO.findByUsername("trans_user1");
            assertThat(found).isNotNull();
            assertThat(found.getName()).isEqualTo("更新后用户1");
        }
    }

    @FastTest
    @DisplayName("事务场景：中间操作失败应回滚之前的操作")
    void should_rollback_when_operation_fails_in_transaction() throws SQLException {
        User user = createTestUser();
        user.setUsername("rollback_test");
        user.setEmail("rollback@example.com");
        userDAO.insert(user);

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            try {
                user.setName("事务内更新");
                userDAO.update(user, conn);

                // 尝试更新不存在的记录，触发异常
                User fakeUser = createTestUser();
                fakeUser.setId(99999);
                fakeUser.setUsername(null); // 会失败
                userDAO.update(fakeUser, conn);

                conn.commit();
                fail("Expected RuntimeException was not thrown");
            } catch (RuntimeException e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }

            // 验证原始更新被回滚
            User found = userDAO.findById(user.getId());
            assertThat(found.getName()).isNotEqualTo("事务内更新");
        }
    }

    @FastTest
    @DisplayName("多DAO同一事务：User和MemberProfile使用同一连接应成功")
    void should_support_multi_dao_transaction_with_same_connection() throws SQLException {
        MemberProfileDAO memberProfileDAO = new MemberProfileDAO();
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                User user = createTestUser();
                user.setUsername("multidao_user");
                user.setEmail("multidao@example.com");
                userDAO.insert(user, conn);

                model.MemberProfile profile = new model.MemberProfile();
                profile.setUserId(user.getId());
                profile.setStudentId("2021" + user.getId());
                profile.setMajor("软件工程");
                profile.setGrade("2021");
                memberProfileDAO.insert(profile, conn);

                conn.commit();
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

            User foundUser = userDAO.findByUsername("multidao_user");
            assertThat(foundUser).isNotNull();
            model.MemberProfile foundProfile = memberProfileDAO.findByUserId(foundUser.getId());
            assertThat(foundProfile).isNotNull();
            assertThat(foundProfile.getMajor()).isEqualTo("软件工程");
        }
    }
}
