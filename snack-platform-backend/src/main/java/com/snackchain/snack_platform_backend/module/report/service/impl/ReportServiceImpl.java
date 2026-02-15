package com.snackchain.snack_platform_backend.module.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snackchain.snack_platform_backend.entity.Order;
import com.snackchain.snack_platform_backend.entity.Store;
import com.snackchain.snack_platform_backend.enums.OrderStatus;
import com.snackchain.snack_platform_backend.mapper.OrderMapper;
import com.snackchain.snack_platform_backend.mapper.StoreMapper;
import com.snackchain.snack_platform_backend.module.report.service.ReportService;
import com.snackchain.snack_platform_backend.module.report.vo.DashboardVO;
import com.snackchain.snack_platform_backend.module.report.vo.DailySalesAggVO;
import com.snackchain.snack_platform_backend.module.report.vo.RealtimeStatsVO;
import com.snackchain.snack_platform_backend.module.report.vo.SalesReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报表服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    
    private final OrderMapper orderMapper;
    private final StoreMapper storeMapper;
    
    @Override
    public DashboardVO getStoreDashboard(Long storeId) {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime monthStartTime = monthStart.atStartOfDay();
        
        // 今日订单统计
        LambdaQueryWrapper<Order> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(Order::getStoreId, storeId)
                    .ge(Order::getCreateTime, todayStart)
                    .le(Order::getCreateTime, todayEnd)
                    .ne(Order::getStatus, OrderStatus.CANCELLED.getCode());
        List<Order> todayOrders = orderMapper.selectList(todayWrapper);
        
        long todayOrderCount = todayOrders.size();
        BigDecimal todaySales = todayOrders.stream()
                .filter(o -> o.getStatus() >= OrderStatus.PENDING_ACCEPT.getCode())
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 待接单数
        LambdaQueryWrapper<Order> pendingAcceptWrapper = new LambdaQueryWrapper<>();
        pendingAcceptWrapper.eq(Order::getStoreId, storeId)
                           .eq(Order::getStatus, OrderStatus.PENDING_ACCEPT.getCode());
        long pendingAcceptCount = orderMapper.selectCount(pendingAcceptWrapper);
        
        // 待自提数
        LambdaQueryWrapper<Order> pendingPickupWrapper = new LambdaQueryWrapper<>();
        pendingPickupWrapper.eq(Order::getStoreId, storeId)
                           .eq(Order::getStatus, OrderStatus.READY_FOR_PICKUP.getCode());
        long pendingPickupCount = orderMapper.selectCount(pendingPickupWrapper);
        
        // 本月订单统计
        LambdaQueryWrapper<Order> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.eq(Order::getStoreId, storeId)
                    .ge(Order::getCreateTime, monthStartTime)
                    .ne(Order::getStatus, OrderStatus.CANCELLED.getCode());
        List<Order> monthOrders = orderMapper.selectList(monthWrapper);
        
        long monthOrderCount = monthOrders.size();
        BigDecimal monthSales = monthOrders.stream()
                .filter(o -> o.getStatus() >= OrderStatus.PENDING_ACCEPT.getCode())
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return DashboardVO.builder()
                .todayOrderCount(todayOrderCount)
                .todaySales(todaySales)
                .pendingAcceptCount(pendingAcceptCount)
                .pendingPickupCount(pendingPickupCount)
                .monthOrderCount(monthOrderCount)
                .monthSales(monthSales)
                .build();
    }
    
    @Override
    public DashboardVO getPlatformDashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime monthStartTime = monthStart.atStartOfDay();
        
        // 今日订单统计（全平台）
        LambdaQueryWrapper<Order> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Order::getCreateTime, todayStart)
                    .le(Order::getCreateTime, todayEnd)
                    .ne(Order::getStatus, OrderStatus.CANCELLED.getCode());
        List<Order> todayOrders = orderMapper.selectList(todayWrapper);
        
        long todayOrderCount = todayOrders.size();
        BigDecimal todaySales = todayOrders.stream()
                .filter(o -> o.getStatus() >= OrderStatus.PENDING_ACCEPT.getCode())
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 待接单数（全平台）
        LambdaQueryWrapper<Order> pendingAcceptWrapper = new LambdaQueryWrapper<>();
        pendingAcceptWrapper.eq(Order::getStatus, OrderStatus.PENDING_ACCEPT.getCode());
        long pendingAcceptCount = orderMapper.selectCount(pendingAcceptWrapper);
        
        // 待自提数（全平台）
        LambdaQueryWrapper<Order> pendingPickupWrapper = new LambdaQueryWrapper<>();
        pendingPickupWrapper.eq(Order::getStatus, OrderStatus.READY_FOR_PICKUP.getCode());
        long pendingPickupCount = orderMapper.selectCount(pendingPickupWrapper);
        
        // 本月订单统计（全平台）
        LambdaQueryWrapper<Order> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.ge(Order::getCreateTime, monthStartTime)
                    .ne(Order::getStatus, OrderStatus.CANCELLED.getCode());
        List<Order> monthOrders = orderMapper.selectList(monthWrapper);
        
        long monthOrderCount = monthOrders.size();
        BigDecimal monthSales = monthOrders.stream()
                .filter(o -> o.getStatus() >= OrderStatus.PENDING_ACCEPT.getCode())
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return DashboardVO.builder()
                .todayOrderCount(todayOrderCount)
                .todaySales(todaySales)
                .pendingAcceptCount(pendingAcceptCount)
                .pendingPickupCount(pendingPickupCount)
                .monthOrderCount(monthOrderCount)
                .monthSales(monthSales)
                .build();
    }
    
    @Override
    public List<SalesReportVO> getStoreSalesReport(Long storeId, LocalDate startDate, LocalDate endDate) {
        return buildSalesReportFromSql(storeId, startDate, endDate);
    }

    @Override
    public List<SalesReportVO> getPlatformSalesReport(LocalDate startDate, LocalDate endDate) {
        return buildSalesReportFromSql(null, startDate, endDate);
    }

    /**
     * 基于SQL聚合构建销售报表（替代内存流聚合，防OOM）
     *
     * What: 使用数据库 GROUP BY 完成按日期的销售聚合
     * Why: 原方案 selectList 全量加载到内存再 Java Stream groupBy，大数据量会 OOM
     * Why good: SQL 层面聚合效率高、内存占用恒定，10万条订单也不怕
     */
    private List<SalesReportVO> buildSalesReportFromSql(Long storeId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        // 数据库层面 GROUP BY 聚合
        List<DailySalesAggVO> aggResults = orderMapper.selectDailySalesAgg(storeId, startTime, endTime);

        // 构建日期→聚合结果的 Map
        Map<LocalDate, DailySalesAggVO> aggMap = aggResults.stream()
                .collect(Collectors.toMap(DailySalesAggVO::getDate, v -> v));

        // 填充日期范围内每一天的数据（包括零值天）
        List<SalesReportVO> report = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            DailySalesAggVO agg = aggMap.get(current);
            report.add(SalesReportVO.builder()
                    .date(current)
                    .orderCount(agg != null ? agg.getOrderCount() : 0L)
                    .sales(agg != null ? agg.getSales() : BigDecimal.ZERO)
                    .completedCount(agg != null ? agg.getCompletedCount() : 0L)
                    .cancelledCount(agg != null ? agg.getCancelledCount() : 0L)
                    .build());
            current = current.plusDays(1);
        }
        return report;
    }

    @Override
    public RealtimeStatsVO getRealtimeStats() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();
        LocalDateTime yesterdayEnd = yesterday.atTime(LocalTime.MAX);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime monthStartTime = monthStart.atStartOfDay();

        // ====== 核心指标 ======

        // 今日订单（排除已取消）
        LambdaQueryWrapper<Order> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Order::getCreateTime, todayStart)
                    .le(Order::getCreateTime, todayEnd)
                    .ne(Order::getStatus, OrderStatus.CANCELLED.getCode());
        List<Order> todayOrders = orderMapper.selectList(todayWrapper);

        long todayOrderCount = todayOrders.size();
        BigDecimal todaySales = todayOrders.stream()
                .filter(o -> o.getStatus() >= OrderStatus.PENDING_ACCEPT.getCode())
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 昨日订单（用于环比计算）
        LambdaQueryWrapper<Order> yesterdayWrapper = new LambdaQueryWrapper<>();
        yesterdayWrapper.ge(Order::getCreateTime, yesterdayStart)
                        .le(Order::getCreateTime, yesterdayEnd)
                        .ne(Order::getStatus, OrderStatus.CANCELLED.getCode());
        List<Order> yesterdayOrders = orderMapper.selectList(yesterdayWrapper);

        long yesterdayOrderCount = yesterdayOrders.size();
        BigDecimal yesterdaySalesAmount = yesterdayOrders.stream()
                .filter(o -> o.getStatus() >= OrderStatus.PENDING_ACCEPT.getCode())
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 活跃门店数（今日有订单的门店）
        long activeStores = todayOrders.stream()
                .map(Order::getStoreId)
                .distinct()
                .count();

        // 门店总数
        long totalStores = storeMapper.selectCount(null);

        // 本月订单统计
        LambdaQueryWrapper<Order> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.ge(Order::getCreateTime, monthStartTime)
                    .ne(Order::getStatus, OrderStatus.CANCELLED.getCode());
        List<Order> monthOrders = orderMapper.selectList(monthWrapper);

        long monthOrderCount = monthOrders.size();
        BigDecimal monthSalesAmount = monthOrders.stream()
                .filter(o -> o.getStatus() >= OrderStatus.PENDING_ACCEPT.getCode())
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ====== 7天销售趋势 ======
        LocalDate trendStart = today.minusDays(6);
        List<SalesReportVO> salesTrend = getPlatformSalesReport(trendStart, today);

        // ====== 门店销售排行（本月 Top5） ======
        // 查询所有门店信息用于名称映射
        List<Store> allStores = storeMapper.selectList(null);
        Map<Long, String> storeNameMap = allStores.stream()
                .collect(Collectors.toMap(Store::getId, Store::getName));

        // 按门店分组计算销售数据
        Map<Long, List<Order>> ordersByStore = monthOrders.stream()
                .collect(Collectors.groupingBy(Order::getStoreId));

        List<RealtimeStatsVO.StoreRankItem> storeRanking = ordersByStore.entrySet().stream()
                .map(entry -> {
                    Long storeId = entry.getKey();
                    List<Order> storeOrders = entry.getValue();
                    BigDecimal storeSales = storeOrders.stream()
                            .filter(o -> o.getStatus() >= OrderStatus.PENDING_ACCEPT.getCode())
                            .map(Order::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return RealtimeStatsVO.StoreRankItem.builder()
                            .name(storeNameMap.getOrDefault(storeId, "未知门店"))
                            .orderCount((long) storeOrders.size())
                            .salesAmount(storeSales)
                            .build();
                })
                .sorted((a, b) -> b.getSalesAmount().compareTo(a.getSalesAmount()))
                .limit(5)
                .collect(Collectors.toList());

        // ====== 最近订单流水（最新10条） ======
        LambdaQueryWrapper<Order> recentWrapper = new LambdaQueryWrapper<>();
        recentWrapper.orderByDesc(Order::getCreateTime)
                     .last("LIMIT 10");
        List<Order> recentOrderList = orderMapper.selectList(recentWrapper);

        List<RealtimeStatsVO.RecentOrderItem> recentOrders = recentOrderList.stream()
                .map(order -> RealtimeStatsVO.RecentOrderItem.builder()
                        .orderNo(order.getOrderNo())
                        .storeName(storeNameMap.getOrDefault(order.getStoreId(), "未知门店"))
                        .amount(order.getTotalAmount())
                        .status(order.getStatus())
                        .statusText(order.getStatusEnum().getDescription())
                        .createTime(order.getCreateTime())
                        .build())
                .collect(Collectors.toList());

        return RealtimeStatsVO.builder()
                .todayOrders(todayOrderCount)
                .todaySales(todaySales)
                .yesterdayOrders(yesterdayOrderCount)
                .yesterdaySales(yesterdaySalesAmount)
                .activeStores(activeStores)
                .totalStores(totalStores)
                .monthOrders(monthOrderCount)
                .monthSales(monthSalesAmount)
                .salesTrend(salesTrend)
                .storeRanking(storeRanking)
                .recentOrders(recentOrders)
                .build();
    }
}