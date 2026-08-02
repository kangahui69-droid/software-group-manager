package com.softwaregroup.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 *
 * 用于微服务间认证：
 * - 生成 JWT Token
 * - 验证 JWT Token
 * - 解析 JWT Claims
 */
public class JwtUtil {

    /**
     * 默认密钥（生产环境应从配置中心或环境变量获取）
     * 长度要求：至少 256 位（32 字节）用于 HS256
     */
    private static final String DEFAULT_SECRET = "software-group-jwt-secret-key-2024-very-long-and-secure";

    /**
     * 默认过期时间：24小时
     */
    private static final long DEFAULT_EXPIRATION_MS = 24 * 60 * 60 * 1000L;

    /**
     * 默认签发者
     */
    private static final String DEFAULT_ISSUER = "software-group";

    /**
     * 生成 JWT Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色（MEMBER/ADMIN/GUEST）
     * @return JWT Token 字符串
     */
    public static String generateToken(int userId, String username, String role) {
        return generateToken(userId, username, role, DEFAULT_EXPIRATION_MS);
    }

    /**
     * 生成 JWT Token（指定过期时间）
     */
    public static String generateToken(int userId, String username, String role, long expirationMs) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuer(DEFAULT_ISSUER)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 生成带额外信息的 JWT Token
     */
    public static String generateToken(int userId, String username, String role, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + DEFAULT_EXPIRATION_MS);

        JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuer(DEFAULT_ISSUER)
                .issuedAt(now)
                .expiration(expiration);

        // 添加额外claims
        if (extraClaims != null) {
            extraClaims.forEach(builder::claim);
        }

        return builder.signWith(getSecretKey()).compact();
    }

    /**
     * 验证 JWT Token
     *
     * @param token JWT Token 字符串
     * @return true if valid, false otherwise
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 解析 JWT Token
     *
     * @param token JWT Token 字符串
     * @return Claims 对象
     * @throws JwtException 如果 token 无效
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中获取用户ID
     */
    public static int getUserId(String token) {
        Claims claims = parseToken(token);
        return Integer.parseInt(claims.getSubject());
    }

    /**
     * 从 Token 中获取用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从 Token 中获取角色
     */
    public static String getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 从 Token 中获取过期时间
     */
    public static Date getExpiration(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    /**
     * 判断 Token 是否过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpiration(token);
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * 获取 SecretKey
     */
    private static SecretKey getSecretKey() {
        // 确保密钥长度足够（至少 256 位）
        byte[] keyBytes = DEFAULT_SECRET.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // 填充密钥到 32 字节
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            keyBytes = paddedKey;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 从请求头中提取 Token
     *
     * @param authHeader Authorization 请求头的值
     * @return Token 字符串（不含 "Bearer " 前缀），如果不存在返回 null
     */
    public static String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
