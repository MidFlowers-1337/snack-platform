package com.snackchain.snack_platform_backend.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录暴力破解防护服务
 *
 * What: 基于 Redis 的登录失败计数 + 账户临时锁定
 * Why: 防止攻击者通过暴力穷举密码入侵账户
 * Why good: 滑动窗口计数，15 分钟内超过 5 次失败则锁定，自动解锁无需人工干预
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;
    private static final String FAILED_PREFIX = "login:failed:";

    /**
     * 记录登录失败
     */
    public void recordFailedAttempt(String username) {
        String key = FAILED_PREFIX + username;
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(key, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
        log.warn("用户 {} 登录失败, 当前失败次数: {}", username, attempts);
    }

    /**
     * 检查账户是否被锁定
     */
    public boolean isLocked(String username) {
        String key = FAILED_PREFIX + username;
        String attempts = redisTemplate.opsForValue().get(key);
        return attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPTS;
    }

    /**
     * 登录成功后清除失败记录
     */
    public void clearAttempts(String username) {
        redisTemplate.delete(FAILED_PREFIX + username);
    }

    /**
     * 获取剩余锁定时间（秒）
     */
    public long getRemainingLockTime(String username) {
        String key = FAILED_PREFIX + username;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null ? ttl : 0;
    }
}
