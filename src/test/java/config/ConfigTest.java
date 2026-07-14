package config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.FastTest;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Config 配置类单元测试
 *
 * 测试覆盖（RULES.md 规则5）：
 * - 正常路径：正整数、零、负数、Long类型
 * - 边界情况：key不存在、空字符串、空白字符、MAX/MIN值、溢出、前导零
 * - 异常场景：非数字字符串、小数、混合字符、null key
 * - 已有方法回归测试
 */
@DisplayName("Config 配置类测试")
class ConfigTest {

    private static Properties originalProps;

    @BeforeAll
    static void saveOriginalConfig() throws Exception {
        Field propsField = Config.class.getDeclaredField("properties");
        propsField.setAccessible(true);
        originalProps = new Properties();
        originalProps.putAll((Properties) propsField.get(null));
    }

    @AfterEach
    void restoreConfig() throws Exception {
        Field propsField = Config.class.getDeclaredField("properties");
        propsField.setAccessible(true);
        Properties current = (Properties) propsField.get(null);
        current.clear();
        current.putAll(originalProps);
    }

    private void setTestProperty(String key, String value) throws Exception {
        Field propsField = Config.class.getDeclaredField("properties");
        propsField.setAccessible(true);
        Properties props = (Properties) propsField.get(null);
        if (value != null) {
            props.setProperty(key, value);
        } else {
            props.remove(key);
        }
    }

    // ==================== getIntProperty 正常路径测试 ====================

    @FastTest
    @DisplayName("getIntProperty: 存在的正整数配置应返回解析后的int值")
    void should_return_parsed_int_when_key_exists_with_positive_value() throws Exception {
        setTestProperty("test.int.positive", "42");
        assertThat(Config.getIntProperty("test.int.positive", 0)).isEqualTo(42);
    }

    @FastTest
    @DisplayName("getIntProperty: 值为0应返回0")
    void should_return_zero_when_value_is_zero() throws Exception {
        setTestProperty("test.int.zero", "0");
        assertThat(Config.getIntProperty("test.int.zero", -1)).isEqualTo(0);
    }

    @FastTest
    @DisplayName("getIntProperty: 负整数应正确返回")
    void should_return_negative_int_when_value_is_negative() throws Exception {
        setTestProperty("test.int.negative", "-10");
        assertThat(Config.getIntProperty("test.int.negative", 0)).isEqualTo(-10);
    }

    // ==================== getIntProperty 边界情况测试 ====================

    @FastTest
    @DisplayName("getIntProperty: key不存在时应返回默认值")
    void should_return_default_when_key_not_found() {
        assertThat(Config.getIntProperty("test.nonexistent.key.12345", 99)).isEqualTo(99);
    }

    @FastTest
    @DisplayName("getIntProperty: key不存在且默认值为0时应返回0")
    void should_return_zero_default_when_key_not_found_and_default_is_zero() {
        assertThat(Config.getIntProperty("test.nonexistent.key.zero", 0)).isEqualTo(0);
    }

    @FastTest
    @DisplayName("getIntProperty: key不存在且默认值为负数时应返回该负数")
    void should_return_negative_default_when_key_not_found_and_default_is_negative() {
        assertThat(Config.getIntProperty("test.nonexistent.key.neg", -1)).isEqualTo(-1);
    }

    @FastTest
    @DisplayName("getIntProperty: 值为空字符串应返回默认值")
    void should_return_default_when_value_is_empty_string() throws Exception {
        setTestProperty("test.int.empty", "");
        assertThat(Config.getIntProperty("test.int.empty", 77)).isEqualTo(77);
    }

    @FastTest
    @DisplayName("getIntProperty: 值为纯空白字符应返回默认值（trim后为空）")
    void should_return_default_when_value_is_whitespace_only() throws Exception {
        setTestProperty("test.int.whitespace", "   ");
        assertThat(Config.getIntProperty("test.int.whitespace", 55)).isEqualTo(55);
    }

