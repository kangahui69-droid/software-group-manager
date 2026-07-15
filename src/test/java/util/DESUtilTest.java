package util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import support.FastTest;
import support.H2Database;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DES加密工具测试")
class DESUtilTest {

    @BeforeAll
    static void init() {
        H2Database.initSchema();
    }

    @FastTest
    @DisplayName("加密后再解密应能还原原文")
    void should_decrypt_back_to_original() {
        String original = "admin123";
        String encrypted = DESUtil.encrypt(original);
        assertThat(encrypted).isNotNull();
        String decrypted = DESUtil.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(original);
    }

    @FastTest
    @DisplayName("相同明文每次加密结果应一致")
    void should_produce_same_result() {
        String a = DESUtil.encrypt("hello");
        String b = DESUtil.encrypt("hello");
        assertThat(a).isEqualTo(b);
    }

    @FastTest
    @DisplayName("不同明文加密结果不同")
    void should_produce_different_result() {
        String a = DESUtil.encrypt("admin123");
        String b = DESUtil.encrypt("member123");
        assertThat(a).isNotEqualTo(b);
    }

    @FastTest
    @DisplayName("null解密应返回null")
    void should_handle_null() {
        assertThat(DESUtil.decrypt(null)).isNull();
    }
}
