package com.realestate.auditservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.realestate.auditservice.dto.AuditLogRequest;
import com.realestate.auditservice.dto.AuditLogResponse;
import com.realestate.auditservice.entity.AuditAction;
import com.realestate.auditservice.entity.AuditLog;
import com.realestate.auditservice.exception.AuditLogNotFoundException;
import com.realestate.auditservice.repository.AuditLogRepository;
import com.realestate.auditservice.service.AuditLogService;
import com.realestate.auditservice.specification.AuditLogSpecification;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AuditLogResponse record(AuditLogRequest request) {
        AuditLog saved = repository.save(toEntity(request));
        return toResponse(saved);
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<Void> recordAsync(AuditLogRequest request) {
        try {
            repository.save(toEntity(request));
        } catch (Exception ex) {
            // Audit logging must never break the caller's business flow.
            // Log and swallow instead of propagating.
            log.error(
                    "Failed to persist async audit log for service={}, action={}: {}",
                    request.getServiceName(),
                    request.getAction(),
                    ex.getMessage(),
                    ex);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse getById(String id) {
        return repository.findById(id).map(this::toResponse).orElseThrow(() -> new AuditLogNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findAuditLog(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> search(
            String serviceName,
            AuditAction action,
            String username,
            String entityType,
            String entityId,
            Instant from,
            Instant to,
            Pageable pageable) {
        var spec = AuditLogSpecification.build(serviceName, action, username, entityType, entityId, from, to);

        return repository.findAll(spec, pageable).map(this::toResponse);
    }

    private AuditLog toEntity(AuditLogRequest request) {
        return AuditLog.builder()
                .serviceName(request.getServiceName())
                .action(request.getAction())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .username(request.getUsername())
                .userId(request.getUserId())
                .ipAddress(request.getIpAddress())
                .description(request.getDescription())
                .metadata(toJson(request.getMetadata()))
                .status(request.getStatus() != null ? request.getStatus() : "SUCCESS")
                .traceId(request.getTraceId())
                .build();
    }

    private AuditLogResponse toResponse(AuditLog entity) {
        return AuditLogResponse.builder()
                .id(entity.getId())
                .serviceName(entity.getServiceName())
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .username(entity.getUsername())
                .userId(entity.getUserId())
                .ipAddress(entity.getIpAddress())
                .description(entity.getDescription())
                .metadata(fromJson(entity.getMetadata()))
                .status(entity.getStatus())
                .traceId(entity.getTraceId())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String toJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize audit metadata, storing as null: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> fromJson(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("Could not deserialize stored audit metadata: {}", e.getMessage());
            return null;
        }
    }
}
