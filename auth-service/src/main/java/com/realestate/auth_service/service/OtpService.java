package com.realestate.auth_service.service;

import com.realestate.auth_service.entity.Otp;
import com.realestate.auth_service.entity.OtpType;
import com.realestate.auth_service.entity.User;
import com.realestate.auth_service.exception.*;
import com.realestate.auth_service.repository.OtpRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private static final String RATE_LIMIT_PREFIX = "otp:rate:";
    private static final int MAX_RESEND = 3;
    private static final int MAX_ATTEMPTS = 5;

    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    @Value("${auth.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${auth.otp.rate-limit-minutes:15}")
    private int rateLimitMinutes;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final SecureRandom secureRandom = new SecureRandom();

    // ── generate + send ───────────────────────────────────────────────────────

    @Transactional
    public void generateAndSend(User user, OtpType type) {
        // delete any existing OTPs of the same type for this user
        otpRepository.deleteAllByUserIdAndType(user.getId(), type);

        String code = generateCode();

        Otp otp = Otp.builder()
                .user(user)
                .code(code)
                .type(type)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .build();

        otpRepository.save(otp);
        sendOtpEmail(user.getEmail(), user.getName(), code, type);

        log.info("OTP generated for userId={} type={}", user.getId(), type);
    }

    // ── verify ────────────────────────────────────────────────────────────────

    @Transactional
    public void verify(User user, String code, OtpType type) {
        Otp otp = otpRepository
                .findLatestValidByUserIdAndType(user.getId(), type)
                .orElseThrow(() -> new OtpNotFoundException("No valid OTP found. Please request a new one."));

        if (otp.getAttempts() >= MAX_ATTEMPTS) {
            throw new OtpMaxAttemptsException("Too many failed attempts. Please request a new OTP.");
        }

        if (!otp.getCode().equals(code)) {
            otpRepository.incrementAttempts(otp.getId());
            int remaining = MAX_ATTEMPTS - otp.getAttempts() - 1;
            throw new InvalidOtpException("Invalid OTP. " + remaining + " attempts remaining.");
        }

        if (otp.isExpired()) {
            throw new OtpExpiredException("OTP has expired. Please request a new one.");
        }

        // mark all OTPs of this type as used
        otpRepository.markAllUsedByUserIdAndType(user.getId(), type);
        log.info("OTP verified for userId={} type={}", user.getId(), type);
    }

    // ── resend ────────────────────────────────────────────────────────────────

    @Transactional
    public void resend(User user, OtpType type) {
        String rateLimitKey = RATE_LIMIT_PREFIX + user.getId() + ":" + type.name();
        String countStr = redisTemplate.opsForValue().get(rateLimitKey);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;

        if (count >= MAX_RESEND) {
            throw new RateLimitException(
                    "Maximum OTP resend limit reached. Try again after " + rateLimitMinutes + " minutes.");
        }

        // increment rate-limit counter (set TTL on first entry)
        if (count == 0) {
            redisTemplate.opsForValue().set(rateLimitKey, "1", Duration.ofMinutes(rateLimitMinutes));
        } else {
            redisTemplate.opsForValue().increment(rateLimitKey);
        }

        generateAndSend(user, type);
    }

    // ── email dispatch (async so login/register don't block) ─────────────────

    @Async
    protected void sendOtpEmail(String to, String name, String code, OtpType type) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(to);
            msg.setSubject(subjectFor(type));
            msg.setText(bodyFor(name, code, type, otpExpiryMinutes));
            mailSender.send(msg);
            log.debug("OTP email sent to={} type={}", to, type);
        } catch (Exception e) {
            log.error("Failed to send OTP email to={} type={}: {}", to, type, e.getMessage());
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String generateCode() {
        int code = 100_000 + secureRandom.nextInt(900_000);
        return String.valueOf(code);
    }

    private String subjectFor(OtpType type) {
        return switch (type) {
            case REGISTRATION -> "Verify your email";
            case PASSWORD_RESET -> "Reset your password";
            case LOGIN_2FA -> "Your login verification code";
            case EMAIL_CHANGE -> "Confirm your new email";
            case PHONE_CHANGE -> "Confirm your new phone";
        };
    }

    private String bodyFor(String name, String code, OtpType type, int expiryMinutes) {
        return switch (type) {
            case REGISTRATION ->
                String.format(
                        "Hi %s,\n\nYour verification code is: %s\n\nThis code expires in %d minutes.\n\nIf you did not create an account, please ignore this email.",
                        name, code, expiryMinutes);
            case PASSWORD_RESET ->
                String.format(
                        "Hi %s,\n\nYour password reset code is: %s\n\nThis code expires in %d minutes.\n\nIf you did not request this, please ignore this email.",
                        name, code, expiryMinutes);
            default ->
                String.format(
                        "Hi %s,\n\nYour verification code is: %s\n\nThis code expires in %d minutes.",
                        name, code, expiryMinutes);
        };
    }
}
