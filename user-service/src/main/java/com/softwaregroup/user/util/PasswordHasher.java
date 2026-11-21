package com.softwaregroup.user.util;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/** One-way password storage with temporary verification of legacy DES records. */
@Component
public class PasswordHasher {
    private static final Argon2PasswordEncoder ARGON2 =
            new Argon2PasswordEncoder(16, 32, 1, 19 * 1024, 2);

    private final DESUtil legacyDes;

    public PasswordHasher(DESUtil legacyDes) {
        this.legacyDes = legacyDes;
    }

    public String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        return ARGON2.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (storedPassword.startsWith("$argon2id$")) {
            try {
                return ARGON2.matches(rawPassword, storedPassword);
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }
        if (!needsUpgrade(storedPassword)) {
            return false;
        }
        try {
            String legacyValue = legacyDes.encrypt(rawPassword);
            return legacyValue != null && MessageDigest.isEqual(legacyValue.getBytes(StandardCharsets.US_ASCII),
                    storedPassword.getBytes(StandardCharsets.US_ASCII));
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public boolean needsUpgrade(String storedPassword) {
        if (storedPassword == null || storedPassword.startsWith("$argon2id$")) {
            return false;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(storedPassword);
            return decoded.length > 0 && decoded.length % 8 == 0;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
