package util;

import config.Config;

/**
 * 解密测试 - 验证 K0hA/4q9nB8= 的解密结果
 */
public class DecryptTest {
    public static void main(String[] args) {
        System.out.println("=== DES 解密测试 ===\n");
        
        // 当前使用的 DES 密钥
        String desKey = Config.getDesKey();
        System.out.println("当前 DES 密钥: " + desKey);
        System.out.println("密钥长度: " + desKey.length() + " 字节\n");
        
        // 要解密的字符串
        String encrypted = "K0hA/4q9nB8=";
        System.out.println("密文: " + encrypted);
        
        // 解密
        String decrypted = DESUtil.decrypt(encrypted);
        System.out.println("解密结果: " + decrypted);
        
        // 验证加密一致性
        System.out.println("\n=== 验证加密一致性 ===");
        String reEncrypted = DESUtil.encrypt(decrypted);
        System.out.println("重新加密: " + reEncrypted);
        System.out.println("与原文相同: " + encrypted.equals(reEncrypted));
    }
}
