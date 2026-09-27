package com.example.userservice.controller;

import com.example.userservice.dto.request.UpdateSettingsRequest;
import com.example.userservice.dto.response.SettingsResponse;
import com.example.userservice.service.UserSettingsService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/settings")
@RequiredArgsConstructor
public class UserSettingsController {

    private final UserSettingsService settingsService;

    @GetMapping
    public ResponseEntity<SettingsResponse> getSettings(@PathVariable UUID userId) {
        return ResponseEntity.ok(settingsService.getSettings(userId));
    }

    @PatchMapping
    public ResponseEntity<SettingsResponse> updateSettings(
            @PathVariable UUID userId, @Valid @RequestBody UpdateSettingsRequest request) {
        return ResponseEntity.ok(settingsService.updateSettings(userId, request));
    }
}
