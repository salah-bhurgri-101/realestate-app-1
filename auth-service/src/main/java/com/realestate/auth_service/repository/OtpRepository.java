package com.realestate.auth_service.repository;

import com.realestate.auth_service.entity.Otp;
import com.realestate.auth_service.entity.OtpType;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepository extends JpaRepository<Otp, UUID> {

    // ── fetch latest valid OTP ────────────────────────────────────────────────

    @Query(
            """
            SELECT o FROM Otp o
            WHERE o.user.id = :userId
              AND o.type = :type
              AND o.used = false
              AND o.expiresAt > CURRENT_TIMESTAMP
            ORDER BY o.createdAt DESC
            """)
    Optional<Otp> findLatestValidByUserIdAndType(@Param("userId") UUID userId, @Param("type") OtpType type);

    // ── mark used ─────────────────────────────────────────────────────────────

    @Modifying
    @Query(
            """
            UPDATE Otp o
            SET o.used = true
            WHERE o.user.id = :userId
              AND o.type = :type
              AND o.used = false
            """)
    int markAllUsedByUserIdAndType(@Param("userId") UUID userId, @Param("type") OtpType type);

    // ── increment attempts ────────────────────────────────────────────────────

    @Modifying
    @Query("UPDATE Otp o SET o.attempts = o.attempts + 1 WHERE o.id = :id")
    int incrementAttempts(@Param("id") UUID id);

    // ── check attempt count ───────────────────────────────────────────────────

    @Query(
            """
            SELECT o.attempts FROM Otp o
            WHERE o.user.id = :userId
              AND o.type = :type
              AND o.used = false
              AND o.expiresAt > CURRENT_TIMESTAMP
            ORDER BY o.createdAt DESC
            """)
    Optional<Integer> findAttemptsByUserIdAndType(@Param("userId") UUID userId, @Param("type") OtpType type);

    // ── delete before resend (generate fresh OTP) ─────────────────────────────

    @Modifying
    @Query(
            """
            DELETE FROM Otp o
            WHERE o.user.id = :userId
              AND o.type = :type
            """)
    int deleteAllByUserIdAndType(@Param("userId") UUID userId, @Param("type") OtpType type);

    // ── cleanup ───────────────────────────────────────────────────────────────

    @Modifying
    @Query("DELETE FROM Otp o WHERE o.expiresAt < :cutoff OR o.used = true")
    int deleteExpiredAndUsed(@Param("cutoff") LocalDateTime cutoff);
}
