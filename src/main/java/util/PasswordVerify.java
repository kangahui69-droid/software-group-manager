package util;

import config.Config;

/**
 * 密码验证工具 - 用于验证当前加密密钥与数据库密码是否匹配
 */
public class PasswordVerify {
    
    public static void main(String[] args) {
        System.out.println("=== 密码验证工具 ===\n");
        
        // 显示当前使用的 DES 密钥
        String desKey = Config.getDesKey();
        System.out.println("当前 DES 密钥: " + desKey);
        System.out.println("密钥长度: " + desKey.length() + " 字节\n");
        
        // 测试加密明文密码
        String[] testPasswords = {"admin123", "member123", "123456", "admin", ""};
        System.out.println("=== 测试加密 ===");
        for (String password : testPasswords) {
            String encrypted = DESUtil.encrypt(password);
            String decrypted = DESUtil.decrypt(encrypted);
            System.out.println("明文: " + password + " -> 密文: " + encrypted + " -> 解密: " + decrypted);
        }
        
        System.out.println("\n=== 验证数据库中的密码 ===");
        // 数据库中的实际密码
        String[][] dbPasswords = {
            {"admin", "sEVTxpZ/OQykR2QyQb9BWw=="},
            {"member1", "Bkj4xIPxGT0="},
            {"member1", "qlkkHyFnxfg="}
        };
        
        for (String[] entry : dbPasswords) {
            String username = entry[0];
            String encrypted = entry[1];
            String decrypted = DESUtil.decrypt(encrypted);
            System.out.println("用户: " + username + ", 密文: " + encrypted + ", 解密: " + decrypted);
        }
        
        System.out.println("\n=== 结论 ===");
        System.out.println("如果解密结果是 'null'，说明 DES 密钥不匹配！");
        System.out.println("需要执行 fix_password.sql 脚本来修复密码。");
    }
}
