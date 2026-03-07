package util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数据库连接测试工具类
 * 用于测试数据库连接是否正常
 */
public class DBConnectionTest {

    public static void main(String[] args) {
        System.out.println("开始测试数据库连接...");
        System.out.println("======================================");

        Connection conn = null;
        try {
            // 测试连接
            conn = DBUtil.getConnection();
            if (conn != null) {
                System.out.println("✓ 数据库连接成功！");

                // 获取数据库元信息
                DatabaseMetaData metaData = conn.getMetaData();
                System.out.println("数据库产品名称: " + metaData.getDatabaseProductName());
                System.out.println("数据库产品版本: " + metaData.getDatabaseProductVersion());
                System.out.println("驱动程序名称: " + metaData.getDriverName());
                System.out.println("驱动程序版本: " + metaData.getDriverVersion());
                System.out.println("数据库URL: " + metaData.getURL());
                System.out.println("用户名: " + metaData.getUserName());

                // 检查关键表是否存在
                System.out.println("\n检查数据库表...");
                String[] tables = {"user", "member_profile", "news", "activity", "award", "project",
                        "recruit_application", "operation_log", "file_storage"};
                for (String tableName : tables) {
                    if (tableExists(conn, tableName)) {
                        System.out.println("✓ 表 '" + tableName + "' 存在");
                    } else {
                        System.out.println("✗ 表 '" + tableName + "' 不存在");
                    }
                }

                System.out.println("\n======================================");
                System.out.println("数据库连接测试完成！");
            }
        } catch (SQLException e) {
            System.err.println("✗ 数据库连接失败！");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("错误代码: " + e.getErrorCode());
            System.err.println("SQL状态: " + e.getSQLState());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ 发生未知错误！");
            e.printStackTrace();
        } finally {
            DBUtil.closeConnection(conn);
        }
    }

    /**
     * 检查表是否存在
     */
    private static boolean tableExists(Connection conn, String tableName) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
            boolean exists = rs.next();
            rs.close();
            return exists;
        } catch (SQLException e) {
            return false;
        }
    }
}

