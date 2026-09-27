package com.realestate.auth_service.event;

import com.realestate.auth_service.entity.Role;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthUserEvent {
    private final String eventId;
    private final String eventType;
    private final UUID userId;
    private final String email;
    private final String name;
    private final Role role;
    private final String displayName;
    private final String bio;
    private final String avatarUrl;
    private final String coverUrl;
    private final LocalDate dateOfBirth;
    private final String gender;
    private final String phone;
    private final LocalDateTime occurredAt;
}