    @FastTest
    @DisplayName("getIntProperty: 值带前后空格应正确解析")
    void should_parse_value_with_surrounding_spaces() throws Exception {
        setTestProperty("test.int.spaces", "  123  ");
        assertThat(Config.getIntProperty("test.int.spaces", 0)).isEqualTo(123);
    }

    @FastTest
    @DisplayName("getIntProperty: Integer.MAX_VALUE应正确返回")
    void should_return_max_value_when_value_is_integer_max() throws Exception {
        setTestProperty("test.int.max", "2147483647");
        assertThat(Config.getIntProperty("test.int.max", 0)).isEqualTo(Integer.MAX_VALUE);
    }

    @FastTest
    @DisplayName("getIntProperty: Integer.MIN_VALUE应正确返回")
    void should_return_min_value_when_value_is_integer_min() throws Exception {
        setTestProperty("test.int.min", "-2147483648");
        assertThat(Config.getIntProperty("test.int.min", 0)).isEqualTo(Integer.MIN_VALUE);
    }

    @FastTest
    @DisplayName("getIntProperty: 超过MAX_VALUE应返回默认值（溢出）")
    void should_return_default_when_value_exceeds_max_int() throws Exception {
        setTestProperty("test.int.overflow", "2147483648");
        assertThat(Config.getIntProperty("test.int.overflow", -1)).isEqualTo(-1);
    }

    @FastTest
    @DisplayName("getIntProperty: 小于MIN_VALUE应返回默认值（下溢）")
    void should_return_default_when_value_below_min_int() throws Exception {
        setTestProperty("test.int.underflow", "-2147483649");
        assertThat(Config.getIntProperty("test.int.underflow", -1)).isEqualTo(-1);
    }

    @FastTest
    @DisplayName("getIntProperty: 前导零应正确解析")
    void should_parse_value_with_leading_zeros() throws Exception {
        setTestProperty("test.int.leadingzero", "007");
        assertThat(Config.getIntProperty("test.int.leadingzero", 0)).isEqualTo(7);
    }

    @FastTest
    @DisplayName("getIntProperty: 大正整数默认值应正确返回")
    void should_return_large_positive_default() {
        assertThat(Config.getIntProperty("test.nonexistent.large", 99999)).isEqualTo(99999);
    }

    // ==================== getIntProperty 异常场景测试 ====================

    @FastTest
    @DisplayName("getIntProperty: 非数字字符串应返回默认值")
    void should_return_default_when_value_is_not_numeric() throws Exception {
        setTestProperty("test.int.abc", "abc");
        assertThat(Config.getIntProperty("test.int.abc", 42)).isEqualTo(42);
    }

    @FastTest
    @DisplayName("getIntProperty: 小数应返回默认值")
    void should_return_default_when_value_is_decimal() throws Exception {
        setTestProperty("test.int.decimal", "12.34");
        assertThat(Config.getIntProperty("test.int.decimal", 0)).isEqualTo(0);
    }

    @FastTest
    @DisplayName("getIntProperty: 字母数字混合应返回默认值")
    void should_return_default_when_value_is_alphanumeric() throws Exception {
        setTestProperty("test.int.mixed", "123abc");
        assertThat(Config.getIntProperty("test.int.mixed", 0)).isEqualTo(0);
    }

    @FastTest
    @DisplayName("getIntProperty: 含特殊字符应返回默认值")
    void should_return_default_when_value_contains_special_chars() throws Exception {
        setTestProperty("test.int.special", "12@34");
        assertThat(Config.getIntProperty("test.int.special", 0)).isEqualTo(0);
    }

    @FastTest
    @DisplayName("getIntProperty: null值应返回默认值")
    void should_return_default_when_value_is_null() throws Exception {
        setTestProperty("test.int.null", null);
        assertThat(Config.getIntProperty("test.int.null", 88)).isEqualTo(88);
    }

    // ==================== getLongProperty 测试 ====================

    @FastTest
    @DisplayName("getLongProperty: 存在的正整数应正确返回")
    void should_return_parsed_long_when_key_exists() throws Exception {
        setTestProperty("test.long.positive", "9223372036854775807");
        assertThat(Config.getLongProperty("test.long.positive", 0L)).isEqualTo(9223372036854775807L);
    }

