package com.example.userservice.event;

import com.example.userservice.domain.enums.AuthEventType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload contract for events published by auth_service on topic
 * "auth.user.events". eventId is the idempotency key (checked against
 * processed_events before applying).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthUserEvent {
    private String eventId;
    private AuthEventType eventType;
    private UUID userId;
    private String email;
    private String name;
    private String role;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private String coverUrl;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private LocalDateTime occurredAt;
}
