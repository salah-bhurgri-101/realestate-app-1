package com.realestate.auth_service.service;

import com.realestate.auth_service.entity.*;
import com.realestate.auth_service.entity.AuditAction;
import com.realestate.auth_service.entity.DeviceType;
import com.realestate.auth_service.entity.RefreshToken;
import com.realestate.auth_service.entity.User;
import com.realestate.auth_service.exception.*;
import com.realestate.auth_service.exception.InvalidTokenException;
import com.realestate.auth_service.exception.TokenExpiredException;
import com.realestate.auth_service.exception.TokenReplayException;
import com.realestate.auth_service.exception.UserNotFoundException;
import com.realestate.auth_service.repository.RefreshTokenRepository;
import com.realestate.auth_service.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final SessionService sessionService;
    private final AuditService auditService;

    @Value("${auth.refresh-token.expiry-days:30}")
    private int refreshTokenExpiryDays;

    // ── issue access token ────────────────────────────────────────────────────

    public String issueAccessToken(User user) {
        return jwtService.generateAccessToken(user);
    }

    // ── issue refresh token ───────────────────────────────────────────────────

    @Transactional
    public String issueRefreshToken(User user, HttpServletRequest request) {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = sha256(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .deviceName(resolveDeviceName(request))
                .deviceType(resolveDeviceType(request))
                .ipAddress(resolveIp(request))
                .userAgent(resolveUserAgent(request))
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpiryDays))
                .build();

        refreshTokenRepository.save(refreshToken);
        sessionService.createSession(user, refreshToken, request);

        log.info("Refresh token issued for userId={}", user.getId());
        return rawToken; // return raw token to client — hash stays in DB
    }

    // ── rotate refresh token ──────────────────────────────────────────────────

    @Transactional
    public TokenPair rotateRefreshToken(String rawRefreshToken, HttpServletRequest request) {
        String tokenHash = sha256(rawRefreshToken);

        // replay attack: token exists but already revoked
        if (refreshTokenRepository.existsRevokedByTokenHash(tokenHash)) {
            String userId = refreshTokenRepository
                    .findByTokenHash(tokenHash)
                    .map(rt -> rt.getUser().getId().toString())
                    .orElse("unknown");
            log.warn("Replay attack detected for userId={}", userId);

            // revoke ALL sessions for this user
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(rt -> {
                refreshTokenRepository.revokeAllByUserId(rt.getUser().getId(), LocalDateTime.now());
                sessionService.deleteAllByUserId(rt.getUser().getId());
                auditService.log(
                        rt.getUser(),
                        AuditAction.TOKEN_REPLAY_DETECTED,
                        resolveIp(request),
                        resolveUserAgent(request),
                        null);
            });
            throw new TokenReplayException("Security alert: token reuse detected. All sessions have been terminated.");
        }

        RefreshToken existing = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found."));

        if (existing.isExpired()) {
            throw new TokenExpiredException("Refresh token has expired. Please log in again.");
        }

        if (existing.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked.");
        }

        // revoke old token
        existing.revoke();
        refreshTokenRepository.save(existing);

        // delete old session
        sessionService.deleteByRefreshTokenId(existing.getId());

        // load user fresh (tokenVersion may have changed)
        User user = userRepository
                .findById(existing.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        // issue new pair
        String newAccessToken = issueAccessToken(user);
        String newRawRefreshToken = issueRefreshToken(user, request);

        auditService.log(user, AuditAction.TOKEN_REFRESHED, resolveIp(request), resolveUserAgent(request), null);

        log.info("Refresh token rotated for userId={}", user.getId());
        return new TokenPair(newAccessToken, newRawRefreshToken);
    }

    // ── revoke single token (logout) ──────────────────────────────────────────

    @Transactional
    public void revokeRefreshToken(String rawRefreshToken) {
        String tokenHash = sha256(rawRefreshToken);
        int updated = refreshTokenRepository.revokeByTokenHash(tokenHash, LocalDateTime.now());
        if (updated == 0) {
            log.warn("Attempted to revoke non-existent or already-revoked refresh token");
        }
    }

    // ── revoke all tokens (logout-all) ────────────────────────────────────────

    @Transactional
    public void revokeAllTokens(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId, LocalDateTime.now());
        userRepository.incrementTokenVersion(userId);
        sessionService.deleteAllByUserId(userId);
        log.info("All tokens revoked for userId={}", userId);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    public String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }

    private String resolveUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    private String resolveDeviceName(HttpServletRequest request) {
        String ua = resolveUserAgent(request);
        if (ua == null) return "Unknown";
        if (ua.contains("Mobile")) return "Mobile Browser";
        if (ua.contains("Tablet")) return "Tablet Browser";
        return "Desktop Browser";
    }

    private DeviceType resolveDeviceType(HttpServletRequest request) {
        String ua = resolveUserAgent(request);
        if (ua == null) return DeviceType.UNKNOWN;
        if (ua.contains("Android")) return DeviceType.ANDROID;
        if (ua.contains("iPhone") || ua.contains("iPad")) return DeviceType.IOS;
        if (ua.contains("Mobile")) return DeviceType.ANDROID;
        return DeviceType.WEB;
    }

    // ── token pair record ─────────────────────────────────────────────────────

    public record TokenPair(String accessToken, String refreshToken) {}
}
