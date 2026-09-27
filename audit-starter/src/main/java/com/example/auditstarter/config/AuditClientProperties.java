package com.example.auditstarter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "audit.client")
public class AuditClientProperties {

    public enum Transport { WEBCLIENT, KAFKA }

    /**
     * Which transport to use to reach the audit-service.
     * WEBCLIENT (default): synchronous HTTP call to the REST API.
     * KAFKA: publish to a topic the audit-service consumes —
     *        true fire-and-forget decoupling, survives audit-service
     *        downtime since events sit in the topic until it's back up.
     * Switching transport is a one-line config change; calling code using
     * AuditClient does not change either way.
     */
    private Transport transport = Transport.WEBCLIENT;

    /** Base URL of the audit-service, e.g. http://audit-service:8084 (WEBCLIENT transport only) */
    private String baseUrl = "http://localhost:8084";

    /**
     * Bearer token sent as Authorization header on WEBCLIENT transport
     * calls. audit-service requires a SERVICE- or ADMIN-roled JWT on its
     * REST ingestion endpoints — issue a long-lived SERVICE-roled token
     * for this microservice's service account from the Auth service and
     * put it here (env var, e.g. AUDIT_SERVICE_TOKEN — never commit a
     * real value). Not needed for KAFKA transport, since Kafka access
     * control happens at the broker, not over HTTP.
     *
     * For production, prefer fetching a short-lived token via your Auth
     * service's client-credentials flow and refreshing it on a schedule
     * over a single static long-lived token — this property is the
     * simplest viable wiring, not the final word on service-to-service
     * auth.
     */
    private String serviceToken;

    /** Whether the client is active. Set false in tests to no-op silently. */
    private boolean enabled = true;

    /** Per-request timeout. */
    private Duration timeout = Duration.ofSeconds(2);

    /** Number of retry attempts on failure (network errors / 5xx only, WEBCLIENT transport). */
    private int maxRetries = 2;

    /** Base backoff delay between retries; grows exponentially. */
    private Duration retryBackoff = Duration.ofMillis(200);

    /**
     * If true, a failure to reach the audit service after retries is only
     * logged (never thrown), so business logic is never affected.
     * Leave this true unless audit completeness is a hard compliance
     * requirement for your use case.
     */
    private boolean failSilently = true;

    /**
     * Whether the @AuditLog aspect/AOP proxying is active. Default true.
     * Set false if a service only wants manual AuditClient.log() calls
     * and doesn't want method-interception overhead anywhere.
     */
    private boolean annotationEnabled = true;

    private final Kafka kafka = new Kafka();

    @Data
    public static class Kafka {
        /** Topic the audit-service's @KafkaListener consumes from. */
        private String topic = "audit-events";
    }
}
