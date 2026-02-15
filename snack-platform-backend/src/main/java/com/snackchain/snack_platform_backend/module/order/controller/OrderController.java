package com.snackchain.snack_platform_backend.module.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snackchain.snack_platform_backend.common.annotation.OperationLog;
import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.entity.Order;
import com.snackchain.snack_platform_backend.module.order.dto.CreateOrderDTO;
import com.snackchain.snack_platform_backend.module.order.service.OrderService;
import com.snackchain.snack_platform_backend.module.order.vo.OrderStatsVO;
import com.snackchain.snack_platform_backend.security.context.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器（消费者端）
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "订单管理-消费者")
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * 创建订单
     */
    @Operation(summary = "创建订单")
    @PostMapping
    @OperationLog(module = "订单管理", operation = "创建订单")
    public Result<Order> create(@Valid @RequestBody CreateOrderDTO dto) {
        Long userId = getCurrentUserId();
        Order order = orderService.createOrder(userId, dto);
        return Result.success(order);
    }
    
    /**
     * 获取我的订单列表
     */
    @Operation(summary = "获取我的订单列表")
    @GetMapping
    public Result<IPage<Order>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        Long userId = getCurrentUserId();
        IPage<Order> page = orderService.pageByUserId(userId, pageNum, pageSize, status);
        return Result.success(page);
    }
    
    /**
     * 获取订单详情
     */
    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<Order> getById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Order order = orderService.getById(id);

        // 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        return Result.success(order);
    }

    /**
     * 根据订单编号获取订单
     */
    @Operation(summary = "根据订单编号获取订单")
    @GetMapping("/no/{orderNo}")
    public Result<Order> getByOrderNo(@PathVariable String orderNo) {
        Long userId = getCurrentUserId();
        Order order = orderService.getByOrderNo(orderNo);
        
        // 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        
        return Result.success(order);
    }
    
    /**
     * 支付订单（模拟支付）
     */
    @Operation(summary = "支付订单")
    @PostMapping("/{id}/pay")
    @OperationLog(module = "订单管理", operation = "支付订单")
    public Result<Void> pay(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        orderService.pay(id, userId);
        return Result.success();
    }
    
    /**
     * 取消订单
     */
    @Operation(summary = "取消订单")
    @PostMapping("/{id}/cancel")
    @OperationLog(module = "订单管理", operation = "取消订单")
    public Result<Void> cancel(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        orderService.cancel(id, userId);
        return Result.success();
    }

    /**
     * 获取当前用户订单状态统计
     */
    @Operation(summary = "获取订单状态统计")
    @GetMapping("/stats")
    public Result<OrderStatsVO> getOrderStats() {
        Long userId = getCurrentUserId();
        OrderStatsVO stats = orderService.getOrderStats(userId);
        return Result.success(stats);
    }
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}