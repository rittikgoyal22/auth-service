package com.etd.auth_service.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JWTUtil {

    private static final Logger logger = LoggerFactory.getLogger(JWTUtil.class);
    private static final long TOKEN_VALIDITY_MS = 1000L * 60 * 60; // 1 hour

    private final SecretKey key;

    public JWTUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY_MS))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Safe on expired tokens — used to persist expiry in token_blacklist on logout.
    public LocalDateTime extractExpiration(String token) {
        try {
            Date expiration = Jwts.parser()
                    .verifyWith(key).build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            return expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getExpiration()
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

}
