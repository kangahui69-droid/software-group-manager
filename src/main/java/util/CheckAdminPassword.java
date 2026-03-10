package util;

/**
 * 检查管理员密码是否正确
 * 验证数据库中的加密密码解密后是否为admin123
 */
public class CheckAdminPassword {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("    验证管理员密码");
        System.out.println("========================================\n");
        
        // 数据库中的管理员加密密码
        String encryptedPassword = "sEVTxpZ/OQykR2QyQb9BWw==";
        
        System.out.println("数据库中的加密密码: " + encryptedPassword);
        System.out.println();
        
        // 使用DES解密
        System.out.println("正在解密密码...");
        String decryptedPassword = DESUtil.decrypt(encryptedPassword);
        
        if (decryptedPassword == null) {
            System.err.println("✗ 密码解密失败！");
            System.exit(1);
        }
        
        System.out.println("解密后的密码: " + decryptedPassword);
        System.out.println();
        
        // 验证是否为admin123
        String expectedPassword = "admin123";
        boolean isCorrect = expectedPassword.equals(decryptedPassword);
        
        System.out.println("期望的密码: " + expectedPassword);
        System.out.println("实际的密码: " + decryptedPassword);
        System.out.println("是否匹配: " + (isCorrect ? "✅ 是" : "❌ 否"));
        System.out.println();
        
        if (!isCorrect) {
            System.out.println("⚠️ 警告：密码不匹配！");
            System.out.println("数据库中的密码解密后是: " + decryptedPassword);
            System.out.println("但期望的密码是: " + expectedPassword);
            System.out.println();
            System.out.println("需要纠正密码为: " + expectedPassword);
        } else {
            System.out.println("✅ 验证通过！密码正确。");
        }
        
        System.out.println("\n========================================");
    }
}