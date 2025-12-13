package com.snackchain.snack_platform_backend.module.report.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 销售报表VO
 */
@Data
@Builder
public class SalesReportVO {
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 订单数
     */
    private Long orderCount;
    
    /**
     * 销售额
     */
    private BigDecimal sales;
    
    /**
     * 完成订单数
     */
    private Long completedCount;
    
    /**
     * 取消订单数
     */
    private Long cancelledCount;
}