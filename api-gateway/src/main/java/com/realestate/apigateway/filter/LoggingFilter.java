package com.realestate.apigateway.filter;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        long startTime = System.currentTimeMillis();

        ServerHttpRequest request = exchange.getRequest();

        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        final String finalRequestId = requestId;

        ServerHttpRequest mutatedRequest =
                request.mutate().header(REQUEST_ID_HEADER, finalRequestId).build();

        ServerWebExchange mutatedExchange =
                exchange.mutate().request(mutatedRequest).build();

        mutatedExchange.getResponse().beforeCommit(() -> {
            mutatedExchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, finalRequestId);
            return Mono.empty();
        });

        log.info(
                "[{}] --> {} {}",
                finalRequestId,
                request.getMethod(),
                request.getURI().getPath());

        return chain.filter(mutatedExchange).doFinally(signalType -> {
            long duration = System.currentTimeMillis() - startTime;

            log.info(
                    "[{}] <-- {} {} ({} ms)",
                    finalRequestId,
                    mutatedExchange.getResponse().getStatusCode(),
                    request.getURI().getPath(),
                    duration);
        });
    }

    @Override
    public int getOrder() {
        // Runs before the JWT filter so the request id exists for its logs too.
        return -2;
    }
}
