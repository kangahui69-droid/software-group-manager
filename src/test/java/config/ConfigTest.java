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
}
