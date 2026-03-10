import util.DESUtil;

public class test_decrypt {
    public static void main(String[] args) {
        // 数据库中存储的加密密码
        String encryptedPassword = "sEVTxpZ/OQykR2QyQb9BWw==";
        
        // 解密
        String decrypted = DESUtil.decrypt(encryptedPassword);
        System.out.println("数据库中admin账号的实际密码是: " + decrypted);
        
        // 验证加密和解密是否匹配
        String testPassword = "admin123";
        String testEncrypted = DESUtil.encrypt(testPassword);
        System.out.println("测试密码 'admin123' 加密后: " + testEncrypted);
        String testDecrypted = DESUtil.decrypt(testEncrypted);
        System.out.println("再解密后: " + testDecrypted);
    }
}
