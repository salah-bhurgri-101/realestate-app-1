package com.realestate.auth_service.service;

import com.realestate.auth_service.entity.*;
import com.realestate.auth_service.entity.DeviceType;
import com.realestate.auth_service.entity.RefreshToken;
import com.realestate.auth_service.entity.Session;
import com.realestate.auth_service.entity.User;
import com.realestate.auth_service.repository.SessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;

    // ── create ────────────────────────────────────────────────────────────────

    @Transactional
    public Session createSession(User user, RefreshToken refreshToken, HttpServletRequest request) {
        Session session = Session.builder()
                .user(user)
                .refreshToken(refreshToken)
                .deviceName(resolveDeviceName(request))
                .deviceType(resolveDeviceType(request))
                .ipAddress(resolveIp(request))
                .userAgent(resolveUserAgent(request))
                .build();
        return sessionRepository.save(session);
    }

    // ── touch (keep lastActiveAt current) ────────────────────────────────────

    @Transactional
    public void touch(UUID sessionId) {
        sessionRepository.touch(sessionId, LocalDateTime.now());
    }

    // ── read ──────────────────────────────────────────────────────────────────

    public List<Session> getActiveSessions(UUID userId) {
        return sessionRepository.findActiveSessionsByUserId(userId);
    }

    public long countActiveSessions(UUID userId) {
        return sessionRepository.countActiveByUserId(userId);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteByRefreshTokenId(UUID refreshTokenId) {
        sessionRepository.deleteByRefreshTokenId(refreshTokenId);
    }

    @Transactional
    public void deleteAllByUserId(UUID userId) {
        sessionRepository.deleteAllByUserId(userId);
    }

    // ── scheduled cleanup (runs every day at 02:00 AM) ────────────────────────

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupInactiveSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(31);
        int deleted = sessionRepository.deleteInactiveSessions(cutoff);
        log.info("Session cleanup: deleted {} stale sessions", deleted);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

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
        return DeviceType.WEB;
    }
}
