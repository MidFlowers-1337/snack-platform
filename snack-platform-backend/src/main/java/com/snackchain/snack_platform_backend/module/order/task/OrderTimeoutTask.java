package com.snackchain.snack_platform_backend.module.order.task;

import com.snackchain.snack_platform_backend.entity.Order;
import com.snackchain.snack_platform_backend.entity.OrderItem;
import com.snackchain.snack_platform_backend.enums.OrderStatus;
import com.snackchain.snack_platform_backend.mapper.OrderItemMapper;
import com.snackchain.snack_platform_backend.mapper.OrderMapper;
import com.snackchain.snack_platform_backend.module.sku.service.StoreSkuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时自动取消定时任务
 * 
 * 业务规则：
 * - 待支付订单超过30分钟未支付，自动取消并回滚库存
 * - 待接单订单超过15分钟未接单，自动取消并回滚库存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {
    
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final StoreSkuService storeSkuService;
    
    /**
     * 待支付订单超时时间（分钟）
     */
    private static final int PENDING_PAYMENT_TIMEOUT = 30;
    
    /**
     * 待接单订单超时时间（分钟）
     */
    private static final int PENDING_ACCEPT_TIMEOUT = 15;
    
    /**
     * 每分钟执行一次，检查超时订单
     */
    @Scheduled(cron = "0 * * * * ?")
    public void cancelTimeoutOrders() {
        log.info("开始执行订单超时检查任务...");
        
        // 1. 处理超时未支付的订单
        cancelTimeoutPendingPaymentOrders();
        
        // 2. 处理超时未接单的订单
        cancelTimeoutPendingAcceptOrders();
        
        log.info("订单超时检查任务执行完成");
    }
    
    /**
     * 取消超时未支付的订单
     */
    private void cancelTimeoutPendingPaymentOrders() {
        List<Order> timeoutOrders = orderMapper.selectTimeoutPendingPaymentOrders(PENDING_PAYMENT_TIMEOUT);
        
        if (timeoutOrders.isEmpty()) {
            log.debug("没有超时未支付的订单");
            return;
        }
        
        log.info("发现 {} 个超时未支付的订单", timeoutOrders.size());
        
        for (Order order : timeoutOrders) {
            try {
                cancelOrderWithStockRollback(order, "超时未支付自动取消");
            } catch (Exception e) {
                log.error("取消超时未支付订单失败: orderNo={}, error={}", order.getOrderNo(), e.getMessage(), e);
            }
        }
    }
    
    /**
     * 取消超时未接单的订单
     */
    private void cancelTimeoutPendingAcceptOrders() {
        List<Order> timeoutOrders = orderMapper.selectTimeoutPendingAcceptOrders(PENDING_ACCEPT_TIMEOUT);
        
        if (timeoutOrders.isEmpty()) {
            log.debug("没有超时未接单的订单");
            return;
        }
        
        log.info("发现 {} 个超时未接单的订单", timeoutOrders.size());
        
        for (Order order : timeoutOrders) {
            try {
                cancelOrderWithStockRollback(order, "超时未接单自动取消");
            } catch (Exception e) {
                log.error("取消超时未接单订单失败: orderNo={}, error={}", order.getOrderNo(), e.getMessage(), e);
            }
        }
    }
    
    /**
     * 取消订单并回滚库存
     * 
     * @param order 订单
     * @param reason 取消原因
     */
    @Transactional
    public void cancelOrderWithStockRollback(Order order, String reason) {
        // 1. 查询订单项
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
        
        // 2. 回滚库存
        for (OrderItem item : items) {
            storeSkuService.restoreStock(item.getSkuId(), item.getQuantity());
            log.debug("回滚库存: skuId={}, quantity={}", item.getSkuId(), item.getQuantity());
        }
        
        // 3. 更新订单状态
        order.setStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelTime(LocalDateTime.now());
        String originalRemark = order.getRemark();
        order.setRemark(originalRemark != null ? 
                originalRemark + " [" + reason + "]" : 
                "[" + reason + "]");
        
        orderMapper.updateById(order);
        
        log.info("订单自动取消成功: orderNo={}, reason={}", order.getOrderNo(), reason);
    }
}