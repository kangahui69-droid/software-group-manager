package util;

import config.Config;
import dao.UserDAO;
import model.User;

/**
 * 最终密码修复工具
 * 直接在应用层修复密码，确保一致性
 */
public class FinalPasswordFix {
    
    public static void main(String[] args) {
        System.out.println("=== 最终密码修复工具 ===\n");
        
        // 1. 检查当前配置
        System.out.println("1. 当前配置信息:");
        System.out.println("   DES密钥: " + Config.getDesKey());
        System.out.println("   密钥长度: " + Config.getDesKey().length() + " 字节");
        
        // 2. 测试加密一致性
        System.out.println("\n2. 加密测试:");
        String[] testPasswords = {"admin123", "member123", "123456"};
        for (String pwd : testPasswords) {
            String encrypted = DESUtil.encrypt(pwd);
            String decrypted = DESUtil.decrypt(encrypted);
            System.out.println("   " + pwd + " -> " + encrypted + " -> " + decrypted);
        }
        
        // 3. 直接修复数据库密码
        System.out.println("\n3. 开始修复数据库密码...");
        fixPassword("admin", "admin123");
        fixPassword("member1", "member123");
        
        // 4. 验证修复结果
        System.out.println("\n4. 验证修复结果:");
        verifyLogin("admin", "admin123");
        verifyLogin("member1", "member123");
        
        System.out.println("\n=== 修复完成 ===");
    }
    
    private static void fixPassword(String username, String newPassword) {
        try {
            String encryptedPassword = DESUtil.encrypt(newPassword);
            
            java.sql.Connection conn = util.DBUtil.getConnection();
            String sql = "UPDATE user SET password = ?, status = 1, updated_at = NOW() WHERE username = ?";
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, encryptedPassword);
            pstmt.setString(2, username);
            
            int rows = pstmt.executeUpdate();
            System.out.println("   " + username + ": " + (rows > 0 ? "修复成功" : "未找到用户"));
            
            pstmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("   " + username + ": 修复失败 - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void verifyLogin(String username, String password) {
        try {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.findByUsernameAndPassword(username, password);
            if (user != null) {
                System.out.println("   ✓ " + username + ": 登录验证成功");
            } else {
                System.out.println("   ✗ " + username + ": 登录验证失败");
            }
        } catch (Exception e) {
            System.out.println("   ✗ " + username + ": 验证出错 - " + e.getMessage());
        }
    }
}
