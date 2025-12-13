package com.snackchain.snack_platform_backend.module.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snackchain.snack_platform_backend.entity.Order;
import com.snackchain.snack_platform_backend.module.order.dto.CreateOrderDTO;

/**
 * 订单服务接口
 */
public interface OrderService {
    
    /**
     * 创建订单
     */
    Order createOrder(Long userId, CreateOrderDTO dto);
    
    /**
     * 模拟支付（实际项目应对接支付网关）
     */
    void pay(Long orderId, Long userId);
    
    /**
     * 取消订单
     */
    void cancel(Long orderId, Long userId);
    
    /**
     * 根据ID获取订单
     */
    Order getById(Long id);
    
    /**
     * 根据订单编号获取订单
     */
    Order getByOrderNo(String orderNo);
    
    /**
     * 获取用户订单列表
     */
    IPage<Order> pageByUserId(Long userId, int pageNum, int pageSize, Integer status);
    
    /**
     * 获取门店订单列表
     */
    IPage<Order> pageByStoreId(Long storeId, int pageNum, int pageSize, Integer status);
    
    /**
     * 门店接单
     */
    void accept(Long orderId, Long storeId);
    
    /**
     * 门店备货完成
     */
    void ready(Long orderId, Long storeId);
    
    /**
     * 核销订单（自提完成）
     */
    void verify(String pickupCode, Long storeId);
    
    /**
     * 门店拒绝订单
     */
    void reject(Long orderId, Long storeId, String reason);
}