    @FastTest
    @DisplayName("getLongProperty: key不存在应返回默认值")
    void should_return_long_default_when_key_not_found() {
        assertThat(Config.getLongProperty("test.nonexistent.long", 12345L)).isEqualTo(12345L);
    }

    @FastTest
    @DisplayName("getLongProperty: 异常值应返回默认值")
    void should_return_long_default_when_value_is_invalid() throws Exception {
        setTestProperty("test.long.invalid", "abc");
        assertThat(Config.getLongProperty("test.long.invalid", 999L)).isEqualTo(999L);
    }

    // ==================== 已有方法回归测试 ====================

    @Test
    @DisplayName("getProperty(key, defaultValue) 应继续正常工作")
    void should_work_existing_get_property_with_default() {
        assertThat(Config.getProperty("des.key", "fallback")).isNotNull();
        assertThat(Config.getProperty("nonexistent.key", "fallback")).isEqualTo("fallback");
    }

    @Test
    @DisplayName("getProperty(key) 应继续正常工作")
    void should_work_existing_get_property_without_default() {
        assertThat(Config.getProperty("des.key")).isNotNull();
    }

    @Test
    @DisplayName("getSessionTimeout 应返回配置的整数值")
    void should_return_configured_session_timeout() throws Exception {
        setTestProperty("session.timeout", "60");
        assertThat(Config.getSessionTimeout()).isEqualTo(60);
    }

    @Test
    @DisplayName("getMaxFileSize 应返回配置的长整数")
    void should_return_configured_max_file_size() throws Exception {
        setTestProperty("upload.maxFileSize", "20971520");
        assertThat(Config.getMaxFileSize()).isEqualTo(20971520L);
    }

    @Test
    @DisplayName("reloadConfig 应清空测试属性并重新加载配置")
    void should_clear_test_properties_on_reload() throws Exception {
        // 设置测试属性后reloadConfig应该清空并重新加载
        setTestProperty("test.reload.key", "before");
        assertThat(Config.getProperty("test.reload.key")).isEqualTo("before");
        Config.reloadConfig();
        // reloadConfig后，测试属性被清空，从配置文件重新加载
        // 由于test.reload.key不在配置文件中，应该返回null
        assertThat(Config.getProperty("test.reload.key")).isNull();
    }

    // ==================== getDbDriver 数据库驱动测试 ====================

    @FastTest
    @DisplayName("getDbDriver: 配置了db.driver时应返回配置值")
    void should_return_configured_driver_when_db_driver_is_set() throws Exception {
        setTestProperty("db.driver", "org.h2.Driver");
        assertThat(Config.getDbDriver()).isEqualTo("org.h2.Driver");
    }

    @FastTest
    @DisplayName("getDbDriver: 未配置db.driver时应返回MySQL驱动默认值")
    void should_return_mysql_driver_default_when_db_driver_not_set() throws Exception {
        setTestProperty("db.driver", null);
        assertThat(Config.getDbDriver()).isEqualTo("com.mysql.cj.jdbc.Driver");
    }

    @FastTest
    @DisplayName("getDbDriver: db.driver为空字符串时应返回默认值")
    void should_return_default_driver_when_db_driver_is_empty() throws Exception {
        setTestProperty("db.driver", "");
        assertThat(Config.getDbDriver()).isEqualTo("com.mysql.cj.jdbc.Driver");
    }

    @FastTest
    @DisplayName("getDbDriver: db.driver为纯空白字符时应返回默认值")
    void should_return_default_driver_when_db_driver_is_whitespace() throws Exception {
        setTestProperty("db.driver", "   ");
        assertThat(Config.getDbDriver()).isEqualTo("com.mysql.cj.jdbc.Driver");
    }

    @FastTest
    @DisplayName("getDbDriver: db.driver值前后有空格应trim后返回")
    void should_trim_whitespace_from_driver_value() throws Exception {
        setTestProperty("db.driver", "  com.mysql.cj.jdbc.Driver  ");
        assertThat(Config.getDbDriver()).isEqualTo("com.mysql.cj.jdbc.Driver");
    }

