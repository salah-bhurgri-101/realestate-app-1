package com.example.userservice.dto.response;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse implements Serializable {
    private UUID id;
    private UUID userId;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private String coverUrl;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
