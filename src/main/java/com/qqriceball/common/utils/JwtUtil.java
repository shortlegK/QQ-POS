package com.qqriceball.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    // 產生 Token
    public static String generateToken(String secretKey, Integer userId, String username, long ttlMillis) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                // 使用者 ID
                .setSubject(String.valueOf(userId))
                // 使用者名稱
                .claim("username", username)
                // 簽發時間
                .setIssuedAt(now)
                // 過期時間
                .setExpiration(new Date(nowMillis + ttlMillis))
                // HS256 by default
                .signWith(key)
                .compact();
    }

    // 解析 Token，回傳 Claims
    public static Claims parseToken(String secretKey, String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
