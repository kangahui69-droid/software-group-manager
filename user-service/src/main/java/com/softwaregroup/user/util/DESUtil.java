package com.softwaregroup.user.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * DES加密工具类
 *
 * 用于密码加密，保持与旧系统兼容
 */
@Component
public class DESUtil {

    private static final String ALGORITHM = "DES/CBC/PKCS5Padding";
    private static final String CHARSET = "UTF-8";

    private final String desKey;

    public DESUtil(@Value("${spring.des.key:(^&%gasie_%^}") String desKey) {
        this.desKey = desKey;
    }

    /**
     * 加密方法
     * @param content 需要加密的内容
     * @return 加密后的字符串
     */
    public String encrypt(String content) {
        try {
            SecretKey secretKey = generateKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[8];
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(content.getBytes(CHARSET));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("DES加密失败", e);
        }
    }

    /**
     * 解密方法
     * @param encryptedContent 加密后的内容
     * @return 解密后的字符串
     */
    public String decrypt(String encryptedContent) {
        try {
            SecretKey secretKey = generateKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[8];
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedContent));
            return new String(decrypted, CHARSET);
        } catch (Exception e) {
            throw new RuntimeException("DES解密失败", e);
        }
    }

    /**
     * 生成密钥
     */
    private SecretKey generateKey() throws Exception {
        DESKeySpec keySpec = new DESKeySpec(desKey.getBytes(CHARSET));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        return keyFactory.generateSecret(keySpec);
    }
}
