package com.snackchain.snack_platform_backend.module.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单项DTO
 */
@Data
public class OrderItemDTO {
    
    /**
     * 门店商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long skuId;
    
    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
}