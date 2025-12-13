package com.snackchain.snack_platform_backend.module.auth.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 用户信息VO
 */
@Data
@Builder
public class UserVO {
    
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
     * 手机号
     */
    private String phone;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 角色
     */
    private String role;
    
    /**
     * 门店ID
     */
    private Long storeId;
}