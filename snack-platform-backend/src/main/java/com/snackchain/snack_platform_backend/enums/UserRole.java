package com.snackchain.snack_platform_backend.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRole {
    CONSUMER("消费者"),
    STORE_ADMIN("门店管理员"),
    SYSTEM_ADMIN("系统管理员");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }
}