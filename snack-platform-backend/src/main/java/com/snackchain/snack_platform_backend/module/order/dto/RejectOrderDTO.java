package com.snackchain.snack_platform_backend.module.order.dto;

import lombok.Data;

/**
 * 拒绝订单DTO
 */
@Data
public class RejectOrderDTO {
    
    /**
     * 拒绝原因
     */
    private String reason;
}