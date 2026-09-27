package com.realestate.auth_service.dto.response;

import java.util.List;
import lombok.*;

@Data
@Builder
public class MeResponse {
    private UserResponse user;
    private List<SessionResponse> activeSessions;
}
