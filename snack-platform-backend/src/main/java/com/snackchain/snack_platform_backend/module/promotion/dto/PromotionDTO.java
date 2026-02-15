package com.snackchain.snack_platform_backend.module.promotion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建/编辑促销活动DTO
 */
@Data
public class PromotionDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotBlank(message = "活动标题不能为空")
    private String title;

    @NotNull(message = "原价不能为空")
    @DecimalMin(value = "0.01", message = "原价必须大于0")
    private BigDecimal originalPrice;

    @NotNull(message = "促销价不能为空")
    @DecimalMin(value = "0.01", message = "促销价必须大于0")
    private BigDecimal promotionPrice;

    @NotNull(message = "促销库存不能为空")
    @Min(value = 1, message = "促销库存至少为1")
    private Integer stock;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
}
