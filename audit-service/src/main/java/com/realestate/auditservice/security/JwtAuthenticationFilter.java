package com.realestate.auditservice.security;

import com.realestate.auditservice.service.impl.TokenBlacklistService;
import com.realestate.auditservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtService;
    private final TokenBlacklistService blacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("request" + request.getContextPath() + " request " + request);

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);
        try {

            // 1. Validate signature + expiry
            if (!jwtService.isTokenValid(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Check blacklist (logout)
            String jti = jwtService.extractJti(token);
            if (blacklistService.isBlacklisted(jti)) {
                log.debug("Blacklisted token jti={}", jti);
                filterChain.doFilter(request, response);
                return;
            }

            // 3. Extract claims
            String userId = jwtService.extractUserId(token);
            String role = jwtService.extractRole(token);

            Object principal;
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

            if ("ROLE_SERVICE".equals(role)) {
                // Service-to-service token: sub is a service name, not a user UUID
                principal = userId; // keep as String, e.g. "property-service"
            } else {
                // User token: sub is expected to be a UUID
                Integer tokenVersion = jwtService.extractTokenVersion(token);
                // ... your existing token-version validation against stored value ...
                principal = UUID.fromString(userId);
            }

            // 5. Set authentication in SecurityContext
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal, // principal — available as @AuthenticationPrincipal UUID userId
                    null,
                    List.of(new SimpleGrantedAuthority(role)));

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT filter error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
