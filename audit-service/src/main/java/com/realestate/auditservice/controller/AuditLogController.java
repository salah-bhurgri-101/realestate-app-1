package com.realestate.auditservice.controller;

import com.realestate.auditservice.dto.AuditLogRequest;
import com.realestate.auditservice.dto.AuditLogResponse;
import com.realestate.auditservice.entity.AuditAction;
import com.realestate.auditservice.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Centralized audit log API for all microservices")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    @Operation(summary = "Record an audit event (synchronous, waits for persistence)")
    @PostMapping
    public ResponseEntity<AuditLogResponse> create(@Valid @RequestBody AuditLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auditLogService.record(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    @Operation(summary = "Record an audit event without blocking the caller")
    @PostMapping("/async")
    public ResponseEntity<Void> createAsync(@Valid @RequestBody AuditLogRequest request) {
        auditLogService.recordAsync(request);
        return ResponseEntity.accepted().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Fetch a single audit log entry by id")
    @GetMapping("/{id}")
    public ResponseEntity<AuditLogResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(auditLogService.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search/filter audit logs with pagination")
    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> search(
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        //        LOG.info("Get posts by page='{}' and query='{}'", page, query);
        if (serviceName == null
                && action == null
                && username == null
                && entityType == null
                && entityId == null
                && from == null
                && to == null) {
            return ResponseEntity.ok(auditLogService.findAuditLog(pageable));
        }

        return ResponseEntity.ok(
                auditLogService.search(serviceName, action, username, entityType, entityId, from, to, pageable));
    }
}
