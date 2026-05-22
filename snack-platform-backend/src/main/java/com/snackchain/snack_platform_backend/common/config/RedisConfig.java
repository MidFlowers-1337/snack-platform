package com.snackchain.snack_platform_backend.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 配置类
 *
 * What: 配置 Redis 序列化方式和 Spring Cache 管理器
 * Why: 默认 JDK 序列化不可读且效率低，改用 JSON 序列化
 * Why good: 便于调试、跨语言兼容、支持按缓存名设置不同 TTL
 */
@Configuration
public class RedisConfig {

    /**
     * 创建注册了 JavaTimeModule 的 ObjectMapper
     * 解决 GenericJackson2JsonRedisSerializer 默认 ObjectMapper 不支持 LocalDateTime 的问题
     */
    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(createJsonSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(createJsonSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        GenericJackson2JsonRedisSerializer valueSerializer = createJsonSerializer();

        // 默认缓存配置：10 分钟 TTL
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .disableCachingNullValues();

        // 为不同缓存设置不同 TTL
        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        configMap.put("categories", defaultConfig.entryTtl(Duration.ofMinutes(30)));  // 分类极少变化
        configMap.put("stores", defaultConfig.entryTtl(Duration.ofMinutes(10)));      // 门店列表
        configMap.put("products", defaultConfig.entryTtl(Duration.ofMinutes(5)));     // 商品信息
        configMap.put("storeSkus", defaultConfig.entryTtl(Duration.ofMinutes(5)));    // 门店商品

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configMap)
                .build();
    }
}
