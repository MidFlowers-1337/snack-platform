package com.snackchain.snack_platform_backend.security.annotation;

import com.snackchain.snack_platform_backend.enums.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限注解
 * 用于标注接口需要的角色权限
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    
    /**
     * 允许访问的角色列表
     */
    UserRole[] value();
}