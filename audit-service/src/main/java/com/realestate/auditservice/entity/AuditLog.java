package com.realestate.auditservice.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Persisted record of a single audit-worthy event.
 *
 * Designed so that ANY microservice in the system can write to this
 * table (directly, via the REST API, or via a shared client library)
 * without needing to know about each other's domain models.
 */
@Entity
@Table(
        name = "audit_logs",
        indexes = {
            // NOTE: These reflect the indexes created in Flyway migration V1.
            // This annotation is documentation only — ddl-auto=validate means
            // Hibernate does not create or alter indexes.
            @Index(name = "idx_audit_service_name", columnList = "serviceName"),
            @Index(name = "idx_audit_entity", columnList = "entityType,entityId"),
            @Index(name = "idx_audit_user", columnList = "username"),
            @Index(name = "idx_audit_created_at", columnList = "createdAt")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** Logical name of the microservice that produced this event, e.g. "order-service". */
    @Column(nullable = false, length = 100)
    private String serviceName;

    /** What happened. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AuditAction action;

    /** Domain type affected, e.g. "Order", "User", "Invoice". */
    @Column(length = 100)
    private String entityType;

    /** Identifier of the affected entity (kept as String to stay entity-agnostic). */
    @Column(length = 100)
    private String entityId;

    /** Who performed the action. */
    @Column(length = 150)
    private String username;

    /** User id, if numeric/UUID id is preferred over username. */
    @Column(length = 100)
    private String userId;

    /** Caller IP address. */
    @Column(length = 45)
    private String ipAddress;

    /** Free-text description of the event. */
    @Column(length = 1000)
    private String description;

    /**
     * Arbitrary structured payload (e.g. before/after diff, request params)
     * stored as JSON text. Use a JSON column type if your DB supports it.
     */
    //    @Lob
    //    @Column(columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /** Outcome of the action. */
    @Column(length = 20)
    @Builder.Default
    private String status = "SUCCESS"; // SUCCESS, FAILURE

    /** Correlation/trace id to tie this entry back to a distributed trace. */
    @Column(length = 100)
    private String traceId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
