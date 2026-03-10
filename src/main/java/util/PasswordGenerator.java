package util;

/**
 * 密码生成工具 - 用于生成正确的加密密码
 */
public class PasswordGenerator {
    
    public static void main(String[] args) {
        // 生成 admin123 的加密密码
        String adminPassword = "admin123";
        String encryptedAdmin = DESUtil.encrypt(adminPassword);
        System.out.println("admin123 加密后: " + encryptedAdmin);
        
        // 生成 member123 的加密密码
        String memberPassword = "member123";
        String encryptedMember = DESUtil.encrypt(memberPassword);
        System.out.println("member123 加密后: " + encryptedMember);
        
        // 生成 123456 的加密密码（旧密码）
        String oldPassword = "123456";
        String encryptedOld = DESUtil.encrypt(oldPassword);
        System.out.println("123456 加密后: " + encryptedOld);
        
        // 验证解密
        System.out.println("\n验证解密:");
        System.out.println("解密 admin 密码: " + DESUtil.decrypt(encryptedAdmin));
        System.out.println("解密 member 密码: " + DESUtil.decrypt(encryptedMember));
    }
}
