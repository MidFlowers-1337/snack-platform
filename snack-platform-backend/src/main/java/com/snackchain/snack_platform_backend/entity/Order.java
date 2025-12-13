package com.snackchain.snack_platform_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.snackchain.snack_platform_backend.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体
 */
@Data
@TableName("orders")
public class Order {
    
    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单编号
     */
    private String orderNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 订单状态
     */
    private Integer status;
    
    /**
     * 自提码
     */
    private String pickupCode;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 支付时间
     */
    private LocalDateTime payTime;
    
    /**
     * 接单时间
     */
    private LocalDateTime acceptTime;
    
    /**
     * 备货完成时间
     */
    private LocalDateTime readyTime;
    
    /**
     * 完成时间
     */
    private LocalDateTime completeTime;
    
    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;
    
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
     * 用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private User user;
    
    /**
     * 订单项列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<OrderItem> items;
    
    /**
     * 获取订单状态枚举
     */
    public OrderStatus getStatusEnum() {
        return OrderStatus.fromCode(this.status);
    }
}