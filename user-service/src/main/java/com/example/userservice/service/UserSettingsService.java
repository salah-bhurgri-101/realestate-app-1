package com.example.userservice.service;

import com.example.userservice.config.RedisCacheConfig;
import com.example.userservice.domain.UserSettings;
import com.example.userservice.domain.enums.ProfileVisibility;
import com.example.userservice.domain.enums.Theme;
import com.example.userservice.dto.request.UpdateSettingsRequest;
import com.example.userservice.dto.response.SettingsResponse;
import com.example.userservice.repository.UserSettingsRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserSettingsRepository settingsRepository;

    @Cacheable(value = RedisCacheConfig.SETTINGS_CACHE, key = "#userId")
    @Transactional(readOnly = true)
    public SettingsResponse getSettings(UUID userId) {
        UserSettings settings = settingsRepository.findByUserId(userId).orElseGet(() -> defaultSettings(userId));
        return toResponse(settings);
    }

    @CacheEvict(value = RedisCacheConfig.SETTINGS_CACHE, key = "#userId")
    @Transactional
    public SettingsResponse updateSettings(UUID userId, UpdateSettingsRequest request) {
        UserSettings settings = settingsRepository
                .findByUserId(userId)
                .orElseGet(() -> UserSettings.builder().userId(userId).build());

        if (request.getLanguage() != null) settings.setLanguage(request.getLanguage());
        if (request.getTimezone() != null) settings.setTimezone(request.getTimezone());
        if (request.getTheme() != null) settings.setTheme(request.getTheme());
        if (request.getProfileVisibility() != null) settings.setProfileVisibility(request.getProfileVisibility());
        if (request.getEmailNotifications() != null) settings.setEmailNotifications(request.getEmailNotifications());
        if (request.getPushNotifications() != null) settings.setPushNotifications(request.getPushNotifications());
        if (request.getSmsNotifications() != null) settings.setSmsNotifications(request.getSmsNotifications());

        return toResponse(settingsRepository.save(settings));
    }

    @CacheEvict(value = RedisCacheConfig.SETTINGS_CACHE, key = "#userId")
    @Transactional
    public void deleteSettings(UUID userId) {
        settingsRepository.deleteByUserId(userId);
    }

    private UserSettings defaultSettings(UUID userId) {
        // Not persisted here — just a view-time default until the user saves real preferences.
        return UserSettings.builder()
                .userId(userId)
                .language("en")
                .timezone("UTC")
                .theme(Theme.SYSTEM)
                .profileVisibility(ProfileVisibility.PUBLIC)
                .emailNotifications(true)
                .pushNotifications(true)
                .smsNotifications(false)
                .build();
    }

    private SettingsResponse toResponse(UserSettings s) {
        return SettingsResponse.builder()
                .id(s.getId())
                .userId(s.getUserId())
                .language(s.getLanguage())
                .timezone(s.getTimezone())
                .theme(s.getTheme())
                .profileVisibility(s.getProfileVisibility())
                .emailNotifications(s.isEmailNotifications())
                .pushNotifications(s.isPushNotifications())
                .smsNotifications(s.isSmsNotifications())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
