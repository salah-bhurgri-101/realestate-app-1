package com.realestate.auth_service.service;

import com.realestate.auth_service.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTokenExpiryMs;
    private final String issuer;

    public JwtService(
            @Value("${auth.jwt.secret}") String secret,
            @Value("${auth.jwt.access-token-expiry-ms:900000}") long accessTokenExpiryMs,
            @Value("${auth.jwt.issuer:auth-service}") String issuer) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("auth.jwt.secret must contain at least 32 UTF-8 bytes.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.issuer = issuer;
    }

    // ── generate ──────────────────────────────────────────────────────────────

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // jti — unique per token, used for blacklisting
                .issuer(issuer)
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("tokenVersion", user.getTokenVersion())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpiryMs)))
                .signWith(secretKey)
                .compact();
    }

    // ── validate ──────────────────────────────────────────────────────────────

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    // ── extract ───────────────────────────────────────────────────────────────

    public Claims extractAllClaims(String token) {
        return parseClaims(token);
    }

    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractJti(String token) {
        return parseClaims(token).getId();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public Integer extractTokenVersion(String token) {
        return parseClaims(token).get("tokenVersion", Integer.class);
    }

    public long extractExpiryMs(String token) {
        return parseClaims(token).getExpiration().getTime();
    }

    public long getRemainingTtlMs(String token) {
        long expiry = extractExpiryMs(token);
        return Math.max(0, expiry - System.currentTimeMillis());
    }

    // ── internal ──────────────────────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
