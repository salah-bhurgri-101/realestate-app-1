package com.realestate.auditservice.dto;

import com.realestate.auditservice.entity.AuditAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload other microservices send when reporting an audit event.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogRequest {

    @NotBlank(message = "serviceName is required") private String serviceName;

    @NotNull(message = "action is required") private AuditAction action;

    private String entityType;

    private String entityId;

    private String username;

    private String userId;

    private String ipAddress;

    private String description;

    /** Arbitrary key/value metadata; will be serialized to JSON for storage. */
    private Map<String, Object> metadata;

    private String status; // SUCCESS / FAILURE, defaults to SUCCESS if omitted

    private String traceId;
}
