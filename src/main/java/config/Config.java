package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置文件类
 * 从配置文件加载敏感信息，避免硬编码
 */
public class Config {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties properties = new Properties();

    // 静态初始化块，加载配置文件
    static {
        loadConfig();
    }

    /**
     * 加载配置文件
     */
    private static void loadConfig() {
        try (InputStream inputStream = Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                System.err.println("[Config] 警告: 无法找到配置文件 " + CONFIG_FILE + ", 使用默认配置");
                // 设置默认值
                properties.setProperty("des.key", "(^&%gasie_%^");
                return;
            }
            properties.load(inputStream);
            System.out.println("[Config] 配置文件加载成功");
        } catch (IOException e) {
            System.err.println("[Config] 加载配置文件时出错: " + e.getMessage());
            // 设置默认值
            properties.setProperty("des.key", "(^&%gasie_%^");
        }
    }

    /**
     * 获取配置项
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * 获取配置项
     * @param key 配置键
     * @return 配置值，如果不存在返回null
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * 重新加载配置文件
     * 可用于在不重启应用的情况下更新配置
     */
    public static void reloadConfig() {
        properties.clear();
        loadConfig();
    }

    // ============== 预定义配置项 ==============

    /**
     * DES加密密钥
     * 从配置文件读取，默认值为兼容旧版本
     */
    public static String getDesKey() {
        return getProperty("des.key", "(^&%gasie_%^");
    }

    /**
     * 文件上传最大大小（字节）
     */
    public static long getMaxFileSize() {
        String value = getProperty("upload.maxFileSize", "10485760");
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 10485760L; // 默认10MB
        }
    }

    /**
     * 文件上传最大请求大小（字节）
     */
    public static long getMaxRequestSize() {
        String value = getProperty("upload.maxRequestSize", "20971520");
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 20971520L; // 默认20MB
        }
    }

    /**
     * 会话超时时间（分钟）
     */
    public static int getSessionTimeout() {
        String value = getProperty("session.timeout", "30");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 30; // 默认30分钟
        }
    }
}
