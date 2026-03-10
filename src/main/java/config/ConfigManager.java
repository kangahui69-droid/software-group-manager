package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置管理类
 * 支持从不同环境加载配置
 * 
 * 使用方法：
 * 1. 在 config 目录下创建配置文件：
 *    - config.local.properties (本地开发)
 *    - config.test.properties (测试环境)
 *    - config.prod.properties (生产环境)
 * 2. 通过环境变量或系统属性指定当前环境：
 *    -Denv=local 或 -Denv=prod
 * 3. 在代码中使用：ConfigManager.getProperty("db.url")
 */
public class ConfigManager {
    
    private static final String CONFIG_DIR = "config/";
    private static final String DEFAULT_ENV = "local";
    private static final String CONFIG_FILE_PREFIX = "config.";
    private static final String CONFIG_FILE_SUFFIX = ".properties";
    
    private static Properties properties = new Properties();
    private static boolean loaded = false;
    
    /**
     * 获取配置属性
     * 
     * @param key 属性键
     * @return 属性值，如果不存在返回 null
     */
    public static String getProperty(String key) {
        ensureLoaded();
        return properties.getProperty(key);
    }
    
    /**
     * 获取配置属性，如果不存在返回默认值
     * 
     * @param key 属性键
     * @param defaultValue 默认值
     * @return 属性值或默认值
     */
    public static String getProperty(String key, String defaultValue) {
        ensureLoaded();
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * 获取整数类型的配置值
     * 
     * @param key 属性键
     * @param defaultValue 默认值
     * @return 整数值
     */
    public static int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("配置项 " + key + " 的值不是有效的整数: " + value);
            return defaultValue;
        }
    }
    
    /**
     * 获取布尔类型的配置值
     * 
     * @param key 属性键
     * @param defaultValue 默认值
     * @return 布尔值
     */
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        value = value.trim().toLowerCase();
        return value.equals("true") || value.equals("yes") || value.equals("1") || value.equals("on") || value.equals("enabled");
    }
    
    /**
     * 重新加载配置
     * 可用于在运行时刷新配置
     */
    public static void reload() {
        loaded = false;
        properties.clear();
        loadConfiguration();
    }
    
    /**
     * 确保配置已加载
     */
    private static void ensureLoaded() {
        if (!loaded) {
            loadConfiguration();
        }
    }
    
    /**
     * 加载配置文件
     */
    private static void loadConfiguration() {
        // 获取当前环境
        String env = getCurrentEnvironment();
        System.out.println("[ConfigManager] 当前环境: " + env);
        
        // 先加载默认配置（模板文件）
        loadPropertiesFromFile(CONFIG_FILE_PREFIX + "properties.template");
        
        // 再加载环境特定配置（覆盖默认值）
        String envConfigFile = CONFIG_FILE_PREFIX + env + CONFIG_FILE_SUFFIX;
        boolean envConfigLoaded = loadPropertiesFromFile(envConfigFile);
        
        if (!envConfigLoaded && !env.equals("local")) {
            System.err.println("[ConfigManager] 警告: 环境配置文件不存在: " + envConfigFile);
            System.err.println("[ConfigManager] 请从 config.properties.template 复制创建该文件");
        }
        
        loaded = true;
        
        // 打印加载的配置信息（调试用，生产环境建议注释掉）
        if (getBooleanProperty("system.debug", false)) {
            System.out.println("\n[ConfigManager] 已加载的配置项:");
            properties.forEach((key, value) -> {
                // 隐藏敏感信息
                String displayValue = key.toString().toLowerCase().contains("password") || 
                                     key.toString().toLowerCase().contains("secret") ||
                                     key.toString().toLowerCase().contains("key")
                                     ? "******" : value.toString();
                System.out.println("  " + key + " = " + displayValue);
            });
            System.out.println();
        }
    }
    
    /**
     * 从文件加载配置
     * 
     * @param filename 文件名
     * @return 是否成功加载
     */
    private static boolean loadPropertiesFromFile(String filename) {
        InputStream is = null;
        try {
            // 尝试从文件系统加载（开发环境）
            String filePath = CONFIG_DIR + filename;
            try {
                is = new FileInputStream(filePath);
                System.out.println("[ConfigManager] 从文件系统加载: " + filePath);
            } catch (IOException e) {
                // 文件系统加载失败，尝试从classpath加载
                is = ConfigManager.class.getClassLoader().getResourceAsStream(filePath);
                if (is != null) {
                    System.out.println("[ConfigManager] 从classpath加载: " + filePath);
                }
            }
            
            // 如果找到了输入流，加载配置
            if (is != null) {
                Properties tempProps = new Properties();
                tempProps.load(is);
                
                // 合并到主配置中（后加载的覆盖先加载的）
                properties.putAll(tempProps);
                return true;
            }
            
            return false;
        } catch (IOException e) {
            System.err.println("[ConfigManager] 加载配置文件失败: " + filename + " - " + e.getMessage());
            return false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // 忽略关闭异常
                }
            }
        }
    }
    
    /**
     * 获取当前环境
     * 优先级：
     * 1. 系统属性：-Denv=xxx
     * 2. 环境变量：ENV=xxx
     * 3. 默认值：local
     * 
     * @return 环境名称（local/test/prod）
     */
    private static String getCurrentEnvironment() {
        // 1. 检查系统属性（JVM参数）
        String env = System.getProperty("env");
        if (env != null && !env.trim().isEmpty()) {
            return env.trim().toLowerCase();
        }
        
        // 2. 检查环境变量
        env = System.getenv("ENV");
        if (env != null && !env.trim().isEmpty()) {
            return env.trim().toLowerCase();
        }
        
        // 3. 返回默认值
        return DEFAULT_ENV;
    }
}