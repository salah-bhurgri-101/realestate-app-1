package com.realestate.auth_service.dto.response;

import lombok.*;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UserResponse user; // null on token refresh
}
