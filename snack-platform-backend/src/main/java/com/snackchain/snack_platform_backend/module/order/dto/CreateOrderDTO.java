package com.snackchain.snack_platform_backend.module.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建订单DTO
 */
@Data
public class CreateOrderDTO {
    
    /**
     * 门店ID
     */
    @NotNull(message = "门店ID不能为空")
    private Long storeId;
    
    /**
     * 订单项列表
     */
    @NotEmpty(message = "订单项不能为空")
    @Valid
    private List<OrderItemDTO> items;
    
    /**
     * 备注
     */
    private String remark;
}