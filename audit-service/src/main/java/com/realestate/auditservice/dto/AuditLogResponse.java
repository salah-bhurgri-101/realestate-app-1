package com.realestate.auditservice.dto;

import com.realestate.auditservice.entity.AuditAction;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private String id;
    private String serviceName;
    private AuditAction action;
    private String entityType;
    private String entityId;
    private String username;
    private String userId;
    private String ipAddress;
    private String description;
    private Map<String, Object> metadata;
    private String status;
    private String traceId;
    private Instant createdAt;
}
