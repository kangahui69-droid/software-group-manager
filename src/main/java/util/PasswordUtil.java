package util;

import java.util.Random;

/**
 * 密码工具类
 */
public class PasswordUtil {

    private static final String DIGITS = "0123456789";
    private static final Random RANDOM = new Random();

    /**
     * 生成指定长度的随机数字密码
     * @param length 密码长度
     * @return 随机密码
     */
    public static String generateRandomPassword(int length) {
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        }
        return password.toString();
    }

    /**
     * 生成6位数字随机密码
     * @return 6位随机密码
     */
    public static String generateTemporaryPassword() {
        return generateRandomPassword(6);
    }

    /**
     * 私有构造方法，防止实例化
     */
    private PasswordUtil() {
        throw new UnsupportedOperationException("工具类不能实例化");
    }
}
