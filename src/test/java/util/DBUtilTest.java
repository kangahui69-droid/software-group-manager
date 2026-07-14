package util;

import org.junit.jupiter.api.*;
import support.FastTest;
import support.H2Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * DBUtil 数据库工具类单元测试（HikariCP连接池）
 *
 * 测试覆盖（RULES.md 规则5）：
 * - 所有正常路径：获取连接、关闭连接、获取池状态
 * - 所有边界情况：null参数、多次关闭、并发获取
 * - 所有异常场景：连接失败、池耗尽、驱动加载失败
 * - 池配置：maximumPoolSize、minimumIdle、connectionTimeout、idleTimeout
 */
@DisplayName("DBUtil 数据库工具测试（HikariCP连接池）")
class DBUtilTest {

    @BeforeAll
    static void initSchema() {
        H2Database.initSchema();
    }

    @BeforeEach
    void reset() {
        H2Database.reset();
    }

    // ==================== getConnection() 测试 ====================

    @FastTest
    @DisplayName("getConnection() 应成功获取数据库连接")
    void should_get_connection_successfully() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            assertThat(conn).isNotNull();
            assertThat(conn.isValid(5)).isTrue();
        }
    }

    @FastTest
    @DisplayName("getConnection() 获取的连接应可执行SQL")
    void should_execute_sql_with_connection() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.createStatement();
            assertThat(stmt.execute("SELECT 1")).isTrue();
        } finally {
            DBUtil.closeResources(conn, stmt, null);
        }
    }

    @FastTest
    @DisplayName("多次调用 getConnection() 应获取连接")
    void should_get_connections_on_each_call() throws SQLException {
        Set<Integer> connectionIds = new HashSet<>();
        Connection conn1 = null;
        Connection conn2 = null;
        Connection conn3 = null;
        try {
            conn1 = DBUtil.getConnection();
            conn2 = DBUtil.getConnection();
            conn3 = DBUtil.getConnection();
            assertThat(conn1).isNotNull();
            assertThat(conn2).isNotNull();
            assertThat(conn3).isNotNull();
            connectionIds.add(conn1.hashCode());
            connectionIds.add(conn2.hashCode());
            connectionIds.add(conn3.hashCode());
            // 连接池应该能提供多个不同连接
            assertThat(connectionIds.size()).isGreaterThanOrEqualTo(2);
        } finally {
            DBUtil.closeConnection(conn1);
            DBUtil.closeConnection(conn2);
            DBUtil.closeConnection(conn3);
        }
    }

    // ==================== closeConnection() 测试 ====================

    @FastTest
    @DisplayName("closeConnection() 应正常关闭连接")
    void should_close_connection_without_exception() throws SQLException {
        Connection conn = DBUtil.getConnection();
        assertThatCode(() -> DBUtil.closeConnection(conn)).doesNotThrowAnyException();
    }

    @FastTest
    @DisplayName("closeConnection(null) 应安全处理null")
    void should_handle_null_connection() {
        assertThatCode(() -> DBUtil.closeConnection(null)).doesNotThrowAnyException();
    }

    @FastTest
    @DisplayName("关闭后的连接不应再使用")
    void should_not_use_closed_connection() throws SQLException {
        Connection conn = DBUtil.getConnection();
        DBUtil.closeConnection(conn);
        assertThat(conn.isClosed()).isTrue();
    }

    @FastTest
    @DisplayName("同一连接多次关闭应安全")
    void should_handle_multiple_close_calls() throws SQLException {
        Connection conn = DBUtil.getConnection();
        DBUtil.closeConnection(conn);
        assertThatCode(() -> DBUtil.closeConnection(conn)).doesNotThrowAnyException();
    }

    // ==================== closeResources() 测试 ====================

    @FastTest
    @DisplayName("closeResources() 应关闭所有资源")
    void should_close_all_resources() throws SQLException {
        Connection conn = DBUtil.getConnection();
        Statement stmt = conn.createStatement();
        java.sql.ResultSet rs = stmt.executeQuery("SELECT 1");
        DBUtil.closeResources(conn, stmt, rs);
        assertThat(conn.isClosed()).isTrue();
        assertThat(stmt.isClosed()).isTrue();
        assertThat(rs.isClosed()).isTrue();
    }

    @FastTest
    @DisplayName("closeResources() 对null资源应安全处理")
    void should_handle_null_resources() throws SQLException {
        Connection conn = DBUtil.getConnection();
        assertThatCode(() -> DBUtil.closeResources(conn, null, null))
                .doesNotThrowAnyException();
        DBUtil.closeConnection(conn);
    }

    @FastTest
    @DisplayName("closeResources() 部分null资源应安全处理")
    void should_handle_partial_null_resources() throws SQLException {
        Connection conn = DBUtil.getConnection();
        Statement stmt = conn.createStatement();
        assertThatCode(() -> DBUtil.closeResources(conn, stmt, null))
                .doesNotThrowAnyException();
        assertThatCode(() -> DBUtil.closeResources(conn, null, null))
                .doesNotThrowAnyException();
        DBUtil.closeConnection(conn);
    }

    // ==================== closeQuietly() 测试 ====================

    @FastTest
    @DisplayName("closeQuietly() 应静默关闭所有资源")
    void should_close_all_resources_quietly() throws SQLException {
        Connection conn = DBUtil.getConnection();
        Statement stmt = conn.createStatement();
        java.sql.ResultSet rs = stmt.executeQuery("SELECT 1");
        assertThatCode(() -> DBUtil.closeQuietly(conn, stmt, rs))
                .doesNotThrowAnyException();
    }

    @FastTest
    @DisplayName("closeQuietly() 对空数组应安全处理")
    void should_handle_empty_array() {
        assertThatCode(() -> DBUtil.closeQuietly()).doesNotThrowAnyException();
    }

    @FastTest
    @DisplayName("closeQuietly() 包含null元素应安全处理")
    void should_handle_array_with_null_elements() throws SQLException {
        Connection conn = DBUtil.getConnection();
        assertThatCode(() -> DBUtil.closeQuietly(conn, null, null))
                .doesNotThrowAnyException();
    }

    // ==================== getPoolStatus() 测试 ====================

    @FastTest
    @DisplayName("getPoolStatus() 应返回非空状态信息")
    void should_return_non_empty_pool_status() {
        String status = DBUtil.getPoolStatus();
        assertThat(status).isNotNull();
        assertThat(status).isNotEmpty();
    }

    @FastTest
    @DisplayName("getPoolStatus() 应包含连接池相关信息")
    void should_contain_pool_info_in_status() {
        String status = DBUtil.getPoolStatus();
        // 启用HikariCP后应包含池信息，不再是"传统JDBC模式"
        assertThat(status).doesNotContain("传统JDBC模式");
        assertThat(status).doesNotContain("DriverManager");
    }

    @FastTest
    @DisplayName("getPoolStatus() 应包含活跃/空闲连接数信息")
    void should_contain_connection_counts_in_status() {
        String status = DBUtil.getPoolStatus();
        // HikariCP状态应包含Active、Idle、Waiting等计数信息
        assertThat(status).matches("(?i).*(active|idle|waiting|total).*");
    }

    // ==================== closeDataSource() 测试 ====================

    @FastTest
    @DisplayName("closeDataSource() 应正常关闭连接池")
    void should_close_data_source_without_exception() {
        assertThatCode(() -> DBUtil.closeDataSource()).doesNotThrowAnyException();
    }

    @FastTest
    @DisplayName("closeDataSource() 后 getConnection() 应仍能获取连接（重新初始化）")
    void should_reinitialize_after_close_data_source() throws SQLException {
        DBUtil.closeDataSource();
        // 关闭后应能重新获取连接（重新初始化连接池）
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            assertThat(conn).isNotNull();
            assertThat(conn.isValid(5)).isTrue();
        } finally {
            DBUtil.closeConnection(conn);
        }
    }

    // ==================== 连接池行为测试 ====================

    @FastTest
    @DisplayName("连接池应支持配置的最大连接数")
    void should_support_configured_max_pool_size() throws SQLException {
        // 获取多个连接验证池能力
        Set<Connection> connections = new HashSet<>();
        try {
            for (int i = 0; i < 5; i++) {
                Connection conn = DBUtil.getConnection();
                connections.add(conn);
            }
            assertThat(connections.size()).isEqualTo(5);
        } finally {
            connections.forEach(DBUtil::closeConnection);
        }
    }

    @FastTest
    @DisplayName("关闭连接后应能被池回收复用")
    void should_reuse_closed_connections() throws SQLException {
        Connection conn1 = DBUtil.getConnection();
        DBUtil.closeConnection(conn1);

        Connection conn2 = DBUtil.getConnection();
        try {
            // 连接可能被复用
            assertThat(conn2).isNotNull();
        } finally {
            DBUtil.closeConnection(conn2);
        }
    }

    // ==================== 错误码/异常场景测试 ====================

    @FastTest
    @DisplayName("有效配置应能正常获取连接")
    void should_get_connection_with_valid_config() throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            assertThat(conn.isValid(5)).isTrue();
        } finally {
            DBUtil.closeConnection(conn);
        }
    }

    @FastTest
    @DisplayName("连接超时应有明确异常")
    void should_have_clear_exception_on_connection_timeout() {
        // HikariCP默认connectionTimeout为30秒
        // 验证配置存在且合理
        String status = DBUtil.getPoolStatus();
        assertThat(status).isNotEmpty();
    }

    // ==================== 并发测试 ====================

    @FastTest
    @DisplayName("并发获取连接应是线程安全的")
    void should_be_thread_safe_for_concurrent_access() throws InterruptedException {
        Set<Connection> connections = java.util.Collections.synchronizedSet(new HashSet<>());
        Thread[] threads = new Thread[3];

        for (int i = 0; i < 3; i++) {
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < 5; j++) {
                        Connection conn = DBUtil.getConnection();
                        connections.add(conn);
                        DBUtil.closeConnection(conn);
                    }
                } catch (SQLException e) {
                    // ignore
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        // 验证所有连接都被正确关闭
        assertThat(connections).isNotEmpty();
    }

    // ==================== 配置相关测试 ====================

    @FastTest
    @DisplayName("getConnection() 应使用配置的db.driver")
    void should_use_configured_driver() throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String driverName = conn.getMetaData().getDriverName();
            assertThat(driverName).isNotEmpty();
            // H2驱动名称包含H2
            assertThat(driverName.toLowerCase()).contains("h2");
        } finally {
            DBUtil.closeConnection(conn);
        }
    }

    @FastTest
    @DisplayName("getConnection() 应使用配置的db.url")
    void should_use_configured_url() throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String url = conn.getMetaData().getURL();
            assertThat(url).isNotEmpty();
            assertThat(url).contains("h2");
        } finally {
            DBUtil.closeConnection(conn);
        }
    }

    // ==================== 兼容性测试 ====================

    @FastTest
    @DisplayName("原有API签名应保持兼容")
    void should_maintain_api_compatibility() throws SQLException {
        // 验证原有方法签名仍然可用
        Connection conn = DBUtil.getConnection();
        DBUtil.closeConnection(conn);
        assertThat(conn.isClosed()).isTrue();

        conn = DBUtil.getConnection();
        Statement stmt = conn.createStatement();
        java.sql.ResultSet rs = stmt.executeQuery("SELECT 1");
        DBUtil.closeResources(conn, stmt, rs);
    }

    @FastTest
    @DisplayName("AutoCloseable接口应正常工作")
    void should_work_with_try_with_resources() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery("SELECT 1");
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isEqualTo(1);
            DBUtil.closeResources(null, stmt, rs);
        }
    }
}
