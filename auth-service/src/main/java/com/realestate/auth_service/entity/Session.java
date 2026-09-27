package com.realestate.auth_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(
        name = "sessions",
        indexes = {
            @Index(name = "idx_session_user", columnList = "user_id"),
            @Index(name = "idx_session_refresh_token", columnList = "refresh_token_id", unique = true)
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Each session is backed by exactly one refresh token.
     * When the refresh token is revoked, the session ends.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "refresh_token_id", nullable = false, unique = true)
    private RefreshToken refreshToken;

    @Column(length = 100)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DeviceType deviceType;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 512)
    private String userAgent;

    /** Updated on every authenticated request — used to detect stale sessions. */
    @Column(nullable = false)
    private LocalDateTime lastActiveAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── lifecycle ─────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActiveAt = LocalDateTime.now();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    public void touch() {
        this.lastActiveAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return refreshToken != null && refreshToken.isValid();
    }
}
