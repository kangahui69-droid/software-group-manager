package util;

import config.Config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC数据库工具类 - 传统JDBC连接方式（兼容模式）
 * 
 * 注意：如需使用HikariCP连接池，请先添加HikariCP依赖库到WEB-INF/lib
 * 当前版本使用传统JDBC连接以兼容现有环境
 */
public class DBUtil {

    // 数据库配置 - 从配置文件读取
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    // 静态初始化块 - 从配置文件加载数据库配置
    static {
        URL = Config.getProperty("db.url", 
            "jdbc:mysql://localhost:3306/software_group?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true");
        USERNAME = Config.getProperty("db.username", "root");
        PASSWORD = Config.getProperty("db.password", "");
        
        // 加载数据库驱动
        try {
            Class.forName(DRIVER);
            System.out.println("[DBUtil] 数据库驱动加载成功");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBUtil] 数据库驱动加载失败: " + e.getMessage());
            throw new RuntimeException("数据库驱动加载失败", e);
        }
    }

    /**
     * 获取数据库连接
     * @return 数据库连接
     * @throws SQLException 当获取连接失败时抛出
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            return conn;
        } catch (SQLException e) {
            System.err.println("[DBUtil] 获取数据库连接失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 关闭数据库连接
     * @param conn 需要关闭的数据库连接
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("[DBUtil] 关闭数据库连接时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 关闭数据库资源（兼容旧代码）
     * @param conn 数据库连接
     * @param stmt Statement对象
     * @param rs ResultSet对象
     */
    public static void closeResources(Connection conn, java.sql.Statement stmt, java.sql.ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("[DBUtil] 关闭ResultSet时出错: " + e.getMessage());
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("[DBUtil] 关闭Statement时出错: " + e.getMessage());
            }
        }
        closeConnection(conn);
    }

    /**
     * 静默关闭资源（新推荐方法）
     * @param closeables 可关闭的资源
     */
    public static void closeQuietly(AutoCloseable... closeables) {
        for (AutoCloseable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    System.err.println("[DBUtil] 关闭资源时出错: " + e.getMessage());
                }
            }
        }
    }

    // ============== HikariCP兼容方法（预留）==============

    /**
     * 获取连接池状态（当前版本返回固定信息）
     * 如需完整HikariCP支持，请添加HikariCP依赖库
     * @return 连接状态描述
     */
    public static String getPoolStatus() {
        return "JDBC传统连接模式 - 当前使用DriverManager获取连接";
    }

    /**
     * 关闭连接池（当前版本无实际操作）
     * 如需完整HikariCP支持，请添加HikariCP依赖库
     */
    public static void closeDataSource() {
        System.out.println("[DBUtil] 传统JDBC模式，无需关闭连接池");
    }
}
