package com.realestate.auth_service.service;

import com.realestate.auth_service.dto.request.*;
import com.realestate.auth_service.dto.response.*;
import com.realestate.auth_service.dto.response.AuthResponse;
import com.realestate.auth_service.dto.response.MeResponse;
import com.realestate.auth_service.dto.response.SessionResponse;
import com.realestate.auth_service.dto.response.UserResponse;
import com.realestate.auth_service.entity.*;
import com.realestate.auth_service.entity.AuditAction;
import com.realestate.auth_service.entity.OtpType;
import com.realestate.auth_service.entity.Session;
import com.realestate.auth_service.entity.User;
import com.realestate.auth_service.exception.*;
import com.realestate.auth_service.repository.UserRepository;
import com.realestate.auth_service.service.TokenService.TokenPair;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final TokenService tokenService;
    private final TokenBlacklistService blacklistService;
    private final SessionService sessionService;
    private final AuditService auditService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthUserEventPublisher authUserEventPublisher;

    // ── REGISTER ──────────────────────────────────────────────────────────────

    @Transactional
    public void register(RegisterRequest request, HttpServletRequest httpRequest) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already registered.");
        }
        if (StringUtils.hasText(request.getPhone()) && userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException("Phone number is already registered.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .name(request.getName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        authUserEventPublisher.publish(user, "USER_REGISTERED", request);
        otpService.generateAndSend(user, OtpType.REGISTRATION);

        auditService.log(user, AuditAction.REGISTER, resolveIp(httpRequest), resolveUserAgent(httpRequest), null);

        log.info("User registered email={}", user.getEmail());
    }

    // ── OTP VERIFY (after registration) ──────────────────────────────────────

    @Transactional
    public void verifyOtp(OtpVerifyRequest request, HttpServletRequest httpRequest) {
        User user = findUserByEmailOrThrow(request.getEmail());

        otpService.verify(user, request.getCode(), OtpType.REGISTRATION);
        userRepository.markVerified(user.getId());
        authUserEventPublisher.publish(user, "USER_UPDATED");

        auditService.log(user, AuditAction.OTP_VERIFIED, resolveIp(httpRequest), resolveUserAgent(httpRequest), null);

        log.info("User verified userId={}", user.getId());
    }

    // ── RESEND OTP ────────────────────────────────────────────────────────────

    @Transactional
    public void resendOtp(ResendOtpRequest request, HttpServletRequest httpRequest) {
        User user = findUserByEmailOrThrow(request.getEmail());

        if (user.getIsVerified()) {
            throw new BadRequestException("Account is already verified.");
        }

        otpService.resend(user, OtpType.REGISTRATION);
        auditService.log(user, AuditAction.OTP_SENT, resolveIp(httpRequest), resolveUserAgent(httpRequest), "resend");
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String ip = resolveIp(httpRequest);
        String ua = resolveUserAgent(httpRequest);

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> {
            auditService.logAnonymous(
                    AuditAction.LOGIN_FAILED,
                    ip,
                    ua,
                    "{\"reason\":\"user_not_found\",\"email\":\"" + request.getEmail() + "\"}");
            return new InvalidCredentialsException("Invalid email or password.");
        });

        if (!user.getIsVerified()) {
            throw new AccountNotVerifiedException("Please verify your email before logging in.");
        }

        if (!user.getIsActive()) {
            throw new AccountDisabledException("Your account has been disabled.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            auditService.log(user, AuditAction.LOGIN_FAILED, ip, ua, "{\"reason\":\"wrong_password\"}");
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        String accessToken = tokenService.issueAccessToken(user);
        String refreshToken = tokenService.issueRefreshToken(user, httpRequest);

        auditService.log(user, AuditAction.LOGIN_SUCCESS, ip, ua, null);
        log.info("User logged in userId={}", user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserResponse.from(user))
                .build();
    }

    // ── REFRESH TOKEN → NEW ACCESS TOKEN ──────────────────────────────────────

    public AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        TokenPair pair = tokenService.rotateRefreshToken(request.getRefreshToken(), httpRequest);
        return AuthResponse.builder()
                .accessToken(pair.accessToken())
                .refreshToken(pair.refreshToken())
                .build();
    }

    // ── LOGOUT (this device) ──────────────────────────────────────────────────

    @Transactional
    public void logout(String bearerToken, String rawRefreshToken, HttpServletRequest httpRequest) {
        String token = extractBearer(bearerToken);

        // blacklist access token in Redis
        String jti = jwtService.extractJti(token);
        long ttl = jwtService.getRemainingTtlMs(token);
        blacklistService.blacklist(jti, ttl);

        // revoke refresh token in DB
        tokenService.revokeRefreshToken(rawRefreshToken);

        // resolve user for audit
        String userId = jwtService.extractUserId(token);
        userRepository
                .findById(UUID.fromString(userId))
                .ifPresent(user -> auditService.log(
                        user, AuditAction.LOGOUT, resolveIp(httpRequest), resolveUserAgent(httpRequest), null));

        log.info("User logged out userId={}", userId);
    }

    // ── LOGOUT ALL DEVICES ────────────────────────────────────────────────────

    @Transactional
    public void logoutAll(String bearerToken, HttpServletRequest httpRequest) {
        String token = extractBearer(bearerToken);
        String userId = jwtService.extractUserId(token);

        // blacklist the current access token
        blacklistService.blacklist(jwtService.extractJti(token), jwtService.getRemainingTtlMs(token));

        // revoke all refresh tokens + increment tokenVersion
        tokenService.revokeAllTokens(UUID.fromString(userId));

        userRepository
                .findById(UUID.fromString(userId))
                .ifPresent(user -> auditService.log(
                        user, AuditAction.LOGOUT_ALL, resolveIp(httpRequest), resolveUserAgent(httpRequest), null));

        log.info("All devices logged out userId={}", userId);
    }

    // ── GET ME ────────────────────────────────────────────────────────────────

    public MeResponse getMe(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found."));

        List<Session> sessions = sessionService.getActiveSessions(userId);

        return MeResponse.builder()
                .user(UserResponse.from(user))
                .activeSessions(sessions.stream().map(SessionResponse::from).toList())
                .build();
    }

    // ── FORGOT PASSWORD ───────────────────────────────────────────────────────

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        // Always respond 200 — never reveal whether email exists
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            otpService.generateAndSend(user, OtpType.PASSWORD_RESET);
            auditService.log(
                    user,
                    AuditAction.PASSWORD_RESET_REQUESTED,
                    resolveIp(httpRequest),
                    resolveUserAgent(httpRequest),
                    null);
        });
    }

    // ── RESET PASSWORD ────────────────────────────────────────────────────────

    @Transactional
    public void resetPassword(ResetPasswordRequest request, HttpServletRequest httpRequest) {
        User user = findUserByEmailOrThrow(request.getEmail());

        otpService.verify(user, request.getOtp(), OtpType.PASSWORD_RESET);

        String newHash = passwordEncoder.encode(request.getNewPassword());
        // also bumps tokenVersion → invalidates all sessions
        userRepository.updatePasswordAndInvalidateSessions(user.getId(), newHash);

        auditService.log(
                user, AuditAction.PASSWORD_RESET_SUCCESS, resolveIp(httpRequest), resolveUserAgent(httpRequest), null);

        log.info("Password reset for userId={}", user.getId());
    }

    // ── CHANGE PASSWORD (authenticated) ──────────────────────────────────────

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from the current password.");
        }

        String newHash = passwordEncoder.encode(request.getNewPassword());
        userRepository.updatePasswordAndInvalidateSessions(userId, newHash);

        auditService.log(
                user, AuditAction.PASSWORD_CHANGED, resolveIp(httpRequest), resolveUserAgent(httpRequest), null);

        log.info("Password changed for userId={}", userId);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private User findUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));
    }

    private String extractBearer(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        throw new BadRequestException("Missing or malformed Authorization header.");
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }

    private String resolveUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
