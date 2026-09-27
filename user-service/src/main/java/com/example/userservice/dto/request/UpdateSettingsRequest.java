package com.example.userservice.dto.request;

import com.example.userservice.domain.enums.ProfileVisibility;
import com.example.userservice.domain.enums.Theme;
import jakarta.validation.constraints.Size;
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
public class UpdateSettingsRequest {

    @Size(max = 10) private String language;

    @Size(max = 50) private String timezone;

    private Theme theme;

    private ProfileVisibility profileVisibility;

    private Boolean emailNotifications;

    private Boolean pushNotifications;

    private Boolean smsNotifications;
}
