package util;

/**
 * 纠正管理员密码为 admin123
 * 将数据库中 admin 用户的密码从当前的加密值
 * 更正为 admin123 的加密值
 */
public class FixAdminPassword {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("    纠正管理员密码为 admin123");
        System.out.println("========================================\n");
        
        // 步骤1：加密 admin123
        System.out.println("【步骤1】加密 admin123...");
        String plainPassword = "admin123";
        String encryptedPassword = DESUtil.encrypt(plainPassword);
        
        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            System.err.println("✗ 密码加密失败！");
            System.exit(1);
        }
        
        System.out.println("  ✓ 密码加密成功！");
        System.out.println("    明文: " + plainPassword);
        System.out.println("    密文: " + encryptedPassword);
        System.out.println();
        
        // 步骤2：验证加密正确性
        System.out.println("【步骤2】验证加密正确性...");
        String decryptedPassword = DESUtil.decrypt(encryptedPassword);
        
        if (decryptedPassword == null || !decryptedPassword.equals(plainPassword)) {
            System.err.println("✗ 加密验证失败！解密后的密码不匹配！");
            System.exit(1);
        }
        
        System.out.println("  ✓ 加密验证成功！");
        System.out.println("    解密结果: " + decryptedPassword);
        System.out.println("    与原始密码匹配: 是\n");
        
        // 步骤3：更新数据库
        System.out.println("【步骤3】更新数据库...");
        
        java.sql.Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            
            // 检查当前密码
            String checkSql = "SELECT username, password FROM user WHERE id = 1 AND username = 'admin'";
            java.sql.PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            java.sql.ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                String currentPassword = rs.getString("password");
                System.out.println("  当前管理员密码: " + currentPassword);
            }
            rs.close();
            checkStmt.close();
            
            // 更新密码
            String updateSql = "UPDATE user SET password = ? WHERE id = 1 AND username = 'admin'";
            java.sql.PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, encryptedPassword);
            
            int rowsAffected = updateStmt.executeUpdate();
            updateStmt.close();
            
            if (rowsAffected > 0) {
                System.out.println("  ✓ 数据库更新成功！");
                System.out.println("    影响行数: " + rowsAffected + "\n");
            } else {
                System.err.println("  ✗ 数据库更新失败！没有行被更新\n");
                System.exit(1);
            }
            
            // 步骤4：验证更新结果
            System.out.println("【步骤4】验证更新结果...");
            String verifySql = "SELECT username, password FROM user WHERE id = 1 AND username = 'admin'";
            java.sql.PreparedStatement verifyStmt = conn.prepareStatement(verifySql);
            java.sql.ResultSet verifyRs = verifyStmt.executeQuery();
            
            if (verifyRs.next()) {
                String storedPassword = verifyRs.getString("password");
                System.out.println("  验证查询结果：");
                System.out.println("    用户名: " + verifyRs.getString("username"));
                System.out.println("    存储的密码: " + storedPassword);
                
                if (storedPassword != null && storedPassword.equals(encryptedPassword)) {
                    System.out.println("  ✓ 验证成功！密码已正确更新为加密形式\n");
                } else {
                    System.err.println("  ✗ 验证失败！密码不匹配\n");
                }
            }
            
            verifyRs.close();
            verifyStmt.close();
            
        } catch (Exception e) {
            System.err.println("✗ 数据库操作失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        // 最终总结
        System.out.println("========================================");
        System.out.println("    修复完成总结");
        System.out.println("========================================");
        System.out.println("用户: admin (ID: 1)");
        System.out.println("原密码: admin (或其他)");
        System.out.println("新密码: admin123");
        System.out.println("加密后: " + encryptedPassword);
        System.out.println();
        System.out.println("✅ 修复成功！管理员密码已更新为 admin123");
        System.out.println();
        System.out.println("重要提示：");
        System.out.println("  1. 请使用以下账号登录：");
        System.out.println("     用户名: admin");
        System.out.println("     密码: admin123");
        System.out.println("  2. 登录成功后建议立即修改为更复杂的密码");
        System.out.println("========================================");
    }
}