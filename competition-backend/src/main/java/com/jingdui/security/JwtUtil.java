package com.jingdui.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类 —— 生成、解析、校验 Token
 *
 * 密钥和过期时间从 application.yml 读取（jwt.secret / jwt.expire-days）。
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-days:7}")
    private long expireDays;

    private SecretKey key;
    private long expireMs;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMs = expireDays * 24 * 60 * 60 * 1000L;
    }

    /**
     * 生成 Token
     */
    public String generateToken(Long userId, String userName) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userName", userName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireMs))
                .signWith(key)
                .compact();
    }

    /**
     * 解析 Token 中的 Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中提取 userId
     */
    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    /**
     * 从 Token 中提取 userName
     */
    public String getUserName(String token) {
        return parseToken(token).get("userName", String.class);
    }

    /**
     * 校验 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}