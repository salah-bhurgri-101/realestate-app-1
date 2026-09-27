package com.realestate.auth_service.repository;

import com.realestate.auth_service.entity.Role;
import com.realestate.auth_service.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Page<User> findAllByRole(Role role, Pageable pageable);

    Optional<User> findByEmailAndIsActiveTrue(String email);

    // ── token version ─────────────────────────────────────────────────────────

    /**
     * Increment tokenVersion atomically.
     * Called on logout-all — instantly invalidates all existing JWTs.
     */
    @Modifying
    @Query("UPDATE User u SET u.tokenVersion = u.tokenVersion + 1 WHERE u.id = :id")
    int incrementTokenVersion(@Param("id") UUID id);

    /**
     * Used by JwtAuthenticationFilter to verify the token's embedded version
     * matches the current DB value (without loading the full entity).
     */
    @Query("SELECT u.tokenVersion FROM User u WHERE u.id = :id")
    Optional<Integer> findTokenVersionById(@Param("id") UUID id);

    // ── account state ─────────────────────────────────────────────────────────

    @Modifying
    @Query("UPDATE User u SET u.isVerified = true WHERE u.id = :id")
    int markVerified(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE User u SET u.isActive = false WHERE u.id = :id")
    int deactivate(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE User u SET u.isActive = true WHERE u.id = :id")
    int activate(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE User u SET u.role = :role WHERE u.id = :id")
    int updateRole(@Param("id") UUID id, @Param("role") Role role);

    @Modifying
    @Query("UPDATE User u SET u.passwordHash = :hash, u.tokenVersion = u.tokenVersion + 1 WHERE u.id = :id")
    int updatePasswordAndInvalidateSessions(@Param("id") UUID id, @Param("hash") String hash);
}
