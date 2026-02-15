package com.snackchain.snack_platform_backend.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * JWT Token 黑名单服务
 *
 * What: 基于 Redis 的 Token 黑名单机制
 * Why: 用户登出、修改密码、被禁用后，旧 Token 仍有效至自然过期，存在安全风险
 * Why good: Token 加入黑名单后立即失效，TTL 与 Token 剩余有效期一致，不浪费内存
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    /**
     * 将 Token 加入黑名单
     *
     * @param token      JWT Token
     * @param expiration Token 剩余有效期（毫秒）
     */
    public void blacklist(String token, long expiration) {
        if (expiration <= 0) {
            return; // Token 已过期，无需拉黑
        }
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "1", expiration, TimeUnit.MILLISECONDS);
        log.info("Token 已加入黑名单, 剩余有效期: {}ms", expiration);
    }

    /**
     * 检查 Token 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
}
