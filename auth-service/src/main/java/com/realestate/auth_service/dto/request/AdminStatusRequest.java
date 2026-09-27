package com.realestate.auth_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminStatusRequest {
    @NotNull private Boolean active;
}
