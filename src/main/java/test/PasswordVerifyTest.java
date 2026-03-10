package test;

import util.DESUtil;

/**
 * 密码验证测试 - 检查数据库中的加密密码
 */
public class PasswordVerifyTest {
    
    public static void main(String[] args) {
        System.out.println("===== 数据库密码验证测试 =====\n");
        
        // 数据库中的加密密码
        String dbAdminPassword = "sEVTxpZ/OQykR2QyQb9BWw==";
        String dbMemberPassword = "Bkj4xIPxGT0=";
        
        // 预期的明文密码
        String expectedAdminPlain = "admin123";
        String expectedMemberPlain = "member123";
        
        System.out.println("1. 管理员账号 (admin) 验证:");
        System.out.println("   数据库加密密码: " + dbAdminPassword);
        System.out.println("   预期明文密码: " + expectedAdminPlain);
        
        // 测试解密数据库密码
        String decryptedAdmin = DESUtil.decrypt(dbAdminPassword);
        System.out.println("   解密数据库密码: " + decryptedAdmin);
        
        if (expectedAdminPlain.equals(decryptedAdmin)) {
            System.out.println("   ✅ 验证成功！密码正确");
        } else {
            System.out.println("   ❌ 验证失败！密码不匹配");
            System.out.println("   预期: " + expectedAdminPlain);
            System.out.println("   实际: " + decryptedAdmin);
        }
        
        // 测试加密明文密码
        String encryptedAdmin = DESUtil.encrypt(expectedAdminPlain);
        System.out.println("   加密明文密码: " + encryptedAdmin);
        System.out.println("   是否与数据库匹配: " + dbAdminPassword.equals(encryptedAdmin));
        
        System.out.println("\n2. 成员账号 (member1) 验证:");
        System.out.println("   数据库加密密码: " + dbMemberPassword);
        System.out.println("   预期明文密码: " + expectedMemberPlain);
        
        // 测试解密数据库密码
        String decryptedMember = DESUtil.decrypt(dbMemberPassword);
        System.out.println("   解密数据库密码: " + decryptedMember);
        
        if (expectedMemberPlain.equals(decryptedMember)) {
            System.out.println("   ✅ 验证成功！密码正确");
        } else {
            System.out.println("   ❌ 验证失败！密码不匹配");
            System.out.println("   预期: " + expectedMemberPlain);
            System.out.println("   实际: " + decryptedMember);
        }
        
        // 测试加密明文密码
        String encryptedMember = DESUtil.encrypt(expectedMemberPlain);
        System.out.println("   加密明文密码: " + encryptedMember);
        System.out.println("   是否与数据库匹配: " + dbMemberPassword.equals(encryptedMember));
        
        System.out.println("\n===== 测试完成 =====");
        
        // 如果有问题，提供解决方案
        if (!expectedAdminPlain.equals(decryptedAdmin) || !expectedMemberPlain.equals(decryptedMember)) {
            System.out.println("\n⚠️ 发现密码不匹配问题！");
            System.out.println("\n可能的原因：");
            System.out.println("1. DES加密密钥已更改");
            System.out.println("2. 数据库中的密码是用不同的密钥加密的");
            System.out.println("3. 加密算法或参数有变化");
            System.out.println("\n解决方案：");
            System.out.println("1. 检查 config/config.local.properties 中的 encryption.des.key");
            System.out.println("2. 如果密钥已更改，需要重新初始化数据库密码");
            System.out.println("3. 或者使用SQL直接更新密码字段");
        }
    }
}