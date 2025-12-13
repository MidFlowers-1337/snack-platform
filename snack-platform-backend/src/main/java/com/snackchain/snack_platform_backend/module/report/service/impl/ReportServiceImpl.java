package com.snackchain.snack_platform_backend.module.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snackchain.snack_platform_backend.entity.Order;
import com.snackchain.snack_platform_backend.enums.OrderStatus;
import com.snackchain.snack_platform_backend.mapper.OrderMapper;
import com.snackchain.snack_platform_backend.module.report.service.ReportService;
import com.snackchain.snack_platform_backend.module.report.vo.DashboardVO;
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
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);
        
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStoreId, storeId)
               .ge(Order::getCreateTime, startTime)
               .le(Order::getCreateTime, endTime);
        List<Order> orders = orderMapper.selectList(wrapper);
        
        return buildSalesReport(orders, startDate, endDate);
    }
    
    @Override
    public List<SalesReportVO> getPlatformSalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);
        
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Order::getCreateTime, startTime)
               .le(Order::getCreateTime, endTime);
        List<Order> orders = orderMapper.selectList(wrapper);
        
        return buildSalesReport(orders, startDate, endDate);
    }
    
    /**
     * 构建销售报表
     */
    private List<SalesReportVO> buildSalesReport(List<Order> orders, LocalDate startDate, LocalDate endDate) {
        // 按日期分组
        Map<LocalDate, List<Order>> ordersByDate = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreateTime().toLocalDate()));
        
        List<SalesReportVO> report = new ArrayList<>();
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            List<Order> dayOrders = ordersByDate.getOrDefault(current, List.of());
            
            long orderCount = dayOrders.size();
            BigDecimal sales = dayOrders.stream()
                    .filter(o -> o.getStatus() >= OrderStatus.PENDING_ACCEPT.getCode() && 
                                 o.getStatus() != OrderStatus.CANCELLED.getCode())
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            long completedCount = dayOrders.stream()
                    .filter(o -> OrderStatus.COMPLETED.getCode().equals(o.getStatus()))
                    .count();
            
            long cancelledCount = dayOrders.stream()
                    .filter(o -> OrderStatus.CANCELLED.getCode().equals(o.getStatus()))
                    .count();
            
            report.add(SalesReportVO.builder()
                    .date(current)
                    .orderCount(orderCount)
                    .sales(sales)
                    .completedCount(completedCount)
                    .cancelledCount(cancelledCount)
                    .build());
            
            current = current.plusDays(1);
        }
        
        return report;
    }
}