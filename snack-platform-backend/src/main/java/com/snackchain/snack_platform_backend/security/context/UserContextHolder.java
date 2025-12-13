package com.snackchain.snack_platform_backend.security.context;

/**
 * 用户上下文持有者（基于ThreadLocal）
 */
public class UserContextHolder {
    
    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();
    
    /**
     * 设置当前用户上下文
     */
    public static void set(UserContext userContext) {
        CONTEXT.set(userContext);
    }
    
    /**
     * 获取当前用户上下文
     */
    public static UserContext get() {
        return CONTEXT.get();
    }
    
    /**
     * 清除当前用户上下文
     */
    public static void clear() {
        CONTEXT.remove();
    }
    
    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        UserContext context = get();
        return context != null ? context.getUserId() : null;
    }
    
    /**
     * 获取当前门店ID
     */
    public static Long getStoreId() {
        UserContext context = get();
        return context != null ? context.getStoreId() : null;
    }
}