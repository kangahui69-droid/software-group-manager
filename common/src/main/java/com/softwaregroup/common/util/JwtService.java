package com.softwaregroup.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/** JWT signing and verification using a deployment-specific secret. */
@Component
@ConditionalOnProperty(name = "JWT_SECRET")
public class JwtService {
    private static final String LEGACY_SECRET = "software-group-jwt-secret-key-2024-very-long-and-secure";
    private static final long DEFAULT_EXPIRATION_MS = 24 * 60 * 60 * 1000L;

    private final SecretKey key;
    private final String keyId;
    private final String issuer;
    private final String audience;

    public JwtService(@Value("${JWT_SECRET}") String secret,
                      @Value("${JWT_KEY_ID:v1}") String keyId,
                      @Value("${JWT_ISSUER:software-group}") String issuer,
                      @Value("${JWT_AUDIENCE:software-group-api}") String audience) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32
                || LEGACY_SECRET.equals(secret)) {
            throw new IllegalArgumentException("JWT_SECRET must be a new secret of at least 32 UTF-8 bytes");
        }
        if (keyId == null || keyId.isBlank() || issuer == null || issuer.isBlank()
                || audience == null || audience.isBlank()) {
            throw new IllegalArgumentException("JWT key id, issuer and audience are required");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.keyId = keyId;
        this.issuer = issuer;
        this.audience = audience;
    }

    public String generateToken(int userId, String username, String role) {
        return generateToken(userId, username, role, DEFAULT_EXPIRATION_MS);
    }

    public String generateToken(int userId, String username, String role, long expirationMs) {
        return generateToken(userId, username, role, expirationMs, null);
    }

    public String generateToken(int userId, String username, String role, Map<String, Object> extraClaims) {
        return generateToken(userId, username, role, DEFAULT_EXPIRATION_MS, extraClaims);
    }

    private String generateToken(int userId, String username, String role, long expirationMs,
                                 Map<String, Object> extraClaims) {
        if (userId <= 0 || username == null || username.isBlank() || !validRole(role)) {
            throw new IllegalArgumentException("Invalid JWT identity");
        }
        Date now = new Date();
        var builder = Jwts.builder().header().keyId(keyId).and();
        if (extraClaims != null) {
            extraClaims.forEach((name, value) -> {
                if ("sub".equals(name) || "iss".equals(name) || "aud".equals(name)
                        || "iat".equals(name) || "exp".equals(name) || "role".equals(name)
                        || "username".equals(name)) {
                    throw new IllegalArgumentException("Reserved JWT claim: " + name);
                }
                builder.claim(name, value);
            });
        }
        return builder.subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims parseToken(String token) {
        var signed = Jwts.parser().verifyWith(key)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build().parseSignedClaims(token);
        if (!keyId.equals(signed.getHeader().getKeyId())) {
            throw new JwtException("Unknown JWT key id");
        }
        Claims claims = signed.getPayload();
        if (claims.getSubject() == null || !claims.getSubject().matches("[1-9][0-9]*")
                || claims.get("username", String.class) == null
                || claims.get("username", String.class).isBlank()
                || !validRole(claims.get("role", String.class))) {
            throw new JwtException("Invalid JWT identity claims");
        }
        return claims;
    }

    public int getUserId(String token) {
        return Integer.parseInt(parseToken(token).getSubject());
    }

    public String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public Date getExpiration(String token) {
        return parseToken(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        try {
            return getExpiration(token).before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }

    public String extractTokenFromHeader(String authHeader) {
        return authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7) : null;
    }

    private boolean validRole(String role) {
        return "MEMBER".equals(role) || "ADMIN".equals(role) || "TEACHER".equals(role);
    }
}
