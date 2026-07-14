package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置文件类
 *
 * 加载优先级（从高到低）：
 *   1. config.local.properties  （本地开发，不提交Git，存放真实密码）
 *   2. config.properties        （默认模板，提交到Git，只含占位符）
 *
 * 使用说明：
 * - 本地开发：复制 config.properties 为 config.local.properties，填入真实密码即可
 * - 生产/测试：通过 config.prod.properties / config.test.properties 或环境变量扩展
 */
public class Config {

    private static final String LOCAL_CONFIG_FILE = "config.local.properties";
    private static final String DEFAULT_CONFIG_FILE = "config.properties";
    private static final String DEFAULT_DES_KEY = "(^&%gasie_%^";
    private static final long DEFAULT_MAX_FILE_SIZE = 10485760L;
    private static final long DEFAULT_MAX_REQUEST_SIZE = 20971520L;
    private static final int DEFAULT_SESSION_TIMEOUT = 30;
    private static final String DEFAULT_UPLOAD_BASE_DIR = "${user.dir}/localstorage";
    private static final Properties properties = new Properties();

    static {
        loadConfig();
    }

    private static void loadConfig() {
        boolean loadedLocal = false;

        try (InputStream localStream = Config.class.getClassLoader().getResourceAsStream(LOCAL_CONFIG_FILE)) {
            if (localStream != null) {
                Properties localProps = new Properties();
                localProps.load(localStream);
                properties.putAll(localProps);
                loadedLocal = true;
                System.out.println("[Config] 已加载本地配置: " + LOCAL_CONFIG_FILE);
            }
        } catch (IOException e) {
            System.err.println("[Config] 加载本地配置失败: " + e.getMessage());
        }

        try (InputStream defaultStream = Config.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE)) {
            if (defaultStream != null) {
                Properties defaultProps = new Properties();
                defaultProps.load(defaultStream);
                for (String key : defaultProps.stringPropertyNames()) {
                    properties.putIfAbsent(key, defaultProps.getProperty(key));
                }
                if (!loadedLocal) {
                    System.out.println("[Config] 已加载默认配置: " + DEFAULT_CONFIG_FILE + "（未找到本地配置文件）");
                }
            } else {
                System.err.println("[Config] 警告: 无法找到配置文件 " + DEFAULT_CONFIG_FILE);
                setFallbackDesKey();
            }
        } catch (IOException e) {
            System.err.println("[Config] 加载默认配置失败: " + e.getMessage());
            setFallbackDesKey();
        }
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        return parseInteger(value, defaultValue);
    }

    public static long getLongProperty(String key, long defaultValue) {
        String value = getProperty(key);
        return parseLong(value, defaultValue);
    }

    public static void reloadConfig() {
        properties.clear();
        loadConfig();
    }

    public static String getDesKey() {
        return getProperty("des.key", DEFAULT_DES_KEY);
    }

    public static long getMaxFileSize() {
        return getLongProperty("upload.maxFileSize", DEFAULT_MAX_FILE_SIZE);
    }

    public static long getMaxRequestSize() {
        return getLongProperty("upload.maxRequestSize", DEFAULT_MAX_REQUEST_SIZE);
    }

    public static int getSessionTimeout() {
        return getIntProperty("session.timeout", DEFAULT_SESSION_TIMEOUT);
    }

    private static int parseInteger(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static long parseLong(String value, long defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static void setFallbackDesKey() {
        properties.setProperty("des.key", DEFAULT_DES_KEY);
    }

    /**
     * 获取文件存储根目录绝对路径
     * 支持占位符：${user.dir}（项目根目录）、${catalina.base}（Tomcat根目录）
     */
    public static String getFileStorageBaseDir() {
        String value = getProperty("upload.baseDir", DEFAULT_UPLOAD_BASE_DIR);
        value = value.replace("${user.dir}", System.getProperty("user.dir", "."));
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null) {
            value = value.replace("${catalina.base}", catalinaBase);
        }
        java.io.File dir = new java.io.File(value);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getAbsolutePath();
    }
}
