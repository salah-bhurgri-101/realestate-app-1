package com.example.auditstarter.examples;

import com.example.auditstarter.AuditClient;
import com.example.auditstarter.annotation.AuditLog;
import com.example.auditstarter.model.AuditAction;
import com.example.auditstarter.model.AuditEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Not part of the library build — illustrates how a consuming microservice
 * (e.g. order-service) would use AuditClient once audit-starter.jar is a
 * dependency. Delete or keep as living documentation.
 */
@Service
@RequiredArgsConstructor
class OrderServiceExampleUsage {

    private final AuditClient auditClient;

    /** Option 1: manual, fire-and-forget call. Full control over the payload. */
    public void shipOrderManualAudit(String orderId, String previousStatus) {
        // ... business logic to ship the order ...

        AuditEvent event = AuditEvent.builder()
                .action(AuditAction.UPDATE)
                .entityType("Order")
                .entityId(orderId)
                .description("Order shipped")
                .metadata(Map.of("previousStatus", previousStatus, "newStatus", "SHIPPED"))
                .build();

        auditClient.log(event); // returns immediately, HTTP call happens in background
    }

    /** Option 2: declarative — zero manual logging code, success/failure captured automatically. */
    @AuditLog(action = AuditAction.UPDATE, entityType = "Order", entityIdExpression = "#orderId",
               description = "Order shipped")
    public void shipOrderDeclarativeAudit(String orderId) {
        // ... business logic only ...
    }
}
