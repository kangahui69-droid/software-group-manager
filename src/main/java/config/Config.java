package config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Function;

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

    // ==================== 配置文件名 ====================
    private static final String LOCAL_CONFIG_FILE = "config.local.properties";
    private static final String DEFAULT_CONFIG_FILE = "config.properties";

    // ==================== DES加密默认值 ====================
    private static final String DEFAULT_DES_KEY = "(^&%gasie_%^";

    // ==================== 文件上传默认值 ====================
    private static final long DEFAULT_MAX_FILE_SIZE = 10485760L;
    private static final long DEFAULT_MAX_REQUEST_SIZE = 20971520L;
    private static final String DEFAULT_UPLOAD_BASE_DIR = "${user.dir}/localstorage";

    // ==================== 会话默认值 ====================
    private static final int DEFAULT_SESSION_TIMEOUT = 30;

    // ==================== 数据库默认值 ====================
    private static final String DEFAULT_DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/software_group?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_DB_USERNAME = "root";
    private static final String DEFAULT_DB_PASSWORD = "";

    // ==================== HikariCP连接池默认值 ====================
    private static final int DEFAULT_HIKARI_MAX_POOL_SIZE = 20;
    private static final int DEFAULT_HIKARI_MIN_IDLE = 5;
    private static final long DEFAULT_HIKARI_CONNECTION_TIMEOUT = 30000L;
    private static final long DEFAULT_HIKARI_IDLE_TIMEOUT = 600000L;
    private static final long DEFAULT_HIKARI_MAX_LIFETIME = 1800000L;
    private static final String DEFAULT_HIKARI_CONNECTION_TEST_QUERY = "SELECT 1";
    private static final long DEFAULT_HIKARI_VALIDATION_TIMEOUT = 5000L;

    private static final Properties properties = new Properties();

    static {
        loadConfig();
    }

    // ==================== 配置加载 ====================

    private static void loadConfig() {
        boolean loadedLocal = loadLocalConfig();
        loadDefaultConfig(loadedLocal);
    }

    private static boolean loadLocalConfig() {
        Properties localProps = loadPropertiesFromResource(LOCAL_CONFIG_FILE);
        if (localProps == null) {
            return false;
        }
        properties.putAll(localProps);
        System.out.println("[Config] 已加载本地配置: " + LOCAL_CONFIG_FILE);
        return true;
    }

    private static void loadDefaultConfig(boolean loadedLocal) {
        Properties defaultProps = loadPropertiesFromResource(DEFAULT_CONFIG_FILE);
        if (defaultProps == null) {
            System.err.println("[Config] 警告: 无法找到配置文件 " + DEFAULT_CONFIG_FILE);
            setFallbackDesKey();
            return;
        }
        for (String key : defaultProps.stringPropertyNames()) {
            properties.putIfAbsent(key, defaultProps.getProperty(key));
        }
        if (!loadedLocal) {
            System.out.println("[Config] 已加载默认配置: " + DEFAULT_CONFIG_FILE + "（未找到本地配置文件）");
        }
    }

    private static Properties loadPropertiesFromResource(String resourceName) {
        try (InputStream stream = Config.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (stream == null) {
                return null;
            }
            Properties props = new Properties();
            props.load(stream);
            return props;
        } catch (IOException e) {
            System.err.println("[Config] 加载配置文件失败 " + resourceName + ": " + e.getMessage());
            return null;
        }
    }

    public static void reloadConfig() {
        properties.clear();
        loadConfig();
    }

    // ==================== 通用属性读取 ====================

    public static String getProperty(String key, String defaultValue) {
        String value = properties.getProperty(key, defaultValue);
        return value == null ? null : resolve(value);
    }

    public static String getProperty(String key) {
        return getProperty(key, null);
    }

    public static int getIntProperty(String key, int defaultValue) {
        return parseConfigValue(getProperty(key), defaultValue, Integer::parseInt);
    }

    public static long getLongProperty(String key, long defaultValue) {
        return parseConfigValue(getProperty(key), defaultValue, Long::parseLong);
    }

    // ==================== DES加密配置 ====================

    public static String getDesKey() {
        return parseString(getProperty("des.key"), DEFAULT_DES_KEY);
    }

    // ==================== 文件上传配置 ====================

    public static long getMaxFileSize() {
        return getLongProperty("upload.maxFileSize", DEFAULT_MAX_FILE_SIZE);
    }

    public static long getMaxRequestSize() {
        return getLongProperty("upload.maxRequestSize", DEFAULT_MAX_REQUEST_SIZE);
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
        File dir = new File(value);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getAbsolutePath();
    }

    // ==================== 会话配置 ====================

    public static int getSessionTimeout() {
        return getIntProperty("session.timeout", DEFAULT_SESSION_TIMEOUT);
    }

    // ==================== 数据库配置 ====================

    public static String getDbDriver() {
        return parseString(getProperty("db.driver"), DEFAULT_DB_DRIVER);
    }

    public static String getDbUrl() {
        return parseString(getProperty("db.url"), DEFAULT_DB_URL);
    }

    public static String getDbUsername() {
        return parseString(getProperty("db.username"), DEFAULT_DB_USERNAME);
    }

    public static String getDbPassword() {
        return getProperty("db.password", DEFAULT_DB_PASSWORD);
    }

    // ==================== HikariCP连接池配置 ====================

    public static int getHikariMaximumPoolSize() {
        return getIntProperty("hikaricp.maximumPoolSize", DEFAULT_HIKARI_MAX_POOL_SIZE);
    }

    public static int getHikariMinimumIdle() {
        return getIntProperty("hikaricp.minimumIdle", DEFAULT_HIKARI_MIN_IDLE);
    }

    public static long getHikariConnectionTimeout() {
        return getLongProperty("hikaricp.connectionTimeout", DEFAULT_HIKARI_CONNECTION_TIMEOUT);
    }

    public static long getHikariIdleTimeout() {
        return getLongProperty("hikaricp.idleTimeout", DEFAULT_HIKARI_IDLE_TIMEOUT);
    }

    public static long getHikariMaxLifetime() {
        return getLongProperty("hikaricp.maxLifetime", DEFAULT_HIKARI_MAX_LIFETIME);
    }

    public static String getHikariConnectionTestQuery() {
        return parseString(getProperty("hikaricp.connectionTestQuery"), DEFAULT_HIKARI_CONNECTION_TEST_QUERY);
    }

    public static long getHikariValidationTimeout() {
        return getLongProperty("hikaricp.validationTimeout", DEFAULT_HIKARI_VALIDATION_TIMEOUT);
    }

    // ==================== 值解析辅助方法 ====================

    private static <T> T parseConfigValue(String value, T defaultValue, Function<String, T> parser) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return parser.apply(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static String parseString(String value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return defaultValue;
        }
        return trimmed;
    }

    // ==================== 运行期占位符解析 ====================

    /**
     * 展开配置值里的 ${NAME:-default} 占位符。
     *
     * 环境变量非空则用环境变量值，否则用默认值；默认值本身可含嵌套占位符，递归展开。
     * 按花括号配对定位，避免 {@code ${A:-${B}/x}} 这种嵌套默认值被截断
     * （upload.baseDir 的默认值就是嵌套形式）。
     *
     * 不含 ":-" 的形式（如 ${user.dir}）原样保留，交由 getFileStorageBaseDir() 处理。
     */
    private static String resolve(String value) {
        if (value.indexOf("${") < 0) {
            return value;
        }
        StringBuilder result = new StringBuilder();
        int cursor = 0;
        while (true) {
            int start = value.indexOf("${", cursor);
            if (start < 0) {
                result.append(value, cursor, value.length());
                return result.toString();
            }
            int end = findClosingBrace(value, start + 2);
            if (end < 0) {
                result.append(value, cursor, value.length());
                return result.toString();
            }
            result.append(value, cursor, start);
            result.append(expand(value.substring(start + 2, end)));
            cursor = end + 1;
        }
    }

    private static int findClosingBrace(String value, int from) {
        int depth = 1;
        for (int i = from; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}' && --depth == 0) {
                return i;
            }
        }
        return -1;
    }

    private static String expand(String placeholder) {
        int separator = placeholder.indexOf(":-");
        if (separator < 0) {
            return "${" + placeholder + "}";
        }
        String name = placeholder.substring(0, separator).trim();
        String envValue = System.getenv(name);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue;
        }
        return resolve(placeholder.substring(separator + 2));
    }

    private static void setFallbackDesKey() {
        properties.setProperty("des.key", DEFAULT_DES_KEY);
    }
}
