package com.snackchain.snack_platform_backend.security.context;

import com.snackchain.snack_platform_backend.enums.UserRole;
import lombok.Data;

/**
 * 用户上下文信息
 */
@Data
public class UserContext {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 角色
     */
    private UserRole role;
    
    /**
     * 门店ID（仅门店管理员有值）
     */
    private Long storeId;
    
    /**
     * 是否为消费者
     */
    public boolean isConsumer() {
        return UserRole.CONSUMER.equals(role);
    }
    
    /**
     * 是否为门店管理员
     */
    public boolean isStoreAdmin() {
        return UserRole.STORE_ADMIN.equals(role);
    }
    
    /**
     * 是否为系统管理员
     */
    public boolean isSystemAdmin() {
        return UserRole.SYSTEM_ADMIN.equals(role);
    }
}