package com.marchive.marchive_backend.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private final SecretKey secretKey;

    private static final long ACCESS_TOKEN_VALIDITY_MS = 1000L * 60 * 60; // 1hour
    private static final long REFRESH_TOKEN_VALIDITY_MS = 1000L * 60 * 60 * 24 * 14; // 14days

    public JwtProvider(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String createAccessToken(Long userId) {
        return createToken(userId, ACCESS_TOKEN_VALIDITY_MS);
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, REFRESH_TOKEN_VALIDITY_MS);
    }

    public long getRefreshTokenValidMs() {
        return REFRESH_TOKEN_VALIDITY_MS;
    }

    private String createToken(Long userId, long validMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public Long validateAndGetUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("토큰이 만료되었습니다.");
        } catch (JwtException e) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
    }
}
