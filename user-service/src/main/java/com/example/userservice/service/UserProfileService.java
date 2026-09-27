package com.example.userservice.service;

import com.example.userservice.config.RedisCacheConfig;
import com.example.userservice.domain.UserProfile;
import com.example.userservice.dto.request.UpsertProfileRequest;
import com.example.userservice.dto.response.ProfileResponse;
import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.repository.UserProfileRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository profileRepository;

    @Cacheable(value = RedisCacheConfig.PROFILES_CACHE, key = "#userId")
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        UserProfile profile = profileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user " + userId));
        return toResponse(profile);
    }

    @CacheEvict(value = RedisCacheConfig.PROFILES_CACHE, key = "#userId")
    @Transactional
    public ProfileResponse upsertProfile(UUID userId, UpsertProfileRequest request) {
        UserProfile profile = profileRepository
                .findByUserId(userId)
                .orElseGet(() -> UserProfile.builder().userId(userId).build());

        profile.setDisplayName(request.getDisplayName());
        profile.setBio(request.getBio());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setCoverUrl(request.getCoverUrl());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        profile.setPhone(request.getPhone());

        return toResponse(profileRepository.save(profile));
    }

    @CacheEvict(value = RedisCacheConfig.PROFILES_CACHE, key = "#userId")
    @Transactional
    public void deleteProfile(UUID userId) {
        profileRepository.deleteByUserId(userId);
    }

    private ProfileResponse toResponse(UserProfile p) {
        return ProfileResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .displayName(p.getDisplayName())
                .bio(p.getBio())
                .avatarUrl(p.getAvatarUrl())
                .coverUrl(p.getCoverUrl())
                .dateOfBirth(p.getDateOfBirth())
                .gender(p.getGender())
                .phone(p.getPhone())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
