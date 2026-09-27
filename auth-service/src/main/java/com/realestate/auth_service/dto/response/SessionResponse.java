package com.realestate.auth_service.dto.response;

import com.realestate.auth_service.entity.Session;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Data
@Builder
public class SessionResponse {
    private UUID id;
    private String deviceName;
    private String deviceType;
    private String ipAddress;
    private LocalDateTime lastActiveAt;
    private LocalDateTime createdAt;

    public static SessionResponse from(Session s) {
        return SessionResponse.builder()
                .id(s.getId())
                .deviceName(s.getDeviceName())
                .deviceType(s.getDeviceType() != null ? s.getDeviceType().name() : null)
                .ipAddress(s.getIpAddress())
                .lastActiveAt(s.getLastActiveAt())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
