package com.realestate.auth_service.repository;

import com.realestate.auth_service.entity.AuditAction;
import com.realestate.auth_service.entity.AuditLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    // ── user activity history (paginated) ─────────────────────────────────────

    Page<AuditLog> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<AuditLog> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);

    // ── filter by action ──────────────────────────────────────────────────────

    Page<AuditLog> findAllByUserIdAndActionOrderByCreatedAtDesc(UUID userId, AuditAction action, Pageable pageable);

    // ── failed login attempts in a time window (rate limiting / alerting) ─────

    @Query(
            """
            SELECT COUNT(a) FROM AuditLog a
            WHERE a.user.id = :userId
              AND a.action = 'LOGIN_FAILED'
              AND a.createdAt >= :since
            """)
    long countFailedLoginsAfter(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    @Query(
            """
            SELECT COUNT(a) FROM AuditLog a
            WHERE a.ipAddress = :ip
              AND a.action = 'LOGIN_FAILED'
              AND a.createdAt >= :since
            """)
    long countFailedLoginsByIpAfter(@Param("ip") String ip, @Param("since") LocalDateTime since);

    // ── replay attack events ──────────────────────────────────────────────────

    @Query(
            """
            SELECT a FROM AuditLog a
            WHERE a.action = 'TOKEN_REPLAY_DETECTED'
              AND a.createdAt >= :since
            ORDER BY a.createdAt DESC
            """)
    List<AuditLog> findReplayAttacksSince(@Param("since") LocalDateTime since);

    // ── admin: all logs paginated ─────────────────────────────────────────────

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AuditLog> findAllByActionOrderByCreatedAtDesc(AuditAction action, Pageable pageable);

    // ── cleanup old logs (GDPR / retention policy) ────────────────────────────

    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.createdAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);
}
