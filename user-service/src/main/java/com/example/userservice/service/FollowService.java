package com.example.userservice.service;

import com.example.userservice.config.RedisCacheConfig;
import com.example.userservice.domain.UserFollow;
import com.example.userservice.dto.response.FollowCountsResponse;
import com.example.userservice.dto.response.FollowResponse;
import com.example.userservice.exception.InvalidOperationException;
import com.example.userservice.repository.UserFollowRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final UserFollowRepository followRepository;

    @Caching(
            evict = {
                @CacheEvict(value = RedisCacheConfig.FOLLOW_COUNTS_CACHE, key = "#followerId"),
                @CacheEvict(value = RedisCacheConfig.FOLLOW_COUNTS_CACHE, key = "#followeeId")
            })
    @Transactional
    public FollowResponse follow(UUID followerId, UUID followeeId) {
        if (followerId.equals(followeeId)) {
            throw new InvalidOperationException("A user cannot follow themselves.");
        }
        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            return followRepository
                    .findByFollowerIdAndFolloweeId(followerId, followeeId)
                    .map(this::toResponse)
                    .orElseThrow(); // effectively unreachable given the exists() check above
        }
        UserFollow follow = UserFollow.builder()
                .followerId(followerId)
                .followeeId(followeeId)
                .build();
        return toResponse(followRepository.save(follow));
    }

    @Caching(
            evict = {
                @CacheEvict(value = RedisCacheConfig.FOLLOW_COUNTS_CACHE, key = "#followerId"),
                @CacheEvict(value = RedisCacheConfig.FOLLOW_COUNTS_CACHE, key = "#followeeId")
            })
    @Transactional
    public void unfollow(UUID followerId, UUID followeeId) {
        followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId).ifPresent(followRepository::delete);
    }

    @Transactional(readOnly = true)
    public Page<FollowResponse> getFollowing(UUID userId, Pageable pageable) {
        return followRepository.findByFollowerId(userId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<FollowResponse> getFollowers(UUID userId, Pageable pageable) {
        return followRepository.findByFolloweeId(userId, pageable).map(this::toResponse);
    }

    @Cacheable(value = RedisCacheConfig.FOLLOW_COUNTS_CACHE, key = "#userId")
    @Transactional(readOnly = true)
    public FollowCountsResponse getCounts(UUID userId) {
        return FollowCountsResponse.builder()
                .userId(userId)
                .followersCount(followRepository.countByFolloweeId(userId))
                .followingCount(followRepository.countByFollowerId(userId))
                .build();
    }

    private FollowResponse toResponse(UserFollow f) {
        return FollowResponse.builder()
                .id(f.getId())
                .followerId(f.getFollowerId())
                .followeeId(f.getFolloweeId())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