    // ==================== getDbUrl 数据库URL测试 ====================

    @FastTest
    @DisplayName("getDbUrl: 配置了db.url时应返回配置值")
    void should_return_configured_url_when_db_url_is_set() throws Exception {
        setTestProperty("db.url", "jdbc:h2:mem:testdb");
        assertThat(Config.getDbUrl()).isEqualTo("jdbc:h2:mem:testdb");
    }

    @FastTest
    @DisplayName("getDbUrl: 未配置db.url时应返回MySQL默认URL")
    void should_return_default_mysql_url_when_db_url_not_set() throws Exception {
        setTestProperty("db.url", null);
        assertThat(Config.getDbUrl()).startsWith("jdbc:mysql://localhost:3306/software_group");
    }

    @FastTest
    @DisplayName("getDbUrl: db.url为空字符串时应返回默认值")
    void should_return_default_url_when_db_url_is_empty() throws Exception {
        setTestProperty("db.url", "");
        assertThat(Config.getDbUrl()).startsWith("jdbc:mysql://");
    }

    @FastTest
    @DisplayName("getDbUrl: db.url前后有空格应trim后返回")
    void should_trim_whitespace_from_url_value() throws Exception {
        setTestProperty("db.url", "  jdbc:mysql://localhost:3306/test  ");
        assertThat(Config.getDbUrl()).isEqualTo("jdbc:mysql://localhost:3306/test");
    }

    // ==================== getDbUsername 数据库用户名测试 ====================

    @FastTest
    @DisplayName("getDbUsername: 配置了db.username时应返回配置值")
    void should_return_configured_username_when_set() throws Exception {
        setTestProperty("db.username", "myuser");
        assertThat(Config.getDbUsername()).isEqualTo("myuser");
    }

    @FastTest
    @DisplayName("getDbUsername: 未配置db.username时应返回root默认值")
    void should_return_root_default_when_username_not_set() throws Exception {
        setTestProperty("db.username", null);
        assertThat(Config.getDbUsername()).isEqualTo("root");
    }

    @FastTest
    @DisplayName("getDbUsername: db.username为空字符串时应返回默认值")
    void should_return_default_username_when_empty() throws Exception {
        setTestProperty("db.username", "");
        assertThat(Config.getDbUsername()).isEqualTo("root");
    }

    @FastTest
    @DisplayName("getDbUsername: db.username为空白字符时应返回默认值")
    void should_return_default_username_when_whitespace() throws Exception {
        setTestProperty("db.username", "  ");
        assertThat(Config.getDbUsername()).isEqualTo("root");
    }

    // ==================== getDbPassword 数据库密码测试 ====================

    @FastTest
    @DisplayName("getDbPassword: 配置了db.password时应返回配置值")
    void should_return_configured_password_when_set() throws Exception {
        setTestProperty("db.password", "secret123");
        assertThat(Config.getDbPassword()).isEqualTo("secret123");
    }

    @FastTest
    @DisplayName("getDbPassword: 未配置db.password时应返回空字符串默认值")
    void should_return_empty_default_when_password_not_set() throws Exception {
        setTestProperty("db.password", null);
        assertThat(Config.getDbPassword()).isEqualTo("");
    }

    @FastTest
    @DisplayName("getDbPassword: db.password为空字符串时应返回空字符串")
    void should_return_empty_string_when_password_is_empty() throws Exception {
        setTestProperty("db.password", "");
        assertThat(Config.getDbPassword()).isEqualTo("");
    }

    // ==================== getHikariMaximumPoolSize 最大连接数测试 ====================

    @FastTest
    @DisplayName("getHikariMaximumPoolSize: 配置了有效值时应返回解析后的int值")
    void should_return_configured_max_pool_size_when_set() throws Exception {
        setTestProperty("hikaricp.maximumPoolSize", "30");
        assertThat(Config.getHikariMaximumPoolSize()).isEqualTo(30);
    }

