package util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import config.Config;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * JDBC数据库工具类 - HikariCP连接池模式
 */
public class DBUtil {

    private static volatile HikariDataSource dataSource;

    private static final int DEFAULT_MAX_POOL_SIZE = 20;
    private static final int DEFAULT_MIN_IDLE = 5;
    private static final long DEFAULT_CONNECTION_TIMEOUT = 30000;
    private static final long DEFAULT_IDLE_TIMEOUT = 600000;

    private static void ensureDataSourceInitialized() {
        if (dataSource == null) {
            synchronized (DBUtil.class) {
                if (dataSource == null) {
                    initDataSource();
                }
            }
        }
    }

    private static int parseIntProperty(String key, int defaultValue) {
        String value = Config.getProperty(key);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("[DBUtil] 无效的整型配置: " + key + "=" + value);
            }
        }
        return defaultValue;
    }

    private static void initDataSource() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(Config.getProperty("db.url",
            "jdbc:mysql://localhost:3306/software_group?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"));
        config.setUsername(Config.getProperty("db.username", "root"));
        config.setPassword(Config.getProperty("db.password", ""));
        config.setDriverClassName(Config.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));

        config.setMaximumPoolSize(parseIntProperty("hikaricp.maximumPoolSize", DEFAULT_MAX_POOL_SIZE));
        config.setMinimumIdle(parseIntProperty("hikaricp.minimumIdle", DEFAULT_MIN_IDLE));
        config.setConnectionTimeout(parseIntProperty("hikaricp.connectionTimeout", (int) DEFAULT_CONNECTION_TIMEOUT));
        config.setIdleTimeout(parseIntProperty("hikaricp.idleTimeout", (int) DEFAULT_IDLE_TIMEOUT));

        config.setPoolName("SoftwareGroup-HikariPool");
        config.setAutoCommit(true);
        config.setConnectionTestQuery("SELECT 1");

        dataSource = new HikariDataSource(config);
        System.out.println("[DBUtil] HikariCP连接池初始化完成: " +
            "maxPoolSize=" + dataSource.getMaximumPoolSize() + ", minIdle=" + dataSource.getMinimumIdle());
    }

    public static Connection getConnection() throws SQLException {
        ensureDataSourceInitialized();
        return dataSource.getConnection();
    }

    public static void closeConnection(Connection conn) {
        if (conn == null) return;
        try {
            conn.close();
        } catch (SQLException e) {
            System.err.println("[DBUtil] 关闭数据库连接时出错: " + e.getMessage());
        }
    }

    public static void closeResources(Connection conn, java.sql.Statement stmt, java.sql.ResultSet rs) {
        closeQuietly(rs);
        closeQuietly(stmt);
        closeConnection(conn);
    }

    public static void closeQuietly(AutoCloseable... closeables) {
        for (AutoCloseable closeable : closeables) {
            if (closeable == null) continue;
            try {
                closeable.close();
            } catch (Exception e) {
                System.err.println("[DBUtil] 关闭资源时出错: " + e.getMessage());
            }
        }
    }

    public static String getPoolStatus() {
        ensureDataSourceInitialized();
        HikariPoolMXBean pool = dataSource.getHikariPoolMXBean();
        if (pool == null) {
            return "连接池未初始化";
        }

        return String.format(
            "HikariCP - Active: %d, Idle: %d, Total: %d, Waiting: %d, Max: %d",
            pool.getActiveConnections(),
            pool.getIdleConnections(),
            pool.getTotalConnections(),
            pool.getThreadsAwaitingConnection(),
            dataSource.getMaximumPoolSize()
        );
    }

    public static void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
            System.out.println("[DBUtil] HikariCP连接池已关闭");
        }
    }
}
