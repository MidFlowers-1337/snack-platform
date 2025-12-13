package com.snackchain.snack_platform_backend.module.sku.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 门店商品请求DTO
 */
@Data
public class StoreSkuDTO {
    
    /**
     * 平台商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    
    /**
     * 门店售价
     */
    @Positive(message = "价格必须大于0")
    private BigDecimal price;
    
    /**
     * 库存数量
     */
    @PositiveOrZero(message = "库存不能为负数")
    private Integer stock;
    
    /**
     * 状态：1-上架，0-下架
     */
    private Integer status;
}