    @FastTest
    @DisplayName("getHikariMaximumPoolSize: 未配置时应返回默认值20")
    void should_return_default_20_when_max_pool_size_not_set() throws Exception {
        setTestProperty("hikaricp.maximumPoolSize", null);
        assertThat(Config.getHikariMaximumPoolSize()).isEqualTo(20);
    }

    @FastTest
    @DisplayName("getHikariMaximumPoolSize: 值为空字符串应返回默认值")
    void should_return_default_when_max_pool_size_is_empty() throws Exception {
        setTestProperty("hikaricp.maximumPoolSize", "");
        assertThat(Config.getHikariMaximumPoolSize()).isEqualTo(20);
    }

    @FastTest
    @DisplayName("getHikariMaximumPoolSize: 值为非数字字符串应返回默认值")
    void should_return_default_when_max_pool_size_is_non_numeric() throws Exception {
        setTestProperty("hikaricp.maximumPoolSize", "abc");
        assertThat(Config.getHikariMaximumPoolSize()).isEqualTo(20);
    }

    @FastTest
    @DisplayName("getHikariMaximumPoolSize: 值为小数应返回默认值")
    void should_return_default_when_max_pool_size_is_decimal() throws Exception {
        setTestProperty("hikaricp.maximumPoolSize", "10.5");
        assertThat(Config.getHikariMaximumPoolSize()).isEqualTo(20);
    }

    @FastTest
    @DisplayName("getHikariMaximumPoolSize: 值超过int范围应返回默认值（溢出）")
    void should_return_default_when_max_pool_size_overflows() throws Exception {
        setTestProperty("hikaricp.maximumPoolSize", "2147483648");
        assertThat(Config.getHikariMaximumPoolSize()).isEqualTo(20);
    }

    @FastTest
    @DisplayName("getHikariMaximumPoolSize: 值前后有空格应trim后解析")
    void should_trim_and_parse_max_pool_size() throws Exception {
        setTestProperty("hikaricp.maximumPoolSize", "  15  ");
        assertThat(Config.getHikariMaximumPoolSize()).isEqualTo(15);
    }

    @FastTest
    @DisplayName("getHikariMaximumPoolSize: 值为1（最小有效值）应正确返回")
    void should_return_1_when_max_pool_size_is_minimum() throws Exception {
        setTestProperty("hikaricp.maximumPoolSize", "1");
        assertThat(Config.getHikariMaximumPoolSize()).isEqualTo(1);
    }

    // ==================== getHikariMinimumIdle 最小空闲连接测试 ====================

    @FastTest
    @DisplayName("getHikariMinimumIdle: 配置了有效值时应返回解析后的int值")
    void should_return_configured_min_idle_when_set() throws Exception {
        setTestProperty("hikaricp.minimumIdle", "10");
        assertThat(Config.getHikariMinimumIdle()).isEqualTo(10);
    }

    @FastTest
    @DisplayName("getHikariMinimumIdle: 未配置时应返回默认值5")
    void should_return_default_5_when_min_idle_not_set() throws Exception {
        setTestProperty("hikaricp.minimumIdle", null);
        assertThat(Config.getHikariMinimumIdle()).isEqualTo(5);
    }

    @FastTest
    @DisplayName("getHikariMinimumIdle: 值为空字符串应返回默认值")
    void should_return_default_when_min_idle_is_empty() throws Exception {
        setTestProperty("hikaricp.minimumIdle", "");
        assertThat(Config.getHikariMinimumIdle()).isEqualTo(5);
    }

    @FastTest
    @DisplayName("getHikariMinimumIdle: 值为非数字应返回默认值")
    void should_return_default_when_min_idle_is_non_numeric() throws Exception {
        setTestProperty("hikaricp.minimumIdle", "xyz");
        assertThat(Config.getHikariMinimumIdle()).isEqualTo(5);
    }

    @FastTest
    @DisplayName("getHikariMinimumIdle: 值带前后空格应trim后解析")
    void should_trim_and_parse_min_idle() throws Exception {
        setTestProperty("hikaricp.minimumIdle", "  3  ");
        assertThat(Config.getHikariMinimumIdle()).isEqualTo(3);
    }

