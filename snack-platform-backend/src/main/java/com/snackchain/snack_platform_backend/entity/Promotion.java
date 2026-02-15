package com.snackchain.snack_platform_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 促销活动实体
 *
 * What: 商品限时促销信息，含促销价、时间范围、限购库存
 * Why: 实现"秒杀/限时抢购"核心功能
 * Why good: 乐观锁防超卖（复用已有CAS模式）+时间校验保证促销规则
 */
@Data
@TableName("promotions")
public class Promotion {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联商品ID（SPU维度） */
    private Long productId;

    /** 活动标题 */
    private String title;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 促销价 */
    private BigDecimal promotionPrice;

    /** 促销库存（限量） */
    private Integer stock;

    /** 已售数量 */
    private Integer soldCount;

    /** 活动开始时间 */
    private LocalDateTime startTime;

    /** 活动结束时间 */
    private LocalDateTime endTime;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 商品名称（非DB字段，查询填充） */
    @TableField(exist = false)
    private String productName;

    /** 商品图片（非DB字段） */
    @TableField(exist = false)
    private String productImage;

    /**
     * 判断当前促销是否进行中
     */
    public boolean isActive() {
        if (status == null || status != 1) return false;
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startTime) && now.isBefore(endTime) && (stock - soldCount) > 0;
    }
}
