package com.realestate.auditservice.kafka;

import com.realestate.auditservice.dto.AuditLogRequest;
import com.realestate.auditservice.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes audit events published by any microservice using audit-starter
 * with transport=kafka, and persists them through the same AuditLogService
 * used by the REST API — so both ingestion paths (HTTP and Kafka) share
 * identical validation/persistence logic.
 *
 * Throwing from this method triggers the configured DefaultErrorHandler:
 * retry with backoff, then publish to the dead-letter topic if retries
 * are exhausted, rather than acknowledging and dropping the message.
 *
 * Note: Kafka delivery is at-least-once. Duplicate audit entries on
 * redelivery are possible (e.g. after a consumer crash mid-batch); if you
 * need strict dedup, have the producer set a stable AuditEvent.traceId
 * per logical event and add a unique constraint / upsert on traceId here.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditLogService auditLogService;

    @KafkaListener(
            topics = "${audit.kafka.topic:audit-events}",
            groupId = "${audit.kafka.consumer-group:audit-service}",
            containerFactory = "auditKafkaListenerContainerFactory")
    public void onAuditEvent(ConsumerRecord<String, AuditLogRequest> record) {

        // Deserialization failure: ErrorHandlingDeserializer sets value=null
        // and populates the exception header. Log it clearly instead of NPE.
        if (record.value() == null) {
            log.error(
                    "[AUDIT-CONSUMER] Deserialization failed — message skipped "
                            + "[topic={}, partition={}, offset={}, key={}]. "
                            + "Check audit-events.DLT topic for the raw payload.",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key());
            return;
        }

        AuditLogRequest request = record.value();
        log.info(
                "[AUDIT-CONSUMER] Received [topic={}, partition={}, offset={}] "
                        + "service={}, action={}, entityType={}, entityId={}",
                record.topic(),
                record.partition(),
                record.offset(),
                request.getServiceName(),
                request.getAction(),
                request.getEntityType(),
                request.getEntityId());

        auditLogService.record(request);

        log.info(
                "[AUDIT-CONSUMER] Persisted successfully [offset={}, service={}, action={}]",
                record.offset(),
                request.getServiceName(),
                request.getAction());
    }
}
