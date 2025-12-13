package com.snackchain.snack_platform_backend.module.report.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 仪表盘统计VO
 */
@Data
@Builder
public class DashboardVO {
    
    /**
     * 今日订单数
     */
    private Long todayOrderCount;
    
    /**
     * 今日销售额
     */
    private BigDecimal todaySales;
    
    /**
     * 待接单数
     */
    private Long pendingAcceptCount;
    
    /**
     * 待自提数
     */
    private Long pendingPickupCount;
    
    /**
     * 本月订单数
     */
    private Long monthOrderCount;
    
    /**
     * 本月销售额
     */
    private BigDecimal monthSales;
}