    @FastTest
    @DisplayName("getHikariMinimumIdle: 值为0应返回0（Config不做业务校验）")
    void should_return_zero_when_min_idle_is_zero() throws Exception {
        setTestProperty("hikaricp.minimumIdle", "0");
        assertThat(Config.getHikariMinimumIdle()).isEqualTo(0);
    }

    // ==================== getHikariConnectionTimeout 连接超时测试 ====================

    @FastTest
    @DisplayName("getHikariConnectionTimeout: 配置了有效值时应返回解析后的long值")
    void should_return_configured_connection_timeout_when_set() throws Exception {
        setTestProperty("hikaricp.connectionTimeout", "60000");
        assertThat(Config.getHikariConnectionTimeout()).isEqualTo(60000L);
    }

    @FastTest
    @DisplayName("getHikariConnectionTimeout: 未配置时应返回默认值30000（30秒）")
    void should_return_default_30000_when_connection_timeout_not_set() throws Exception {
        setTestProperty("hikaricp.connectionTimeout", null);
        assertThat(Config.getHikariConnectionTimeout()).isEqualTo(30000L);
    }

    @FastTest
    @DisplayName("getHikariConnectionTimeout: 值为空字符串应返回默认值")
    void should_return_default_when_connection_timeout_is_empty() throws Exception {
        setTestProperty("hikaricp.connectionTimeout", "");
        assertThat(Config.getHikariConnectionTimeout()).isEqualTo(30000L);
    }

    @FastTest
    @DisplayName("getHikariConnectionTimeout: 值为非数字应返回默认值")
    void should_return_default_when_connection_timeout_is_non_numeric() throws Exception {
        setTestProperty("hikaricp.connectionTimeout", "timeout");
        assertThat(Config.getHikariConnectionTimeout()).isEqualTo(30000L);
    }

    @FastTest
    @DisplayName("getHikariConnectionTimeout: 值为小数应返回默认值")
    void should_return_default_when_connection_timeout_is_decimal() throws Exception {
        setTestProperty("hikaricp.connectionTimeout", "30000.5");
        assertThat(Config.getHikariConnectionTimeout()).isEqualTo(30000L);
    }

    @FastTest
    @DisplayName("getHikariConnectionTimeout: 值超过long范围应返回默认值")
    void should_return_default_when_connection_timeout_overflows() throws Exception {
        setTestProperty("hikaricp.connectionTimeout", "9223372036854775808");
        assertThat(Config.getHikariConnectionTimeout()).isEqualTo(30000L);
    }

    @FastTest
    @DisplayName("getHikariConnectionTimeout: 值带前后空格应trim后解析")
    void should_trim_and_parse_connection_timeout() throws Exception {
        setTestProperty("hikaricp.connectionTimeout", "  5000  ");
        assertThat(Config.getHikariConnectionTimeout()).isEqualTo(5000L);
    }

    // ==================== getHikariIdleTimeout 空闲超时测试 ====================

    @FastTest
    @DisplayName("getHikariIdleTimeout: 配置了有效值时应返回解析后的long值")
    void should_return_configured_idle_timeout_when_set() throws Exception {
        setTestProperty("hikaricp.idleTimeout", "300000");
        assertThat(Config.getHikariIdleTimeout()).isEqualTo(300000L);
    }

    @FastTest
    @DisplayName("getHikariIdleTimeout: 未配置时应返回默认值600000（10分钟）")
    void should_return_default_600000_when_idle_timeout_not_set() throws Exception {
        setTestProperty("hikaricp.idleTimeout", null);
        assertThat(Config.getHikariIdleTimeout()).isEqualTo(600000L);
    }

    @FastTest
    @DisplayName("getHikariIdleTimeout: 值为空字符串应返回默认值")
    void should_return_default_when_idle_timeout_is_empty() throws Exception {
        setTestProperty("hikaricp.idleTimeout", "");
        assertThat(Config.getHikariIdleTimeout()).isEqualTo(600000L);
    }

