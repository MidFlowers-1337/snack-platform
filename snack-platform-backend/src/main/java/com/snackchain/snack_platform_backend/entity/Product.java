package com.snackchain.snack_platform_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 平台商品实体（SPU）
 */
@Data
@TableName("products")
public class Product {
    
    /**
     * 商品ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 商品名称
     */
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
    private BigDecimal price;
    
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
     * 分类信息（非数据库字段）
     */
    @TableField(exist = false)
    private Category category;
}