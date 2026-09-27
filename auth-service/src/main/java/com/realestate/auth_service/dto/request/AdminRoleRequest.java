package com.realestate.auth_service.dto.request;

import com.realestate.auth_service.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminRoleRequest {
    @NotNull private Role role;
}