    @FastTest
    @DisplayName("getHikariIdleTimeout: 值为非数字应返回默认值")
    void should_return_default_when_idle_timeout_is_non_numeric() throws Exception {
        setTestProperty("hikaricp.idleTimeout", "never");
        assertThat(Config.getHikariIdleTimeout()).isEqualTo(600000L);
    }

    @FastTest
    @DisplayName("getHikariIdleTimeout: 值带前后空格应trim后解析")
    void should_trim_and_parse_idle_timeout() throws Exception {
        setTestProperty("hikaricp.idleTimeout", "  120000  ");
        assertThat(Config.getHikariIdleTimeout()).isEqualTo(120000L);
    }

    // ==================== getHikariMaxLifetime 连接最大生命周期测试 ====================

    @FastTest
    @DisplayName("getHikariMaxLifetime: 配置了有效值时应返回解析后的long值")
    void should_return_configured_max_lifetime_when_set() throws Exception {
        setTestProperty("hikaricp.maxLifetime", "3600000");
        assertThat(Config.getHikariMaxLifetime()).isEqualTo(3600000L);
    }

    @FastTest
    @DisplayName("getHikariMaxLifetime: 未配置时应返回默认值1800000（30分钟）")
    void should_return_default_1800000_when_max_lifetime_not_set() throws Exception {
        setTestProperty("hikaricp.maxLifetime", null);
        assertThat(Config.getHikariMaxLifetime()).isEqualTo(1800000L);
    }

    @FastTest
    @DisplayName("getHikariMaxLifetime: 值为空字符串应返回默认值")
    void should_return_default_when_max_lifetime_is_empty() throws Exception {
        setTestProperty("hikaricp.maxLifetime", "");
        assertThat(Config.getHikariMaxLifetime()).isEqualTo(1800000L);
    }

    @FastTest
    @DisplayName("getHikariMaxLifetime: 值为非数字应返回默认值")
    void should_return_default_when_max_lifetime_is_non_numeric() throws Exception {
        setTestProperty("hikaricp.maxLifetime", "forever");
        assertThat(Config.getHikariMaxLifetime()).isEqualTo(1800000L);
    }

    @FastTest
    @DisplayName("getHikariMaxLifetime: 值带前后空格应trim后解析")
    void should_trim_and_parse_max_lifetime() throws Exception {
        setTestProperty("hikaricp.maxLifetime", "  900000  ");
        assertThat(Config.getHikariMaxLifetime()).isEqualTo(900000L);
    }

    // ==================== getHikariConnectionTestQuery 连接测试SQL测试 ====================

    @FastTest
    @DisplayName("getHikariConnectionTestQuery: 配置了有效值时应返回配置值")
    void should_return_configured_test_query_when_set() throws Exception {
        setTestProperty("hikaricp.connectionTestQuery", "SELECT 1 FROM DUAL");
        assertThat(Config.getHikariConnectionTestQuery()).isEqualTo("SELECT 1 FROM DUAL");
    }

    @FastTest
    @DisplayName("getHikariConnectionTestQuery: 未配置时应返回默认值SELECT 1")
    void should_return_default_select_1_when_test_query_not_set() throws Exception {
        setTestProperty("hikaricp.connectionTestQuery", null);
        assertThat(Config.getHikariConnectionTestQuery()).isEqualTo("SELECT 1");
    }

    @FastTest
    @DisplayName("getHikariConnectionTestQuery: 值为空字符串应返回默认值")
    void should_return_default_when_test_query_is_empty() throws Exception {
        setTestProperty("hikaricp.connectionTestQuery", "");
        assertThat(Config.getHikariConnectionTestQuery()).isEqualTo("SELECT 1");
    }

    @FastTest
    @DisplayName("getHikariConnectionTestQuery: 值为纯空白字符应返回默认值")
    void should_return_default_when_test_query_is_whitespace() throws Exception {
        setTestProperty("hikaricp.connectionTestQuery", "   ");
        assertThat(Config.getHikariConnectionTestQuery()).isEqualTo("SELECT 1");
    }

