package com.realestate.auth_service.controller;

import com.realestate.auth_service.dto.request.AdminRoleRequest;
import com.realestate.auth_service.dto.request.AdminStatusRequest;
import com.realestate.auth_service.dto.response.UserResponse;
import com.realestate.auth_service.entity.Role;
import com.realestate.auth_service.service.AdminUserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public List<UserResponse> list(
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return adminUserService.listUsers(role, page, Math.min(size, 100));
    }

    @PatchMapping("/{userId}/status")
    public UserResponse setStatus(@PathVariable UUID userId, @Valid @RequestBody AdminStatusRequest request) {
        return adminUserService.setStatus(userId, request.getActive());
    }

    @PatchMapping("/{userId}/role")
    public UserResponse setRole(@PathVariable UUID userId, @Valid @RequestBody AdminRoleRequest request) {
        return adminUserService.setRole(userId, request.getRole());
    }
}
