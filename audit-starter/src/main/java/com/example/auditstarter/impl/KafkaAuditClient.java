package com.example.auditstarter.impl;

import com.example.auditstarter.AuditClient;
import com.example.auditstarter.config.AuditClientProperties;
import com.example.auditstarter.model.AuditEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Publishes audit events to a Kafka topic instead of calling the
 * audit-service over HTTP. This gives TRUE async decoupling:
 *  - Publishing never waits on audit-service availability or latency.
 *  - If audit-service is down or slow, events queue up in the topic
 *    and are processed once it recovers — nothing is lost (subject to
 *    normal Kafka retention/replication guarantees).
 *  - Multiple consumers (e.g. a second compliance pipeline) can read the
 *    same topic independently of audit-service.
 *
 * The partition key is serviceName:entityType:entityId when available,
 * so all events for the same entity land on the same partition and are
 * processed in order by the consumer.
 */
@Slf4j
public class KafkaAuditClient implements AuditClient {

    private final KafkaTemplate<String, AuditEvent> kafkaTemplate;
    private final AuditClientProperties properties;

    @Value("${spring.application.name:unknown-service}")
    private String defaultServiceName;

    public KafkaAuditClient(KafkaTemplate<String, AuditEvent> kafkaTemplate, AuditClientProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @Override
    public CompletableFuture<Void> log(AuditEvent event) {
        if (!properties.isEnabled()) {
            return CompletableFuture.completedFuture(null);
        }
        enrich(event);

        CompletableFuture<Void> result = new CompletableFuture<>();
        kafkaTemplate.send(properties.getKafka().getTopic(), partitionKey(event), event)
                .whenComplete((sendResult, ex) -> {
                    if (ex != null) {
                        handleFailure(event, ex);
                        result.complete(null); // log() never propagates failures to the caller
                    } else {
                        logSuccess(event, sendResult);
                        result.complete(null);
                    }
                });
        return result;
    }

    @Override
    public void logBlocking(AuditEvent event) {
        if (!properties.isEnabled()) return;
        enrich(event);
        try {
            SendResult<String, AuditEvent> sendResult = kafkaTemplate
                    .send(properties.getKafka().getTopic(), partitionKey(event), event)
                    .get(properties.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
            logSuccess(event, sendResult);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            handleFailure(event, e);
            if (!properties.isFailSilently()) {
                throw new RuntimeException("Interrupted while publishing audit event", e);
            }
        } catch (ExecutionException | TimeoutException e) {
            handleFailure(event, e);
            if (!properties.isFailSilently()) {
                throw new RuntimeException("Failed to publish audit event to Kafka", e);
            }
        }
    }

    @Override
    public Mono<Void> logReactive(AuditEvent event) {
        if (!properties.isEnabled()) {
            return Mono.empty();
        }
        enrich(event);
        return Mono.fromFuture(() ->
                        kafkaTemplate.send(properties.getKafka().getTopic(), partitionKey(event), event)
                                .toCompletableFuture()
                )
                .timeout(properties.getTimeout())
                .doOnNext(sendResult -> logSuccess(event, sendResult))
                .then()
                .onErrorResume(err -> {
                    handleFailure(event, err);
                    return properties.isFailSilently() ? Mono.empty() : Mono.error(err);
                });
    }

    private String partitionKey(AuditEvent event) {
        if (event.getEntityType() != null && event.getEntityId() != null) {
            return event.getServiceName() + ":" + event.getEntityType() + ":" + event.getEntityId();
        }
        return event.getServiceName();
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

    private void logSuccess(AuditEvent event, SendResult<String, AuditEvent> sendResult) {
        log.debug("Audit event published [topic={}, partition={}, offset={}, entityType={}, entityId={}]",
                sendResult.getRecordMetadata().topic(),
                sendResult.getRecordMetadata().partition(),
                sendResult.getRecordMetadata().offset(),
                event.getEntityType(), event.getEntityId());
    }

    private void handleFailure(AuditEvent event, Throwable ex) {
        log.warn("Failed to publish audit event to Kafka [service={}, action={}, entityType={}, entityId={}]: {}",
                event.getServiceName(), event.getAction(), event.getEntityType(), event.getEntityId(),
                ex.toString());
    }
}
