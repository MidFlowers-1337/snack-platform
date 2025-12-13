package com.snackchain.snack_platform_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车项实体
 * 存储用户在特定门店的购物车商品信息
 */
@Data
@TableName("cart_item")
public class CartItem {
    
    /**
     * 购物车项ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * SKU ID（门店商品ID）
     */
    private Long skuId;
    
    /**
     * 数量
     */
    private Integer quantity;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /**
     * 用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private User user;
    
    /**
     * 门店信息（非数据库字段）
     */
    @TableField(exist = false)
    private Store store;
    
    /**
     * 门店商品信息（非数据库字段）
     */
    @TableField(exist = false)
    private StoreSku storeSku;
}