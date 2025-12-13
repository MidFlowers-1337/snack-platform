package com.snackchain.snack_platform_backend.module.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 分类请求DTO
 */
@Data
public class CategoryDTO {
    
    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String name;
    
    /**
     * 分类图标URL
     */
    private String icon;
    
    /**
     * 排序值
     */
    private Integer sort;
    
    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;
}