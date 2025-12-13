package com.snackchain.snack_platform_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 门店实体
 */
@Data
@TableName("stores")
public class Store {
    
    /**
     * 门店ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 门店名称
     */
    private String name;
    
    /**
     * 门店地址
     */
    private String address;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 门店图片URL
     */
    private String image;
    
    /**
     * 经度
     */
    private BigDecimal longitude;
    
    /**
     * 纬度
     */
    private BigDecimal latitude;
    
    /**
     * 营业时间（如：09:00-22:00）
     */
    private String businessHours;
    
    /**
     * 状态：1-营业中，0-休息中
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
}