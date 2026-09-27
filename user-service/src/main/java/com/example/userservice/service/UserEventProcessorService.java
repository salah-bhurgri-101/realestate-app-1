package com.example.userservice.service;

import com.example.userservice.config.RedisCacheConfig;
import com.example.userservice.domain.ProcessedEvent;
import com.example.userservice.domain.UserProfile;
import com.example.userservice.domain.enums.AuthEventType;
import com.example.userservice.event.AuthUserEvent;
import com.example.userservice.repository.AddressRepository;
import com.example.userservice.repository.ProcessedEventRepository;
import com.example.userservice.repository.UserFollowRepository;
import com.example.userservice.repository.UserProfileRepository;
import com.example.userservice.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applies inbound identity events from auth_service. Every event is checked
 * against processed_events for idempotency BEFORE being applied, and the
 * ledger insert happens in the SAME transaction as the resulting state
 * change — so a redelivered/replayed message from Kafka can never double-
 * apply a cascade delete or corrupt state.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventProcessorService {

    private final ProcessedEventRepository processedEventRepository;
    private final UserProfileRepository profileRepository;
    private final AddressRepository addressRepository;
    private final UserSettingsRepository settingsRepository;
    private final UserFollowRepository followRepository;

    @CacheEvict(
            value = {
                RedisCacheConfig.PROFILES_CACHE,
                RedisCacheConfig.SETTINGS_CACHE,
                RedisCacheConfig.FOLLOW_COUNTS_CACHE
            },
            key = "#event.userId")
    @Transactional
    public void process(AuthUserEvent event) {
        if (processedEventRepository.existsByEventId(event.getEventId())) {
            log.info("Skipping already-processed event {} ({})", event.getEventId(), event.getEventType());
            return;
        }

        switch (event.getEventType()) {
            case USER_DELETED -> handleUserDeleted(event);
            case USER_DEACTIVATED -> handleUserDeactivated(event);
            case USER_REGISTERED, USER_UPDATED, USER_REACTIVATED -> upsertProfile(event);
        }

        markProcessed(event);
    }

    private void upsertProfile(AuthUserEvent event) {
        if (event.getEventType() != AuthEventType.USER_REGISTERED
                && event.getDisplayName() == null
                && event.getBio() == null
                && event.getAvatarUrl() == null
                && event.getCoverUrl() == null
                && event.getDateOfBirth() == null
                && event.getGender() == null
                && event.getPhone() == null) {
            return;
        }

        UserProfile profile = profileRepository
                .findByUserId(event.getUserId())
                .orElseGet(() -> UserProfile.builder().userId(event.getUserId()).build());

        if (event.getDisplayName() != null) profile.setDisplayName(event.getDisplayName());
        if (event.getBio() != null) profile.setBio(event.getBio());
        if (event.getAvatarUrl() != null) profile.setAvatarUrl(event.getAvatarUrl());
        if (event.getCoverUrl() != null) profile.setCoverUrl(event.getCoverUrl());
        if (event.getDateOfBirth() != null) profile.setDateOfBirth(event.getDateOfBirth());
        if (event.getGender() != null) profile.setGender(event.getGender());
        if (event.getPhone() != null) profile.setPhone(event.getPhone());

        profileRepository.save(profile);
        log.info("Synchronized profile for user {} from {}", event.getUserId(), event.getEventType());
    }

    private void handleUserDeleted(AuthUserEvent event) {
        var userId = event.getUserId();
        profileRepository.deleteByUserId(userId);
        addressRepository.deleteByUserId(userId);
        settingsRepository.deleteByUserId(userId);
        followRepository.deleteByFollowerIdOrFolloweeId(userId, userId);
        log.info("Cascade-deleted user_service data for deleted user {}", userId);
    }

    private void handleUserDeactivated(AuthUserEvent event) {
        // Deactivation doesn't delete data, but downstream features (e.g. showing
        // this user in search/follow suggestions) should treat them as hidden.
        // Since is_active isn't mirrored locally, that check happens by calling
        // auth_service at read time, or by a separate "hidden" flag if this
        // service later needs it without a network round-trip.
        log.info("User {} deactivated upstream — no local rows removed.", event.getUserId());
    }

    private void markProcessed(AuthUserEvent event) {
        processedEventRepository.save(ProcessedEvent.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType().name())
                .source("auth_service")
                .build());
    }
}
