package util;

import config.Config;
import dao.UserDAO;
import model.User;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * 登录问题诊断工具
 * 全面检查配置文件、加密、数据库连接
 */
public class LoginDiagnostic {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("      登录问题全面诊断工具");
        System.out.println("========================================\n");
        
        // 1. 检查配置文件
        checkConfigFiles();
        
        // 2. 检查当前使用的配置
        checkCurrentConfig();
        
        // 3. 测试DES加密
        testDESEncryption();
        
        // 4. 检查数据库连接
        testDatabaseConnection();
        
        // 5. 检查数据库中的密码
        checkDatabasePasswords();
        
        // 6. 测试登录
        testLogin();
        
        System.out.println("\n========================================");
        System.out.println("           诊断完成");
        System.out.println("========================================");
    }
    
    private static void checkConfigFiles() {
        System.out.println("【1】检查配置文件存在性:");
        String[] configFiles = {
            "src/main/resources/config.properties",
            "config/config.local.properties",
            "target/classes/config.properties",
            "target/software-group/WEB-INF/classes/config.properties"
        };
        
        for (String path : configFiles) {
            File file = new File(path);
            System.out.println("   " + (file.exists() ? "✓" : "✗") + " " + path);
            if (file.exists()) {
                try {
                    Properties props = new Properties();
                    props.load(new FileInputStream(file));
                    String desKey = props.getProperty("des.key");
                    String encKey = props.getProperty("encryption.des.key");
                    System.out.println("       des.key=" + desKey);
                    if (encKey != null) {
                        System.out.println("       encryption.des.key=" + encKey);
                    }
                } catch (Exception e) {
                    System.out.println("       读取失败: " + e.getMessage());
                }
            }
        }
        System.out.println();
    }
    
    private static void checkCurrentConfig() {
        System.out.println("【2】当前应用使用的配置:");
        System.out.println("   DES密钥: " + Config.getDesKey());
        System.out.println("   密钥长度: " + Config.getDesKey().length());
        System.out.println();
    }
    
    private static void testDESEncryption() {
        System.out.println("【3】DES加密测试:");
        String[] testPasswords = {"admin123", "member123", "123456"};
        for (String pwd : testPasswords) {
            String encrypted = DESUtil.encrypt(pwd);
            String decrypted = DESUtil.decrypt(encrypted);
            System.out.println("   " + pwd + " -> " + encrypted + " -> " + decrypted);
        }
        System.out.println();
    }
    
    private static void testDatabaseConnection() {
        System.out.println("【4】数据库连接测试:");
        try {
            java.sql.Connection conn = DBUtil.getConnection();
            System.out.println("   ✓ 数据库连接成功");
            conn.close();
        } catch (Exception e) {
            System.out.println("   ✗ 数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static void checkDatabasePasswords() {
        System.out.println("【5】数据库中的密码检查:");
        try {
            java.sql.Connection conn = DBUtil.getConnection();
            String sql = "SELECT username, password FROM user WHERE username IN ('admin', 'member1')";
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                String decrypted = DESUtil.decrypt(password);
                
                System.out.println("   用户名: " + username);
                System.out.println("   密文: " + password);
                System.out.println("   解密: " + decrypted);
                
                if (decrypted == null || decrypted.equals("null")) {
                    System.out.println("   ⚠️ 警告: 密码无法解密，密钥可能不匹配！");
                }
                System.out.println();
            }
            
            rs.close();
            pstmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("   查询失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testLogin() {
        System.out.println("【6】登录测试:");
        testUserLogin("admin", "admin123");
        testUserLogin("member1", "member123");
    }
    
    private static void testUserLogin(String username, String password) {
        try {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.findByUsernameAndPassword(username, password);
            if (user != null) {
                System.out.println("   ✓ " + username + "/" + password + " 登录成功");
            } else {
                System.out.println("   ✗ " + username + "/" + password + " 登录失败");
            }
        } catch (Exception e) {
            System.out.println("   ✗ " + username + " 测试出错: " + e.getMessage());
        }
    }
}
