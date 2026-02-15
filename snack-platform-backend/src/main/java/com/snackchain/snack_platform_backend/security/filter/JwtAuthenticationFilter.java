package com.snackchain.snack_platform_backend.security.filter;

import com.snackchain.snack_platform_backend.common.service.TokenBlacklistService;
import com.snackchain.snack_platform_backend.common.util.JwtUtil;
import com.snackchain.snack_platform_backend.enums.UserRole;
import com.snackchain.snack_platform_backend.security.context.UserContext;
import com.snackchain.snack_platform_backend.security.context.UserContextHolder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // 检查 Token 是否在黑名单中（登出/修改密码后失效）
                if (tokenBlacklistService.isBlacklisted(token)) {
                    log.debug("Token 已被拉黑，拒绝认证");
                    filterChain.doFilter(request, response);
                    return;
                }

                // 解析Token信息（仅解析一次，避免重复解析 4 次的性能问题）
                Claims claims = jwtUtil.parseToken(token);
                Long userId = claims.get("userId", Long.class);
                String username = claims.getSubject();
                String roleStr = claims.get("role", String.class);
                Long storeId = claims.get("storeId", Long.class);
                
                UserRole role = UserRole.valueOf(roleStr);
                
                // 设置用户上下文
                UserContext userContext = new UserContext();
                userContext.setUserId(userId);
                userContext.setUsername(username);
                userContext.setRole(role);
                userContext.setStoreId(storeId);
                UserContextHolder.set(userContext);
                
                // 设置Spring Security认证信息
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        username, 
                        null, 
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleStr))
                    );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("用户认证成功: userId={}, username={}, role={}", userId, username, roleStr);
            }
            
            filterChain.doFilter(request, response);
        } finally {
            // 清理用户上下文
            UserContextHolder.clear();
        }
    }
    
    /**
     * 从请求头中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}