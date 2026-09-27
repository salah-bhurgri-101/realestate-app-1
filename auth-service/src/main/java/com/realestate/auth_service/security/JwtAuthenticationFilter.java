package com.realestate.auth_service.security;

import com.realestate.auth_service.repository.UserRepository;
import com.realestate.auth_service.service.JwtService;
import com.realestate.auth_service.service.TokenBlacklistService;
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
import org.springframework.lang.NonNull;
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

    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

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
            Integer tokenVersion = jwtService.extractTokenVersion(token);

            // 4. Verify tokenVersion (logout-all check) — lightweight DB call
            Integer currentVersion =
                    userRepository.findTokenVersionById(UUID.fromString(userId)).orElse(null);

            if (currentVersion == null || !currentVersion.equals(tokenVersion)) {
                log.debug("Token version mismatch userId={}", userId);
                filterChain.doFilter(request, response);
                return;
            }

            // 5. Set authentication in SecurityContext
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    UUID.fromString(userId), // principal — available as @AuthenticationPrincipal UUID userId
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
