package com.snackchain.snack_platform_backend.module.order.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 订单状态统计VO
 *
 * What: 返回当前用户各状态订单的数量
 * Why: 替代前端 getMyOrders({size:1000}) 全量拉取后客户端计数
 * Why good: 减少95%数据传输，数据库聚合比内存遍历更高效
 */
@Data
@Builder
public class OrderStatsVO {

    /** 总订单数 */
    private Long total;

    /** 待支付 */
    private Long pendingPayment;

    /** 处理中（待接单+已接单+待自提） */
    private Long processing;

    /** 已完成 */
    private Long completed;

    /** 已取消 */
    private Long cancelled;
}
