package com.realestate.auth_service.repository;

import com.realestate.auth_service.entity.RefreshToken;
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
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByUserId(UUID userId);

    // ── active tokens for a user ──────────────────────────────────────────────

    @Query(
            """
            SELECT rt FROM RefreshToken rt
            WHERE rt.user.id = :userId
              AND rt.revokedAt IS NULL
              AND rt.expiresAt > CURRENT_TIMESTAMP
            ORDER BY rt.createdAt DESC
            """)
    List<RefreshToken> findActiveByUserId(@Param("userId") UUID userId);

    // ── single-device logout ──────────────────────────────────────────────────

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :now WHERE rt.tokenHash = :tokenHash AND rt.revokedAt IS NULL")
    int revokeByTokenHash(@Param("tokenHash") String tokenHash, @Param("now") LocalDateTime now);

    // ── logout-all: revoke every active token for this user ──────────────────

    @Modifying
    @Query(
            """
            UPDATE RefreshToken rt
            SET rt.revokedAt = :now
            WHERE rt.user.id = :userId
              AND rt.revokedAt IS NULL
            """)
    int revokeAllByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    // ── replay-attack detection ───────────────────────────────────────────────

    /**
     * Returns true if a token with this hash exists but is already revoked.
     * Indicates a possible token replay attack — caller should revoke ALL sessions.
     */
    @Query(
            """
            SELECT COUNT(rt) > 0 FROM RefreshToken rt
            WHERE rt.tokenHash = :tokenHash
              AND rt.revokedAt IS NOT NULL
            """)
    boolean existsRevokedByTokenHash(@Param("tokenHash") String tokenHash);

    // ── cleanup ───────────────────────────────────────────────────────────────

    @Modifying
    @Query(
            """
            DELETE FROM RefreshToken rt
            WHERE rt.expiresAt < :cutoff
               OR rt.revokedAt IS NOT NULL
            """)
    int deleteExpiredAndRevoked(@Param("cutoff") LocalDateTime cutoff);

    long countByUserIdAndRevokedAtIsNull(UUID userId);
}
