package com.realestate.auth_service.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    private final StringRedisTemplate redisTemplate;

    /**
     * Add JWT jti to blacklist with TTL equal to the token's remaining lifetime.
     * After TTL the key auto-expires — no manual cleanup needed.
     */
    public void blacklist(String jti, long remainingTtlMs) {
        if (remainingTtlMs <= 0) return;
        String key = BLACKLIST_PREFIX + jti;
        redisTemplate.opsForValue().set(key, "1", Duration.ofMillis(remainingTtlMs));
        log.debug("Blacklisted token jti={} ttl={}ms", jti, remainingTtlMs);
    }

    /**
     * Returns true if the jti has been blacklisted (token is revoked).
     */
    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
    }
}
