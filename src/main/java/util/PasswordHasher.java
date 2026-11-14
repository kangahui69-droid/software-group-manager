package util;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/** One-way password storage with temporary verification of legacy DES records. */
public final class PasswordHasher {
    private static final Argon2PasswordEncoder ARGON2 =
            new Argon2PasswordEncoder(16, 32, 1, 19 * 1024, 2);

    private PasswordHasher() {
    }

    public static String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        return ARGON2.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (isArgon2(storedPassword)) {
            try {
                return ARGON2.matches(rawPassword, storedPassword);
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }
        if (!isLegacyDes(storedPassword)) {
            return false;
        }
        String legacyValue = DESUtil.encrypt(rawPassword);
        return legacyValue != null && MessageDigest.isEqual(
                legacyValue.getBytes(StandardCharsets.US_ASCII),
                storedPassword.getBytes(StandardCharsets.US_ASCII));
    }

    public static boolean needsUpgrade(String storedPassword) {
        return isLegacyDes(storedPassword);
    }

    private static boolean isArgon2(String value) {
        return value.startsWith("$argon2id$");
    }

    private static boolean isLegacyDes(String value) {
        if (value == null || isArgon2(value)) {
            return false;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(value);
            return decoded.length > 0 && decoded.length % 8 == 0;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
