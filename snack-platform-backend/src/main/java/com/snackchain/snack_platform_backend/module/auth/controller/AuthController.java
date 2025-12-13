package com.snackchain.snack_platform_backend.module.auth.controller;

import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.module.auth.dto.LoginDTO;
import com.snackchain.snack_platform_backend.module.auth.dto.RegisterDTO;
import com.snackchain.snack_platform_backend.module.auth.dto.UpdateProfileDTO;
import com.snackchain.snack_platform_backend.module.auth.service.AuthService;
import com.snackchain.snack_platform_backend.module.auth.vo.LoginVO;
import com.snackchain.snack_platform_backend.module.auth.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        LoginVO vo = authService.register(dto);
        return Result.success(vo);
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = authService.login(dto);
        return Result.success(vo);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<UserVO> getProfile() {
        UserVO vo = authService.getCurrentUser();
        return Result.success(vo);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UpdateProfileDTO dto) {
        authService.updateProfile(dto.getNickname(), dto.getPhone(), dto.getAvatar());
        return Result.success();
    }
}