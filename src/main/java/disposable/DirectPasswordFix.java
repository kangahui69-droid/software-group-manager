package util;

import config.Config;
import dao.UserDAO;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 直接密码修复工具
 * 使用当前应用相同的加密逻辑修复密码
 */
public class DirectPasswordFix {
    
    public static void main(String[] args) {
        System.out.println("============================================");
        System.out.println("        直接密码修复工具");
        System.out.println("============================================\n");
        
        try {
            // 1. 显示当前密钥
            String currentKey = Config.getDesKey();
            System.out.println("当前DES密钥: " + currentKey);
            System.out.println("密钥长度: " + currentKey.length() + "\n");
            
            // 2. 生成正确的加密密码
            String adminEncrypted = DESUtil.encrypt("admin123");
            String memberEncrypted = DESUtil.encrypt("member123");
            
            System.out.println("正确的加密结果:");
            System.out.println("  admin123 -> " + adminEncrypted);
            System.out.println("  member123 -> " + memberEncrypted + "\n");
            
            // 3. 直接更新数据库
            System.out.println("开始更新数据库...\n");
            
            Connection conn = DBUtil.getConnection();
            
            // 更新 admin
            String updateAdmin = "UPDATE user SET password = ?, status = 1 WHERE username = ?";
            PreparedStatement pstmt1 = conn.prepareStatement(updateAdmin);
            pstmt1.setString(1, adminEncrypted);
            pstmt1.setString(2, "admin");
            int rows1 = pstmt1.executeUpdate();
            System.out.println("admin 更新: " + (rows1 > 0 ? "成功" : "失败"));
            pstmt1.close();
            
            // 更新 member1
            String updateMember = "UPDATE user SET password = ?, status = 1 WHERE username = ?";
            PreparedStatement pstmt2 = conn.prepareStatement(updateMember);
            pstmt2.setString(1, memberEncrypted);
            pstmt2.setString(2, "member1");
            int rows2 = pstmt2.executeUpdate();
            System.out.println("member1 更新: " + (rows2 > 0 ? "成功" : "失败"));
            pstmt2.close();
            
            // 查询验证
            System.out.println("\n验证更新结果:");
            String query = "SELECT username, password FROM user WHERE username IN ('admin', 'member1')";
            PreparedStatement pstmt3 = conn.prepareStatement(query);
            ResultSet rs = pstmt3.executeQuery();
            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                String decrypted = DESUtil.decrypt(password);
                System.out.println("  " + username + ": " + password + " -> " + decrypted);
            }
            rs.close();
            pstmt3.close();
            
            conn.close();
            
            // 测试登录
            System.out.println("\n测试登录:");
            UserDAO userDAO = new UserDAO();
            
            User adminUser = userDAO.findByUsernameAndPassword("admin", "admin123");
            System.out.println("  admin/admin123: " + (adminUser != null ? "✓ 成功" : "✗ 失败"));
            
            User memberUser = userDAO.findByUsernameAndPassword("member1", "member123");
            System.out.println("  member1/member123: " + (memberUser != null ? "✓ 成功" : "✗ 失败"));
            
            System.out.println("\n============================================");
            System.out.println("修复完成！请重启 Tomcat 后测试登录。");
            System.out.println("============================================");
            
        } catch (Exception e) {
            System.out.println("\n修复过程中出错:");
            e.printStackTrace();
        }
    }
}
