package config;

import java.io.*;
import java.util.Properties;

/**
 * 简化的配置管理类
 * 
 * 使用方法：
 * 1. 复制 config.properties.template 为 config.local.properties
 * 2. 修改 config.local.properties 中的数据库配置
 * 3. 确保 config.local.properties 不会被提交到Git（已添加到.gitignore）
 * 4. 在代码中使用：String dbUrl = SimpleConfig.get("db.url");
 */
public class SimpleConfig {
    
    private static final String CONFIG_FILE = "config/config.local.properties";
    private static final String TEMPLATE_FILE = "config/config.properties.template";
    private static Properties props = new Properties();
    private static boolean loaded = false;
    
    /**
     * 获取配置值
     * @param key 配置键
     * @return 配置值，如果不存在返回null
     */
    public static String get(String key) {
        ensureLoaded();
        return props.getProperty(key);
    }
    
    /**
     * 获取配置值，带默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    public static String get(String key, String defaultValue) {
        ensureLoaded();
        return props.getProperty(key, defaultValue);
    }
    
    /**
     * 确保配置已加载
     */
    private static void ensureLoaded() {
        if (!loaded) {
            loadConfig();
        }
    }
    
    /**
     * 加载配置文件
     */
    private static void loadConfig() {
        InputStream is = null;
        
        try {
            // 首先尝试加载本地配置文件
            File localConfigFile = new File(CONFIG_FILE);
            if (localConfigFile.exists()) {
                is = new FileInputStream(localConfigFile);
                System.out.println("[SimpleConfig] 已加载本地配置: " + CONFIG_FILE);
            } else {
                // 如果本地配置不存在，尝试加载模板文件
                File templateFile = new File(TEMPLATE_FILE);
                if (templateFile.exists()) {
                    is = new FileInputStream(templateFile);
                    System.out.println("[SimpleConfig] 警告: 未找到 " + CONFIG_FILE);
                    System.out.println("[SimpleConfig] 已加载模板配置: " + TEMPLATE_FILE);
                    System.out.println("[SimpleConfig] 请复制 " + TEMPLATE_FILE + " 为 " + CONFIG_FILE + " 并修改为您的实际配置");
                } else {
                    System.err.println("[SimpleConfig] 错误: 未找到配置文件");
                    System.err.println("[SimpleConfig] 请确保存在以下文件之一:");
                    System.err.println("  - " + CONFIG_FILE);
                    System.err.println("  - " + TEMPLATE_FILE);
                }
            }
            
            // 如果找到了配置文件，加载它
            if (is != null) {
                props.load(is);
                
                // 验证关键配置
                validateCriticalConfig();
            }
            
        } catch (IOException e) {
            System.err.println("[SimpleConfig] 加载配置文件时出错: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // 忽略关闭异常
                }
            }
        }
        
        loaded = true;
    }
    
    /**
     * 验证关键配置
     */
    private static void validateCriticalConfig() {
        String[] criticalKeys = {"db.url", "db.username", "db.password"};
        boolean hasError = false;
        
        for (String key : criticalKeys) {
            String value = props.getProperty(key);
            if (value == null || value.trim().isEmpty() || value.contains("your_")) {
                System.err.println("[SimpleConfig] 警告: 配置项 " + key + " 未设置或使用了默认值");
                hasError = true;
            }
        }
        
        if (hasError) {
            System.err.println("[SimpleConfig] 请编辑 config/config.local.properties 文件，设置正确的数据库配置");
        }
    }
    
    /**
     * 重新加载配置
     * 可用于在运行时刷新配置
     */
    public static void reload() {
        System.out.println("[SimpleConfig] 重新加载配置...");
        loaded = false;
        props.clear();
        loadConfig();
    }
    
    /**
     * 打印当前加载的所有配置（用于调试）
     * 注意：会隐藏密码等敏感信息
     */
    public static void printAllConfig() {
        ensureLoaded();
        System.out.println("\n========== 当前配置列表 ==========");
        props.forEach((key, value) -> {
            String displayValue;
            String keyStr = key.toString().toLowerCase();
            
            // 隐藏敏感信息
            if (keyStr.contains("password") || keyStr.contains("secret") || keyStr.contains("key") && !keyStr.contains("public")) {
                displayValue = "******";
            } else {
                displayValue = value.toString();
            }
            
            System.out.println(String.format("  %-30s = %s", key, displayValue));
        });
        System.out.println("==================================\n");
    }
}