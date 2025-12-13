package com.snackchain.snack_platform_backend.module.store.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 门店请求DTO
 */
@Data
public class StoreDTO {
    
    /**
     * 门店名称
     */
    @NotBlank(message = "门店名称不能为空")
    private String name;
    
    /**
     * 门店地址
     */
    @NotBlank(message = "门店地址不能为空")
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
     * 营业时间
     */
    private String businessHours;
    
    /**
     * 状态：1-营业中，0-休息中
     */
    private Integer status;
}