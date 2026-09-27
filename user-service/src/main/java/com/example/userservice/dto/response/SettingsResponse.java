package com.example.userservice.dto.response;

import com.example.userservice.domain.enums.ProfileVisibility;
import com.example.userservice.domain.enums.Theme;
import java.io.Serializable;
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
public class SettingsResponse implements Serializable {
    private UUID id;
    private UUID userId;
    private String language;
    private String timezone;
    private Theme theme;
    private ProfileVisibility profileVisibility;
    private boolean emailNotifications;
    private boolean pushNotifications;
    private boolean smsNotifications;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
