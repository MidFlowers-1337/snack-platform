package com.snackchain.snack_platform_backend.module.auth.service;

import com.snackchain.snack_platform_backend.module.auth.dto.ChangePasswordDTO;
import com.snackchain.snack_platform_backend.module.auth.dto.LoginDTO;
import com.snackchain.snack_platform_backend.module.auth.dto.RegisterDTO;
import com.snackchain.snack_platform_backend.module.auth.vo.LoginVO;
import com.snackchain.snack_platform_backend.module.auth.vo.UserVO;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户注册
     */
    LoginVO register(RegisterDTO dto);
    
    /**
     * 用户登录
     */
    LoginVO login(LoginDTO dto);
    
    /**
     * 获取当前用户信息
     */
    UserVO getCurrentUser();
    
    /**
     * 更新用户信息
     */
    void updateProfile(String nickname, String phone, String avatar);

    /**
     * 修改密码
     */
    void changePassword(ChangePasswordDTO dto);
}