package com.snackchain.snack_platform_backend.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.security.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;
    
    /**
     * 公开接口路径
     */
    private static final String[] PUBLIC_PATHS = {
        "/auth/register",
        "/auth/login",
        "/stores",
        "/stores/**",
        "/categories",
        "/actuator/**"
    };
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（使用JWT无需CSRF）
            .csrf(AbstractHttpConfigurer::disable)
            // 启用CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 无状态会话
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 公开接口
                .requestMatchers(PUBLIC_PATHS).permitAll()
                // 门店管理员接口
                .requestMatchers("/store/**").hasRole("STORE_ADMIN")
                // 系统管理员接口
                .requestMatchers("/admin/**").hasRole("SYSTEM_ADMIN")
                // 其他接口需要认证
                .anyRequest().authenticated()
            )
            // 异常处理
            .exceptionHandling(exception -> exception
                // 未认证处理
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                        objectMapper.writeValueAsString(Result.error(ResultCode.UNAUTHORIZED))
                    );
                })
                // 无权限处理
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                        objectMapper.writeValueAsString(Result.error(ResultCode.FORBIDDEN))
                    );
                })
            )
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * CORS配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}