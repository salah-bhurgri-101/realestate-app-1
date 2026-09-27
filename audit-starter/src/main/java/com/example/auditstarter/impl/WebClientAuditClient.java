package com.example.auditstarter.impl;

import com.example.auditstarter.AuditClient;
import com.example.auditstarter.config.AuditClientProperties;
import com.example.auditstarter.model.AuditEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class WebClientAuditClient implements AuditClient {

    private final WebClient webClient;
    private final AuditClientProperties properties;

    @Value("${spring.application.name:unknown-service}")
    private String defaultServiceName;

    public WebClientAuditClient(WebClient webClient, AuditClientProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    @Override
    public CompletableFuture<Void> log(AuditEvent event) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (!properties.isEnabled()) {
            future.complete(null);
            return future;
        }
        logReactive(event)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        v -> future.complete(null),
                        err -> future.complete(null) // errors already handled/logged inside logReactive
                );
        return future;
    }

    @Override
    public void logBlocking(AuditEvent event) {
        if (!properties.isEnabled()) return;
        logReactive(event).block(properties.getTimeout().plus(Duration.ofSeconds(1)));
    }

    @Override
    public Mono<Void> logReactive(AuditEvent event) {
        if (!properties.isEnabled()) {
            return Mono.empty();
        }

        enrich(event);

        return webClient.post()
                .uri("/api/v1/audit-logs/async")
                .bodyValue(event)
                .retrieve()
                .toBodilessEntity()
                .timeout(properties.getTimeout())
                .retryWhen(buildRetrySpec())
                .doOnError(err -> log.warn(
                        "Audit event delivery failed after retries [service={}, action={}, entityType={}, entityId={}]: {}",
                        event.getServiceName(), event.getAction(), event.getEntityType(), event.getEntityId(),
                        err.toString()
                ))
                .then()
                .onErrorResume(err -> {
                    if (properties.isFailSilently()) {
                        // Audit logging must never break the caller's business flow.
                        return Mono.empty();
                    }
                    return Mono.error(err);
                });
    }

    private Retry buildRetrySpec() {
        return Retry.backoff(properties.getMaxRetries(), properties.getRetryBackoff())
                .filter(this::isRetryable)
                .onRetryExhaustedThrow((spec, signal) -> signal.failure());
    }

    /** Only retry on network-level failures or 5xx — never retry 4xx (bad request, validation, etc.). */
    private boolean isRetryable(Throwable throwable) {
        if (throwable instanceof WebClientRequestException) {
            return true;
        }
        if (throwable instanceof WebClientResponseException responseException) {
            return responseException.getStatusCode().is5xxServerError();
        }
        return false;
    }

    private void enrich(AuditEvent event) {
        if (event.getServiceName() == null || event.getServiceName().isBlank()) {
            event.setServiceName(defaultServiceName);
        }
        if (event.getTraceId() == null || event.getTraceId().isBlank()) {
            event.setTraceId(UUID.randomUUID().toString());
        }
        if (event.getStatus() == null || event.getStatus().isBlank()) {
            event.setStatus("SUCCESS");
        }
    }
}
