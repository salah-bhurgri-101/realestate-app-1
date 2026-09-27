package com.realestate.auth_service.controller;

import com.realestate.auth_service.dto.request.*;
import com.realestate.auth_service.dto.response.*;
import com.realestate.auth_service.dto.response.ApiResponse;
import com.realestate.auth_service.dto.response.AuthResponse;
import com.realestate.auth_service.dto.response.MeResponse;
import com.realestate.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── POST /api/v1/auth/register ────────────────────────────────────────────
    // Public — no auth required
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        authService.register(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful. Please check your email for the OTP."));
    }

    // ── POST /api/v1/auth/otp/verify ──────────────────────────────────────────
    // Public
    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request, HttpServletRequest httpRequest) {
        authService.verifyOtp(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Email verified successfully. You can now log in."));
    }

    // ── POST /api/v1/auth/otp/resend ──────────────────────────────────────────
    // Public
    @PostMapping("/otp/resend")
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @Valid @RequestBody ResendOtpRequest request, HttpServletRequest httpRequest) {
        authService.resendOtp(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("OTP sent. Please check your email."));
    }

    // ── POST /api/v1/auth/login ───────────────────────────────────────────────
    // Public
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        AuthResponse data = authService.login(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login successful.")
                .data(data)
                .build());
    }

    // ── POST /api/v1/auth/token/refresh ──────────────────────────────────────
    // Public (refresh token in body)
    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        AuthResponse data = authService.refreshToken(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Token refreshed.")
                .data(data)
                .build());
    }

    // ── POST /api/v1/auth/logout ──────────────────────────────────────────────
    // Protected — requires valid access token
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        authService.logout(authorization, request.getRefreshToken(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully."));
    }

    // ── POST /api/v1/auth/logout/all ─────────────────────────────────────────
    // Protected — requires valid access token
    @PostMapping("/logout/all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest) {
        authService.logoutAll(authorization, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Logged out from all devices."));
    }

    // ── GET /api/v1/auth/me ───────────────────────────────────────────────────
    // Protected
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> getMe(@AuthenticationPrincipal UUID userId) {
        MeResponse data = authService.getMe(userId);
        return ResponseEntity.ok(ApiResponse.<MeResponse>builder()
                .success(true)
                .message("User profile fetched.")
                .data(data)
                .build());
    }

    // ── POST /api/v1/auth/password/forgot ─────────────────────────────────────
    // Public — always returns 200 (never reveals email existence)
    @PostMapping("/password/forgot")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        authService.forgotPassword(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("If that email is registered, you will receive a password reset OTP."));
    }

    // ── POST /api/v1/auth/password/reset ──────────────────────────────────────
    // Public
    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {
        authService.resetPassword(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Password reset successful. Please log in with your new password."));
    }

    // ── PATCH /api/v1/auth/password/change ────────────────────────────────────
    // Protected
    @PatchMapping("/password/change")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        authService.changePassword(userId, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully. Please log in again on all devices."));
    }
}
