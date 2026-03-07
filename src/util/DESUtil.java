package util;

import config.Config;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * DES加密工具类
 */
public class DESUtil {
    private static final String ALGORITHM = "DES/CBC/PKCS5Padding";
    private static final String CHARSET = "UTF-8";

    /**
     * 加密方法
     * @param content 需要加密的内容
     * @return 加密后的字符串
     */
    public static String encrypt(String content) {
        try {
            // 创建密钥
            SecretKey secretKey = generateKey();
            // 创建加密器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            // 使用固定的初始化向量，确保相同明文生成相同密文
            byte[] iv = new byte[8]; // DES使用8字节IV
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            // 加密
            byte[] encrypted = cipher.doFinal(content.getBytes(CHARSET));
            // 使用Base64编码
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解密方法
     * @param encryptedContent 加密后的内容
     * @return 解密后的字符串
     */
    public static String decrypt(String encryptedContent) {
        try {
            // 创建密钥
            SecretKey secretKey = generateKey();
            // 创建解密器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            // 使用相同的固定初始化向量
            byte[] iv = new byte[8]; // DES使用8字节IV
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            // 解密
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedContent));
            // 转换为字符串
            return new String(decrypted, CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成密钥
     * @return 密钥
     */
    private static SecretKey generateKey() throws Exception {
        // 使用配置文件中的密钥
        DESKeySpec keySpec = new DESKeySpec(Config.DES_KEY.getBytes(CHARSET));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        return keyFactory.generateSecret(keySpec);
    }
}