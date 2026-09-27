package com.realestate.auth_service.service;

import com.realestate.auth_service.entity.AuditAction;
import com.realestate.auth_service.entity.AuditLog;
import com.realestate.auth_service.entity.User;
import com.realestate.auth_service.repository.AuditLogRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Async + new transaction so a failure in the main flow never
     * blocks the audit log, and vice-versa.
     */
    @Async
    //    @Transactional(propagation = Propagation.REQUIRES_NEW) // because of issues 1. user not add first, 2.(For
    // Mysql) leak memory
    public void log(User user, AuditAction action, String ip, String userAgent, String detail) {
        try {
            AuditLog entry = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .ipAddress(ip)
                    .userAgent(userAgent)
                    .detail(detail)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error(
                    "Failed to write audit log action={} userId={}: {}",
                    action,
                    user != null ? user.getId() : "null",
                    e.getMessage());
        }
    }

    /** Overload — no user (e.g. failed login for unknown email) */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAnonymous(AuditAction action, String ip, String userAgent, String detail) {
        try {
            AuditLog entry = AuditLog.builder()
                    .action(action)
                    .ipAddress(ip)
                    .userAgent(userAgent)
                    .detail(detail)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write anonymous audit log action={}: {}", action, e.getMessage());
        }
    }

    /** Runs every Sunday at 03:00 AM — delete logs older than 90 days (GDPR). */
    @Scheduled(cron = "0 0 3 * * SUN")
    @Transactional
    public void cleanupOldLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
        int deleted = auditLogRepository.deleteOlderThan(cutoff);
        log.info("Audit log cleanup: deleted {} entries older than {}", deleted, cutoff);
    }
}
