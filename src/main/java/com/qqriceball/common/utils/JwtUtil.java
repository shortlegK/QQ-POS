package com.qqriceball.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {


    public static String createJwt(String secretKey,long ttlMillis, Map<String, Object> claims) {
        SecretKey key = getSecretKey(secretKey);
         return Jwts.builder()
                .claims(claims) // Payload
                .expiration(new Date(System.currentTimeMillis() + ttlMillis)) //設定時效
                .signWith(key)
                .compact();
    }

    public static Claims parseJwt(String secretKey, String jwt) {
        SecretKey key = getSecretKey(secretKey);

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    private static SecretKey getSecretKey(String secretKey) {
        return io.jsonwebtoken.security.Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

}
