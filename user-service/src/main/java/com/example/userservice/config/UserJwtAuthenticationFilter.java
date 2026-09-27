package com.example.userservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class UserJwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey signingKey;
    private final String issuer;

    public UserJwtAuthenticationFilter(
            @Value("${auth.jwt.secret}") String secret, @Value("${auth.jwt.issuer}") String issuer) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("auth.jwt.secret must contain at least 32 UTF-8 bytes.");
        }
        signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(header.substring(7))
                    .getPayload();
            UUID userId = UUID.fromString(claims.getSubject());
            String role = claims.get("role", String.class);

            if (!pathBelongsToUser(request.getRequestURI(), userId)) {
                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN, "User path does not match the authenticated user.");
                return;
            }

            List<GrantedAuthority> authorities = role == null ? List.of() : List.of(new SimpleGrantedAuthority(role));
            SecurityContextHolder.getContext()
                    .setAuthentication(new UsernamePasswordAuthenticationToken(userId, null, authorities));
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Rejected invalid user-service JWT: {}", ex.getMessage());
        }

        chain.doFilter(request, response);
    }

    private boolean pathBelongsToUser(String path, UUID userId) {
        String prefix = "/api/v1/users/";
        if (!path.startsWith(prefix)) {
            return true;
        }
        int end = path.indexOf('/', prefix.length());
        String pathUserId = end < 0 ? path.substring(prefix.length()) : path.substring(prefix.length(), end);
        try {
            return userId.equals(UUID.fromString(pathUserId));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
