package com.realestate.auth_service.repository;

import com.realestate.auth_service.entity.Session;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByRefreshTokenId(UUID refreshTokenId);

    List<Session> findAllByUserId(UUID userId);

    // ── active sessions for user (used in GetMe / device listing) ────────────

    @Query(
            """
            SELECT s FROM Session s
            JOIN FETCH s.refreshToken rt
            WHERE s.user.id = :userId
              AND rt.revokedAt IS NULL
              AND rt.expiresAt > CURRENT_TIMESTAMP
            ORDER BY s.lastActiveAt DESC
            """)
    List<Session> findActiveSessionsByUserId(@Param("userId") UUID userId);

    // ── touch lastActiveAt ────────────────────────────────────────────────────

    @Modifying
    @Query("UPDATE Session s SET s.lastActiveAt = :now WHERE s.id = :id")
    int touch(@Param("id") UUID id, @Param("now") LocalDateTime now);

    // ── delete by refreshToken (logout single device) ─────────────────────────

    @Modifying
    @Query("DELETE FROM Session s WHERE s.refreshToken.id = :refreshTokenId")
    int deleteByRefreshTokenId(@Param("refreshTokenId") UUID refreshTokenId);

    // ── delete all sessions for user (logout-all) ─────────────────────────────

    @Modifying
    @Query("DELETE FROM Session s WHERE s.user.id = :userId")
    int deleteAllByUserId(@Param("userId") UUID userId);

    // ── count active sessions (useful for dashboard / max-device limit) ───────

    @Query(
            """
            SELECT COUNT(s) FROM Session s
            JOIN s.refreshToken rt
            WHERE s.user.id = :userId
              AND rt.revokedAt IS NULL
              AND rt.expiresAt > CURRENT_TIMESTAMP
            """)
    long countActiveByUserId(@Param("userId") UUID userId);

    // ── cleanup stale sessions ────────────────────────────────────────────────

    @Modifying
    @Query(
            """
            DELETE FROM Session s
            WHERE s.refreshToken.revokedAt IS NOT NULL
               OR s.refreshToken.expiresAt < :cutoff
            """)
    int deleteInactiveSessions(@Param("cutoff") LocalDateTime cutoff);
}
