package util;

import dao.UserDAO;
import model.User;

/**
 * 重置管理员密码工具类
 * 用于将管理员密码重置为admin123，使用DES加密方式
 */
public class ResetAdminPassword {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        
        try {
            // 查找管理员用户
            User adminUser = userDAO.findByUsername("admin");
            
            if (adminUser != null) {
                System.out.println("Found admin user: " + adminUser.getUsername());
                
                // 重置密码为admin123
                boolean success = userDAO.updatePassword(adminUser.getId(), "admin123");
                
                if (success) {
                    System.out.println("Admin password reset successfully to: admin123");
                } else {
                    System.out.println("Failed to reset admin password");
                }
            } else {
                System.out.println("Admin user not found");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error resetting admin password: " + e.getMessage());
        }
    }
}