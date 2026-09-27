package com.realestate.apigateway.filter;

import com.realestate.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private static final List<String> OPEN_PATHS = List.of(
            "/api/properties/search",
            "/api/properties/featured",
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/otp/verify",
            "/api/v1/auth/otp/resend",
            "/api/v1/auth/token/refresh",
            "/api/v1/auth/password/forgot",
            "/api/v1/auth/password/reset",
            "/actuator/health",
            "/v3/api-docs/");

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isOpenPath(path)) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return reject(exchange, "Missing or malformed Authorization header");
        }

        String token = authHeader.substring(7).trim();

        try {
            Claims claims = jwtUtil.parseAndValidate(token);

            String userId = claims.getSubject();
            String roles = claims.get("role", String.class);

            if (userId == null || userId.isBlank()) {
                return reject(exchange, "Token has no subject (user id) claim");
            }

            ServerHttpRequest mutatedRequest = request.mutate()
                    .headers(headers -> {
                        // Never trust whatever the client sent in these headers.
                        headers.remove("X-User-Id");
                        headers.remove("X-User-Roles");
                        headers.set("X-User-Id", userId);
                        if (roles != null && !roles.isBlank()) {
                            headers.set("X-User-Roles", roles);
                        }
                    })
                    .build();

            ServerWebExchange mutatedExchange =
                    exchange.mutate().request(mutatedRequest).build();
            return chain.filter(mutatedExchange);

        } catch (JwtException | IllegalArgumentException ex) {
            return reject(exchange, "Invalid or expired token");
        }
    }

    private boolean isOpenPath(String path) {
        return OPEN_PATHS.stream().anyMatch(openPath -> path.equals(openPath) || path.startsWith(openPath + "/"));
    }

    private Mono<Void> reject(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = String.format("{\"error\":\"unauthorized\",\"message\":\"%s\"}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // Runs after LoggingFilter (-2) but before routing.
        return -1;
    }
}
