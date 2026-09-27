package com.example.userservice.repository;

import com.example.userservice.domain.UserFollow;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {
    Optional<UserFollow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    Page<UserFollow> findByFollowerId(UUID followerId, Pageable pageable); // who this user follows

    Page<UserFollow> findByFolloweeId(UUID followeeId, Pageable pageable); // this user's followers

    long countByFollowerId(UUID followerId);

    long countByFolloweeId(UUID followeeId);

    void deleteByFollowerIdOrFolloweeId(UUID followerId, UUID followeeId);
}
