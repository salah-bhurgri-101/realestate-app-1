package com.realestate.auth_service.dto.response;

import com.realestate.auth_service.entity.User;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String phone;
    private String name;
    private String role;
    private Boolean isVerified;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static UserResponse from(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .phone(u.getPhone())
                .name(u.getName())
                .role(u.getRole().name())
                .isVerified(u.getIsVerified())
                .isActive(u.getIsActive())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
