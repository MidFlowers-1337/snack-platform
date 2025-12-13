package com.snackchain.snack_platform_backend.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatus {
    
    PENDING_PAYMENT(0, "待支付"),
    PENDING_ACCEPT(1, "待接单"),
    ACCEPTED(2, "已接单"),
    READY_FOR_PICKUP(3, "待自提"),
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消");
    
    private final Integer code;
    private final String description;
    
    OrderStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public static OrderStatus fromCode(Integer code) {
        for (OrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}