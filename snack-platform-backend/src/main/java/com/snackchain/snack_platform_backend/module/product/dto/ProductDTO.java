package com.snackchain.snack_platform_backend.module.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品请求DTO
 */
@Data
public class ProductDTO {
    
    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
    
    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    private String name;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 商品主图URL
     */
    private String image;
    
    /**
     * 建议零售价
     */
    @NotNull(message = "价格不能为空")
    @Positive(message = "价格必须大于0")
    private BigDecimal price;
    
    /**
     * 状态：1-上架，0-下架
     */
    private Integer status;
}