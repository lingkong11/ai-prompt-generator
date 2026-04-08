package com.ai.prompt.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 *
 * <p>基于 HMAC-SHA 算法签发和验证 JWT Token，承载的 payload 仅包含 {@code sub}（用户名）。
 * Token 有效期通过 {@code jwt.expiration} 配置，默认 24 小时。</p>
 *
 * <p>安全说明：生产环境务必通过环境变量或配置中心注入高强度密钥，
 * 不要使用默认值 {@code ai-prompt-generator-secret-key-...}。</p>
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret:ai-prompt-generator-secret-key-very-long-string-for-security}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    /**
     * 根据 JWT secret 字符串构造 HMAC-SHA 密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 为指定用户名签发 JWT
     *
     * @param username 用户名，写入 Token 的 sub 字段
     * @return 紧凑格式的 JWT 字符串
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从已验证的 Token 中提取用户名（sub 字段）
     *
     * @throws JwtException Token 无效时抛出
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * 验证 Token 签名和有效期
     *
     * @return true — 签名正确且未过期；false — 签名错误、格式异常或已过期
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
