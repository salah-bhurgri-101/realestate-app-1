package com.example.auditstarter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Maps 1:1 onto AuditLogRequest in the audit-service.
 * Use the builder for a fluent call site:
 *
 *   AuditEvent.builder()
 *       .action(AuditAction.UPDATE)
 *       .entityType("Order")
 *       .entityId(orderId)
 *       .username(currentUser)
 *       .description("Order shipped")
 *       .build();
 *
 * serviceName and traceId are filled in automatically by the client if left null.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    private String serviceName;
    private AuditAction action;
    private String entityType;
    private String entityId;
    private String username;
    private String userId;
    private String ipAddress;
    private String description;
    private Map<String, Object> metadata;
    private String status; // SUCCESS / FAILURE, defaults to SUCCESS server-side if omitted
    private String traceId;
}
