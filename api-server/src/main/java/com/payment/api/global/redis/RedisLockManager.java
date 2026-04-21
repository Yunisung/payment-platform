package com.payment.api.global.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockManager {

    private static final String LOCK_PREFIX = "lock:payment:";
    private static final Duration LOCK_TTL = Duration.ofSeconds(10);

    private final StringRedisTemplate redisTemplate;

    public boolean tryLock(String idempotencyKey) {
        String lockKey = LOCK_PREFIX + idempotencyKey;
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "locked", LOCK_TTL);
        return Boolean.TRUE.equals(acquired);
    }

    public void unlock(String idempotencyKey) {
        String lockKey = LOCK_PREFIX + idempotencyKey;
        redisTemplate.delete(lockKey);
    }
}
