package com.snackchain.snack_platform_backend.module.report.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 每日销售SQL聚合结果VO
 *
 * What: 数据库 GROUP BY 聚合查询的直接映射对象
 * Why: 替代内存流聚合，避免大数据量 OOM
 * Why good: SQL层面聚合效率远高于Java Stream，且内存占用恒定
 */
@Data
public class DailySalesAggVO {

    /** 日期 */
    private LocalDate date;

    /** 订单数 */
    private Long orderCount;

    /** 销售额（排除待支付和已取消） */
    private BigDecimal sales;

    /** 已完成订单数 */
    private Long completedCount;

    /** 已取消订单数 */
    private Long cancelledCount;
}
