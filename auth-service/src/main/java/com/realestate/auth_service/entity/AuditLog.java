package com.realestate.auth_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(
        name = "audit_logs",
        indexes = {
            @Index(name = "idx_audit_user", columnList = "user_id"),
            @Index(name = "idx_audit_action", columnList = "action"),
            @Index(name = "idx_audit_created_at", columnList = "createdAt")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * Nullable — some events (e.g. failed login for unknown email) have no user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 512)
    private String userAgent;

    /**
     * Optional JSON string for extra context.
     * E.g. {"reason":"invalid_otp","attempts":3}
     */
    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── lifecycle ─────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
