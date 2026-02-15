package com.snackchain.snack_platform_backend.module.auth.controller;

import com.snackchain.snack_platform_backend.common.annotation.OperationLog;
import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.common.service.LoginAttemptService;
import com.snackchain.snack_platform_backend.common.service.TokenBlacklistService;
import com.snackchain.snack_platform_backend.common.util.JwtUtil;
import com.snackchain.snack_platform_backend.module.auth.dto.ChangePasswordDTO;
import com.snackchain.snack_platform_backend.module.auth.dto.LoginDTO;
import com.snackchain.snack_platform_backend.module.auth.dto.RegisterDTO;
import com.snackchain.snack_platform_backend.module.auth.dto.UpdateProfileDTO;
import com.snackchain.snack_platform_backend.module.auth.service.AuthService;
import com.snackchain.snack_platform_backend.module.auth.vo.LoginVO;
import com.snackchain.snack_platform_backend.module.auth.vo.UserVO;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理")
public class AuthController {
    
    private final AuthService authService;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;
    
    /**
     * 用户注册
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    @OperationLog(module = "认证模块", operation = "用户注册")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        LoginVO vo = authService.register(dto);
        return Result.success(vo);
    }
    
    /**
     * 用户登录（含暴力破解防护）
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    @OperationLog(module = "认证模块", operation = "用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        // 检查账户是否被锁定
        if (loginAttemptService.isLocked(dto.getUsername())) {
            long remaining = loginAttemptService.getRemainingLockTime(dto.getUsername());
            return Result.error(429, "账户已被临时锁定，请 " + (remaining / 60 + 1) + " 分钟后重试");
        }
        try {
            LoginVO vo = authService.login(dto);
            loginAttemptService.clearAttempts(dto.getUsername());
            return Result.success(vo);
        } catch (Exception e) {
            loginAttemptService.recordFailedAttempt(dto.getUsername());
            throw e;
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/profile")
    public Result<UserVO> getProfile() {
        UserVO vo = authService.getCurrentUser();
        return Result.success(vo);
    }
    
    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        authService.updateProfile(dto.getNickname(), dto.getPhone(), dto.getAvatar());
        return Result.success();
    }

    /**
     * 修改密码（修改后将当前 Token 加入黑名单）
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    @OperationLog(module = "认证模块", operation = "修改密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto,
                                       HttpServletRequest request) {
        authService.changePassword(dto);
        // 修改密码后，将当前 Token 加入黑名单
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.parseToken(token);
                long expiration = claims.getExpiration().getTime() - System.currentTimeMillis();
                tokenBlacklistService.blacklist(token, expiration);
            } catch (Exception ignored) {
                // Token 解析失败则忽略
            }
        }
        return Result.success();
    }
}