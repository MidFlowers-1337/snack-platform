package com.snackchain.snack_platform_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单项实体
 */
@Data
@TableName("order_items")
public class OrderItem {
    
    /**
     * 订单项ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 门店商品ID
     */
    private Long skuId;
    
    /**
     * 商品名称（冗余）
     */
    private String productName;
    
    /**
     * 商品图片（冗余）
     */
    private String productImage;
    
    /**
     * 单价
     */
    private BigDecimal price;
    
    /**
     * 数量
     */
    private Integer quantity;
    
    /**
     * 小计金额
     */
    private BigDecimal subtotal;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 门店商品信息（非数据库字段）
     */
    @TableField(exist = false)
    private StoreSku sku;
}