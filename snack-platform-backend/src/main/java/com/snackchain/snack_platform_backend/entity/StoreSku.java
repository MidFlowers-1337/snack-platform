package com.snackchain.snack_platform_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 门店商品实体（SKU）
 * 门店级别的商品信息，包含门店自定义价格和库存
 */
@Data
@TableName("store_skus")
public class StoreSku {
    
    /**
     * SKU ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 平台商品ID
     */
    private Long productId;
    
    /**
     * 门店售价（可覆盖平台建议价）
     */
    private BigDecimal price;
    
    /**
     * 库存数量
     */
    private Integer stock;
    
    /**
     * 状态：1-上架，0-下架
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 门店信息（非数据库字段）
     */
    @TableField(exist = false)
    private Store store;
    
    /**
     * 商品信息（非数据库字段）
     */
    @TableField(exist = false)
    private Product product;
}