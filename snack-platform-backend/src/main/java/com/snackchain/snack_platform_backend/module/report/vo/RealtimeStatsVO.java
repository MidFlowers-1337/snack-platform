package com.snackchain.snack_platform_backend.module.report.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据大屏实时统计VO
 *
 * What: 聚合数据大屏所需的全部统计数据，一次请求返回所有图表数据
 * Why: 避免前端多次请求，减少网络开销
 * Why good: 单接口设计简化前端逻辑，适合定时轮询刷新场景
 */
@Data
@Builder
public class RealtimeStatsVO {

    // ========== 核心指标 ==========

    /** 今日订单数 */
    private Long todayOrders;

    /** 今日销售额 */
    private BigDecimal todaySales;

    /** 昨日订单数（用于计算环比） */
    private Long yesterdayOrders;

    /** 昨日销售额（用于计算环比） */
    private BigDecimal yesterdaySales;

    /** 活跃门店数（今日有订单的门店） */
    private Long activeStores;

    /** 门店总数 */
    private Long totalStores;

    /** 本月订单数 */
    private Long monthOrders;

    /** 本月销售额 */
    private BigDecimal monthSales;

    // ========== 图表数据 ==========

    /** 最近7天销售趋势 */
    private List<SalesReportVO> salesTrend;

    /** 门店销售排行（本月 Top5） */
    private List<StoreRankItem> storeRanking;

    /** 最近订单流水（最新10条） */
    private List<RecentOrderItem> recentOrders;

    // ========== 内部数据结构 ==========

    @Data
    @Builder
    public static class StoreRankItem {
        private String name;
        private Long orderCount;
        private BigDecimal salesAmount;
    }

    @Data
    @Builder
    public static class RecentOrderItem {
        private String orderNo;
        private String storeName;
        private BigDecimal amount;
        private Integer status;
        private String statusText;
        private LocalDateTime createTime;
    }
}
