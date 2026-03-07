/*
 * @Author: 张坤 penck1983@msn.com
 * @Date: 2026-01-21 16:32:28
 * @LastEditors: 张坤 penck1983@msn.com
 * @LastEditTime: 2026-01-22 08:47:16
 * @FilePath: /SoftwareGroupManagement/src/util/UpdatePasswordEncryption.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package util;

import dao.UserDAO;
import model.User;

import java.util.List;

/**
 * 密码加密更新工具类
 * 用于将user表中的所有密码更新为DES加密后的信息
 */
public class UpdatePasswordEncryption {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        
        try {
            // 获取所有用户
            List<User> users = userDAO.findAll();
            System.out.println("Found " + users.size() + " users");
            
            int updatedCount = 0;
            
            // 遍历用户，更新密码加密方式
            for (User user : users) {
                if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                    // 对密码进行DES加密
                    String encryptedPassword = DESUtil.encrypt(user.getPassword());
                    
                    if (encryptedPassword != null) {
                        // 直接更新加密后的密码
                        boolean success = userDAO.updatePasswordDirect(user.getId(), encryptedPassword);
                        if (success) {
                            updatedCount++;
                            System.out.println("Updated password for user: " + user.getUsername());
                        } else {
                            System.out.println("Failed to update password for user: " + user.getUsername());
                        }
                    } else {
                        System.out.println("Failed to encrypt password for user: " + user.getUsername());
                    }
                }
            }
            
            System.out.println("Update completed. Updated " + updatedCount + " users out of " + users.size());
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error updating password encryption: " + e.getMessage());
        }
    }
}