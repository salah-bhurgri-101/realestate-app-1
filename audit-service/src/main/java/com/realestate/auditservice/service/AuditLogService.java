package com.realestate.auditservice.service;

import com.realestate.auditservice.dto.AuditLogRequest;
import com.realestate.auditservice.dto.AuditLogResponse;
import com.realestate.auditservice.entity.AuditAction;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {

    /** Synchronous write — use when the caller needs a guarantee the record was persisted. */
    AuditLogResponse record(AuditLogRequest request);

    /** Fire-and-forget write — use from hot paths where audit logging must not block the caller. */
    CompletableFuture<Void> recordAsync(AuditLogRequest request);

    AuditLogResponse getById(String id);

    Page<AuditLogResponse> search(
            String serviceName,
            AuditAction action,
            String username,
            String entityType,
            String entityId,
            Instant from,
            Instant to,
            Pageable pageable);

    Page<AuditLogResponse> findAuditLog(Pageable pageable);
}
