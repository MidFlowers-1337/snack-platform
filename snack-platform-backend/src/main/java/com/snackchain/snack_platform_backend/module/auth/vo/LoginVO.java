package com.snackchain.snack_platform_backend.module.auth.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应VO
 */
@Data
@Builder
public class LoginVO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 角色
     */
    private String role;
    
    /**
     * 门店ID（门店管理员有值）
     */
    private Long storeId;
    
    /**
     * JWT Token
     */
    private String token;
}