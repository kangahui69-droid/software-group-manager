package util;

import config.Config;
import dao.UserDAO;
import model.User;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * 登录问题诊断工具
 * 全面检查配置文件、加密、数据库
 */
public class LoginDiagnosticTool {
    
    public static void main(String[] args) {
        System.out.println("============================================");
        System.out.println("        登录问题诊断工具");
        System.out.println("============================================\n");
        
        // 1. 检查所有配置文件
        checkAllConfigFiles();
        
        // 2. 检查当前应用配置
        checkCurrentAppConfig();
        
        // 3. 测试加密
        testEncryption();
        
        // 4. 检查数据库
        checkDatabase();
        
        // 5. 诊断结论
        showDiagnosis();
        
        System.out.println("\n============================================");
        System.out.println("        诊断完成");
        System.out.println("============================================");
    }
    
    private static void checkAllConfigFiles() {
        System.out.println("【步骤1】检查所有配置文件\n");
        
        String[][] configs = {
            {"src/main/resources/config.properties", "主配置文件"},
            {"config/config.local.properties", "本地配置"},
            {"target/classes/config.properties", "编译后配置"},
            {"target/software-group/WEB-INF/classes/config.properties", "部署配置"}
        };
        
        for (String[] config : configs) {
            File file = new File(config[0]);
            System.out.println("  " + config[1] + ":");
            System.out.println("    路径: " + config[0]);
            System.out.println("    存在: " + (file.exists() ? "✓ 是" : "✗ 否"));
            
            if (file.exists()) {
                try {
                    Properties props = new Properties();
                    props.load(new FileInputStream(file));
                    
                    String desKey = props.getProperty("des.key");
                    String encKey = props.getProperty("encryption.des.key");
                    String dbPass = props.getProperty("db.password");
                    
                    if (desKey != null) {
                        System.out.println("    des.key: " + desKey);
                    }
                    if (encKey != null) {
                        System.out.println("    encryption.des.key: " + encKey);
                    }
                    if (dbPass != null) {
                        System.out.println("    db.password: " + (dbPass.isEmpty() ? "(空)" : dbPass.replaceAll(".", "*")));
                    }
                } catch (Exception e) {
                    System.out.println("    读取失败: " + e.getMessage());
                }
            }
            System.out.println();
        }
    }
    
    private static void checkCurrentAppConfig() {
        System.out.println("【步骤2】当前应用实际使用的配置\n");
        System.out.println("  DES密钥: " + Config.getDesKey());
        System.out.println("  密钥长度: " + Config.getDesKey().length() + " 字节");
        System.out.println();
    }
    
    private static void testEncryption() {
        System.out.println("【步骤3】DES加密测试\n");
        
        String[] passwords = {"admin123", "member123", "123456"};
        System.out.println("  明文 -> 密文 -> 解密");
        System.out.println("  ---------------------------");
        
        for (String pwd : passwords) {
            String encrypted = DESUtil.encrypt(pwd);
            String decrypted = DESUtil.decrypt(encrypted);
            System.out.println("  " + padRight(pwd, 12) + " -> " + encrypted + " -> " + decrypted);
        }
        System.out.println();
    }
    
    private static void checkDatabase() {
        System.out.println("【步骤4】数据库检查\n");
        
        // 4.1 连接测试
        System.out.println("  4.1 数据库连接:");
        try {
            Connection conn = DBUtil.getConnection();
            System.out.println("      ✓ 连接成功");
            conn.close();
        } catch (Exception e) {
            System.out.println("      ✗ 连接失败: " + e.getMessage());
            return;
        }
        
        // 4.2 用户表检查
        System.out.println("\n  4.2 用户密码检查:");
        try {
            Connection conn = DBUtil.getConnection();
            String sql = "SELECT username, password, status FROM user WHERE username IN ('admin', 'member1')";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            boolean foundAdmin = false;
            boolean foundMember = false;
            
            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                int status = rs.getInt("status");
                String decrypted = DESUtil.decrypt(password);
                
                System.out.println("      用户名: " + username);
                System.out.println("      密文: " + password);
                System.out.println("      解密: " + decrypted);
                System.out.println("      状态: " + (status == 1 ? "启用" : "禁用"));
                
                if (decrypted == null || decrypted.equals("null")) {
                    System.out.println("      ⚠️ 警告: 密码无法解密，密钥不匹配！");
                }
                System.out.println();
                
                if ("admin".equals(username)) foundAdmin = true;
                if ("member1".equals(username)) foundMember = true;
            }
            
            if (!foundAdmin) {
                System.out.println("      ✗ 未找到 admin 用户");
            }
            if (!foundMember) {
                System.out.println("      ✗ 未找到 member1 用户");
            }
            
            rs.close();
            pstmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("      查询失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void showDiagnosis() {
        System.out.println("【步骤5】诊断结论\n");
        System.out.println("  请根据以上检查结果判断问题原因:\n");
        System.out.println("  常见问题:");
        System.out.println("  1. 配置文件中的 des.key 不一致");
        System.out.println("  2. 数据库密码使用了错误的密钥加密");
        System.out.println("  3. Tomcat 未重启，使用旧配置");
        System.out.println("  4. 数据库连接配置错误\n");
    }
    
    private static String padRight(String s, int n) {
        if (s.length() >= n) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < n) {
            sb.append(" ");
        }
        return sb.toString();
    }
}
