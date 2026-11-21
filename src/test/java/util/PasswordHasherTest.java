package util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHasherTest {
    @Test
    void usesSaltedArgon2idAndRejectsIncorrectPassword() {
        String first = PasswordHasher.hash("password123");
        String second = PasswordHasher.hash("password123");
        assertThat(first).startsWith("$argon2id$").hasSizeLessThan(256).isNotEqualTo(second);
        assertThat(PasswordHasher.matches("password123", first)).isTrue();
        assertThat(PasswordHasher.matches("wrong", first)).isFalse();
        assertThat(PasswordHasher.needsUpgrade(first)).isFalse();
    }

    @Test
    void acceptsLegacyDesOnlyWithCorrectPassword() {
        String legacy = DESUtil.encrypt("password123");
        assertThat(PasswordHasher.needsUpgrade(legacy)).isTrue();
        assertThat(PasswordHasher.matches("password123", legacy)).isTrue();
        assertThat(PasswordHasher.matches("wrong", legacy)).isFalse();
        assertThat(PasswordHasher.matches("password123", "plain text")).isFalse();
        assertThat(PasswordHasher.matches("password123", null)).isFalse();
    }
}
