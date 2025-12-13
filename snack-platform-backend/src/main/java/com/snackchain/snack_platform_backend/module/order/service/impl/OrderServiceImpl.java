package com.snackchain.snack_platform_backend.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.entity.*;
import com.snackchain.snack_platform_backend.enums.OrderStatus;
import com.snackchain.snack_platform_backend.mapper.*;
import com.snackchain.snack_platform_backend.module.order.dto.CreateOrderDTO;
import com.snackchain.snack_platform_backend.module.order.dto.OrderItemDTO;
import com.snackchain.snack_platform_backend.module.order.service.OrderService;
import com.snackchain.snack_platform_backend.module.sku.service.StoreSkuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final StoreMapper storeMapper;
    private final StoreSkuMapper storeSkuMapper;
    private final ProductMapper productMapper;
    private final StoreSkuService storeSkuService;
    
    @Override
    @Transactional
    public Order createOrder(Long userId, CreateOrderDTO dto) {
        // 验证门店存在且营业中
        Store store = storeMapper.selectById(dto.getStoreId());
        if (store == null) {
            throw new BusinessException(ResultCode.STORE_NOT_FOUND);
        }
        if (store.getStatus() != 1) {
            throw new BusinessException(ResultCode.STORE_CLOSED);
        }
        
        // 计算订单总金额并验证库存
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemDTO itemDto : dto.getItems()) {
            StoreSku sku = storeSkuService.getById(itemDto.getSkuId());
            if (sku == null || !sku.getStoreId().equals(dto.getStoreId())) {
                throw new BusinessException(ResultCode.SKU_NOT_FOUND);
            }
            if (sku.getStatus() != 1) {
                throw new BusinessException(ResultCode.PRODUCT_OFF_SHELF);
            }
            if (sku.getStock() < itemDto.getQuantity()) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
            }
            
            BigDecimal subtotal = sku.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
        }
        
        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setStoreId(dto.getStoreId());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setRemark(dto.getRemark());
        
        orderMapper.insert(order);
        
        // 创建订单项并扣减库存
        for (OrderItemDTO itemDto : dto.getItems()) {
            StoreSku sku = storeSkuService.getById(itemDto.getSkuId());
            Product product = productMapper.selectById(sku.getProductId());
            
            // 扣减库存
            boolean success = storeSkuService.deductStock(sku.getId(), itemDto.getQuantity());
            if (!success) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
            }
            
            // 创建订单项
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setSkuId(sku.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getImage());
            item.setPrice(sku.getPrice());
            item.setQuantity(itemDto.getQuantity());
            item.setSubtotal(sku.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            
            orderItemMapper.insert(item);
        }
        
        log.info("创建订单成功: orderNo={}, userId={}, storeId={}, totalAmount={}", 
                order.getOrderNo(), userId, dto.getStoreId(), totalAmount);
        
        return order;
    }
    
    @Override
    @Transactional
    public void pay(Long orderId, Long userId) {
        Order order = getById(orderId);
        
        // 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        
        // 验证订单状态
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        
        // 更新订单状态为待接单
        order.setStatus(OrderStatus.PENDING_ACCEPT.getCode());
        order.setPayTime(LocalDateTime.now());
        order.setPickupCode(generatePickupCode());  // 生成自提码
        
        orderMapper.updateById(order);
        log.info("订单支付成功: orderNo={}, pickupCode={}", order.getOrderNo(), order.getPickupCode());
    }
    
    @Override
    @Transactional
    public void cancel(Long orderId, Long userId) {
        Order order = getById(orderId);
        
        // 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        
        // 只有待支付和待接单状态可以取消
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(order.getStatus()) &&
            !OrderStatus.PENDING_ACCEPT.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        
        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            storeSkuService.restoreStock(item.getSkuId(), item.getQuantity());
        }
        
        // 更新订单状态
        order.setStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelTime(LocalDateTime.now());
        
        orderMapper.updateById(order);
        log.info("订单取消成功: orderNo={}", order.getOrderNo());
    }
    
    @Override
    public Order getById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        
        // 填充订单项
        List<OrderItem> items = orderItemMapper.selectByOrderId(id);
        order.setItems(items);
        
        // 填充门店信息
        if (order.getStoreId() != null) {
            Store store = storeMapper.selectById(order.getStoreId());
            order.setStore(store);
        }
        
        return order;
    }
    
    @Override
    public Order getByOrderNo(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        
        // 填充订单项
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
        order.setItems(items);
        
        return order;
    }
    
    @Override
    public IPage<Order> pageByUserId(Long userId, int pageNum, int pageSize, Integer status) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        
        IPage<Order> result = orderMapper.selectPage(page, wrapper);
        
        // 填充订单项和门店信息
        fillOrderDetails(result.getRecords());
        
        return result;
    }
    
    @Override
    public IPage<Order> pageByStoreId(Long storeId, int pageNum, int pageSize, Integer status) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStoreId, storeId);
        
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        
        IPage<Order> result = orderMapper.selectPage(page, wrapper);
        
        // 填充订单项
        fillOrderDetails(result.getRecords());
        
        return result;
    }
    
    @Override
    @Transactional
    public void accept(Long orderId, Long storeId) {
        Order order = getById(orderId);
        
        // 验证门店归属
        if (!order.getStoreId().equals(storeId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_BELONG_STORE);
        }
        
        // 验证订单状态
        if (!OrderStatus.PENDING_ACCEPT.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        
        // 更新订单状态
        order.setStatus(OrderStatus.ACCEPTED.getCode());
        order.setAcceptTime(LocalDateTime.now());
        
        orderMapper.updateById(order);
        log.info("门店接单成功: orderNo={}, storeId={}", order.getOrderNo(), storeId);
    }
    
    @Override
    @Transactional
    public void ready(Long orderId, Long storeId) {
        Order order = getById(orderId);
        
        // 验证门店归属
        if (!order.getStoreId().equals(storeId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_BELONG_STORE);
        }
        
        // 验证订单状态
        if (!OrderStatus.ACCEPTED.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        
        // 更新订单状态
        order.setStatus(OrderStatus.READY_FOR_PICKUP.getCode());
        order.setReadyTime(LocalDateTime.now());
        
        orderMapper.updateById(order);
        log.info("备货完成: orderNo={}, storeId={}", order.getOrderNo(), storeId);
    }
    
    @Override
    @Transactional
    public void verify(String pickupCode, Long storeId) {
        // 根据自提码查询订单
        Order order = orderMapper.selectByPickupCodeAndStoreId(pickupCode, storeId);
        if (order == null) {
            throw new BusinessException(ResultCode.PICKUP_CODE_INVALID);
        }
        
        // 验证订单状态
        if (!OrderStatus.READY_FOR_PICKUP.getCode().equals(order.getStatus())) {
            if (OrderStatus.COMPLETED.getCode().equals(order.getStatus())) {
                throw new BusinessException(ResultCode.ORDER_ALREADY_VERIFIED);
            }
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        
        // 更新订单状态
        order.setStatus(OrderStatus.COMPLETED.getCode());
        order.setCompleteTime(LocalDateTime.now());
        
        orderMapper.updateById(order);
        log.info("订单核销成功: orderNo={}, pickupCode={}", order.getOrderNo(), pickupCode);
    }
    
    @Override
    @Transactional
    public void reject(Long orderId, Long storeId, String reason) {
        Order order = getById(orderId);
        
        // 验证门店归属
        if (!order.getStoreId().equals(storeId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_BELONG_STORE);
        }
        
        // 验证订单状态（只有待接单状态可以拒绝）
        if (!OrderStatus.PENDING_ACCEPT.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        
        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            storeSkuService.restoreStock(item.getSkuId(), item.getQuantity());
        }
        
        // 更新订单状态
        order.setStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelTime(LocalDateTime.now());
        order.setRemark(order.getRemark() != null ? 
                order.getRemark() + " [门店拒绝: " + reason + "]" : 
                "[门店拒绝: " + reason + "]");
        
        orderMapper.updateById(order);
        log.info("门店拒绝订单: orderNo={}, reason={}", order.getOrderNo(), reason);
    }
    
    // 使用 SecureRandom 替代 Random，提高安全性
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    /**
     * 生成订单编号 - 使用 SecureRandom 增加随机性
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        // 使用 SecureRandom 生成6位随机数，增加不可预测性
        String random = String.format("%06d", SECURE_RANDOM.nextInt(1000000));
        return "ORD" + timestamp + random;
    }
    
    /**
     * 生成自提码（6位数字）- 使用 SecureRandom 防止预测
     */
    private String generatePickupCode() {
        // 使用 SecureRandom 生成安全的随机取货码
        return String.format("%06d", SECURE_RANDOM.nextInt(1000000));
    }
    
    /**
     * 填充订单详情
     */
    private void fillOrderDetails(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        
        // 获取所有门店ID
        List<Long> storeIds = orders.stream()
                .map(Order::getStoreId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询门店
        Map<Long, Store> storeMap = storeIds.isEmpty() ? Map.of() :
                storeMapper.selectBatchIds(storeIds).stream()
                        .collect(Collectors.toMap(Store::getId, s -> s));
        
        // 填充门店信息和订单项
        for (Order order : orders) {
            if (order.getStoreId() != null) {
                order.setStore(storeMap.get(order.getStoreId()));
            }
            List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
            order.setItems(items);
        }
    }
}