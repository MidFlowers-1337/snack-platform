package com.snackchain.snack_platform_backend.common.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 安全配置验证
 * 在应用启动时检查必要的安全配置是否已正确设置
 */
@Slf4j
@Configuration
public class SecurityValidationConfig {

    private final Environment environment;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${spring.datasource.url:}")
    private String dbUrl;

    @Value("${spring.datasource.username:}")
    private String dbUsername;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    public SecurityValidationConfig(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validateSecurityConfiguration() {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        boolean isDevProfile = activeProfiles.contains("dev");
        
        List<String> missingConfigs = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 检查 JWT 密钥
        if (jwtSecret == null || jwtSecret.isBlank()) {
            if (isDevProfile) {
                warnings.add("JWT_SECRET 未设置，使用开发环境默认值");
            } else {
                missingConfigs.add("JWT_SECRET");
            }
        } else if (jwtSecret.length() < 32) {
            warnings.add("JWT_SECRET 长度不足32字符，建议使用更长的密钥以提高安全性");
        }

        // 检查数据库配置
        if (dbUrl == null || dbUrl.isBlank()) {
            if (isDevProfile) {
                warnings.add("DB_URL 未设置，使用开发环境默认值");
            } else {
                missingConfigs.add("DB_URL");
            }
        }

        if (dbUsername == null || dbUsername.isBlank()) {
            if (isDevProfile) {
                warnings.add("DB_USERNAME 未设置，使用开发环境默认值");
            } else {
                missingConfigs.add("DB_USERNAME");
            }
        }

        if (dbPassword == null || dbPassword.isBlank()) {
            if (isDevProfile) {
                warnings.add("DB_PASSWORD 未设置，使用开发环境默认值");
            } else {
                missingConfigs.add("DB_PASSWORD");
            }
        }

        // 输出警告
        for (String warning : warnings) {
            log.warn("⚠️  安全配置警告: {}", warning);
        }

        // 如果有缺失的必要配置，在非开发环境下抛出异常
        if (!missingConfigs.isEmpty() && !isDevProfile) {
            String errorMessage = String.format(
                "❌ 安全配置错误: 以下必要的环境变量未设置: %s。" +
                "请设置这些环境变量或使用 -Dspring.profiles.active=dev 启用开发模式。",
                String.join(", ", missingConfigs)
            );
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        // 输出安全配置状态
        if (isDevProfile) {
            log.info("🔧 开发模式已启用 - 使用开发环境默认配置");
        } else {
            log.info("✅ 安全配置验证通过");
        }
    }
}