    @FastTest
    @DisplayName("getHikariConnectionTestQuery: 值前后有空格应trim后返回")
    void should_trim_whitespace_from_test_query() throws Exception {
        setTestProperty("hikaricp.connectionTestQuery", "  SELECT 1  ");
        assertThat(Config.getHikariConnectionTestQuery()).isEqualTo("SELECT 1");
    }

    // ==================== getHikariValidationTimeout 验证超时测试 ====================

    @FastTest
    @DisplayName("getHikariValidationTimeout: 配置了有效值时应返回解析后的long值")
    void should_return_configured_validation_timeout_when_set() throws Exception {
        setTestProperty("hikaricp.validationTimeout", "10000");
        assertThat(Config.getHikariValidationTimeout()).isEqualTo(10000L);
    }

    @FastTest
    @DisplayName("getHikariValidationTimeout: 未配置时应返回默认值5000（5秒）")
    void should_return_default_5000_when_validation_timeout_not_set() throws Exception {
        setTestProperty("hikaricp.validationTimeout", null);
        assertThat(Config.getHikariValidationTimeout()).isEqualTo(5000L);
    }

    @FastTest
    @DisplayName("getHikariValidationTimeout: 值为空字符串应返回默认值")
    void should_return_default_when_validation_timeout_is_empty() throws Exception {
        setTestProperty("hikaricp.validationTimeout", "");
        assertThat(Config.getHikariValidationTimeout()).isEqualTo(5000L);
    }

    @FastTest
    @DisplayName("getHikariValidationTimeout: 值为非数字应返回默认值")
    void should_return_default_when_validation_timeout_is_non_numeric() throws Exception {
        setTestProperty("hikaricp.validationTimeout", "fast");
        assertThat(Config.getHikariValidationTimeout()).isEqualTo(5000L);
    }

    @FastTest
    @DisplayName("getHikariValidationTimeout: 值带前后空格应trim后解析")
    void should_trim_and_parse_validation_timeout() throws Exception {
        setTestProperty("hikaricp.validationTimeout", "  3000  ");
        assertThat(Config.getHikariValidationTimeout()).isEqualTo(3000L);
    }

    // ==================== 配置文件完整性验证 ====================

    @Test
    @DisplayName("config.properties 应包含db.driver配置键（模板默认值验证）")
    void should_have_db_driver_in_config_template() {
        // 验证config.properties模板中包含db.driver键（加载优先级：local > default）
        // 测试环境下config.local.properties已设置db.driver=org.h2.Driver
        // 这里验证getProperty能读到db.driver（local配置存在）
        String driver = Config.getProperty("db.driver");
        assertThat(driver).isNotNull();
    }

    @Test
    @DisplayName("config.properties 应包含所有hikaricp配置键")
    void should_have_all_hikaricp_keys_in_config() {
        String[] hikariKeys = {
            "hikaricp.maximumPoolSize",
            "hikaricp.minimumIdle",
            "hikaricp.connectionTimeout",
            "hikaricp.idleTimeout",
            "hikaricp.maxLifetime",
            "hikaricp.connectionTestQuery",
            "hikaricp.validationTimeout"
        };
        for (String key : hikariKeys) {
            // 这些key在config.properties模板中必须存在
            // 测试环境config.local.properties未设置hikaricp.*，应从config.properties读取
            String value = Config.getProperty(key);
            assertThat(value)
                .as("config.properties应包含配置键: " + key)
                .isNotNull();
        }
    }

    @Test
    @DisplayName("reloadConfig后所有HikariCP getter仍应返回正确值")
    void should_return_correct_hikari_values_after_reload() throws Exception {
        setTestProperty("hikaricp.maximumPoolSize", "50");
        assertThat(Config.getHikariMaximumPoolSize()).isEqualTo(50);
        Config.reloadConfig();
        // reload后清空测试属性，从config.properties重新加载，模板中hikaricp.maximumPoolSize=10
        assertThat(Config.getHikariMaximumPoolSize()).isEqualTo(10);
    }
}
