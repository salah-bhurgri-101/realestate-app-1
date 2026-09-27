package com.example.userservice.controller;

import com.example.userservice.dto.request.UpsertProfileRequest;
import com.example.userservice.dto.response.ProfileResponse;
import com.example.userservice.service.UserProfileService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> upsertProfile(
            @PathVariable UUID userId, @Valid @RequestBody UpsertProfileRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(profileService.upsertProfile(userId, request));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProfile(@PathVariable UUID userId) {
        profileService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }
}
