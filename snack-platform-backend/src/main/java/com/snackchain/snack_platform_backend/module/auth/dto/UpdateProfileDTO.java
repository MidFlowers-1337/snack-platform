package com.snackchain.snack_platform_backend.module.auth.dto;

import lombok.Data;

/**
 * 更新用户信息DTO
 */
@Data
public class UpdateProfileDTO {
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 头像URL
     */
    private String avatar;
}