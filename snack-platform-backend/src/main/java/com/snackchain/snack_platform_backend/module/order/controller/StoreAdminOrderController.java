package com.snackchain.snack_platform_backend.module.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snackchain.snack_platform_backend.common.annotation.OperationLog;
import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.entity.Order;
import com.snackchain.snack_platform_backend.enums.OrderStatus;
import com.snackchain.snack_platform_backend.module.order.dto.RejectOrderDTO;
import com.snackchain.snack_platform_backend.module.order.service.OrderService;
import com.snackchain.snack_platform_backend.security.context.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * 门店订单管理控制器（门店管理员端）
 */
@RestController
@RequestMapping("/store/orders")
@RequiredArgsConstructor
@Tag(name = "订单管理-门店")
public class StoreAdminOrderController {

    private final OrderService orderService;

    /**
     * 获取门店订单列表
     */
    @Operation(summary = "获取门店订单列表")
    @GetMapping
    public Result<IPage<Order>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long storeId = getCurrentStoreId();
        LocalDateTime startDateTime = parseStartDate(startDate);
        LocalDateTime endDateTime = parseEndDate(endDate);
        if (startDateTime != null && endDateTime != null && startDateTime.isAfter(endDateTime)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "开始日期不能晚于结束日期");
        }
        IPage<Order> page = orderService.pageByStoreId(
                storeId,
                pageNum,
                pageSize,
                parseStatus(status),
                normalizeOrderNo(orderNo),
                startDateTime,
                endDateTime);
        return Result.success(page);
    }

    /**
     * 获取订单详情
     */
    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<Order> getById(@PathVariable Long id) {
        Long storeId = getCurrentStoreId();
        Order order = orderService.getById(id);

        // 验证门店归属
        if (!order.getStoreId().equals(storeId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_BELONG_STORE);
        }

        return Result.success(order);
    }

    /**
     * 接单
     */
    @Operation(summary = "接单")
    @PostMapping("/{id}/accept")
    @OperationLog(module = "订单管理", operation = "门店接单")
    public Result<Void> accept(@PathVariable Long id) {
        Long storeId = getCurrentStoreId();
        orderService.accept(id, storeId);
        return Result.success();
    }

    /**
     * 拒绝订单
     */
    @Operation(summary = "拒绝订单")
    @PostMapping("/{id}/reject")
    @OperationLog(module = "订单管理", operation = "门店拒绝订单")
    public Result<Void> reject(@PathVariable Long id, @RequestBody RejectOrderDTO dto) {
        Long storeId = getCurrentStoreId();
        orderService.reject(id, storeId, dto.getReason());
        return Result.success();
    }

    /**
     * 备货完成
     */
    @Operation(summary = "备货完成")
    @PostMapping("/{id}/ready")
    @OperationLog(module = "订单管理", operation = "门店备货完成")
    public Result<Void> ready(@PathVariable Long id) {
        Long storeId = getCurrentStoreId();
        orderService.ready(id, storeId);
        return Result.success();
    }

    /**
     * 核销订单（通过自提码）
     */
    @Operation(summary = "核销订单")
    @PostMapping("/verify")
    @OperationLog(module = "订单管理", operation = "核销订单")
    public Result<Void> verify(@RequestParam String pickupCode) {
        Long storeId = getCurrentStoreId();
        orderService.verify(pickupCode, storeId);
        return Result.success();
    }

    /**
     * 兼容字符串和数字两种订单状态参数
     */
    private Integer parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        String normalized = status.trim().toUpperCase();
        if (normalized.matches("\\d+")) {
            return Integer.parseInt(normalized);
        }

        return switch (normalized) {
            case "PENDING", "PENDING_PAYMENT" -> OrderStatus.PENDING_PAYMENT.getCode();
            case "PAID", "PENDING_ACCEPT" -> OrderStatus.PENDING_ACCEPT.getCode();
            case "CONFIRMED", "ACCEPTED" -> OrderStatus.ACCEPTED.getCode();
            case "READY", "READY_FOR_PICKUP" -> OrderStatus.READY_FOR_PICKUP.getCode();
            case "COMPLETED" -> OrderStatus.COMPLETED.getCode();
            case "CANCELLED", "REJECTED" -> OrderStatus.CANCELLED.getCode();
            default -> throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "无效的订单状态: " + status);
        };
    }

    private String normalizeOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            return null;
        }
        return orderNo.trim();
    }

    private LocalDateTime parseStartDate(String startDate) {
        if (startDate == null || startDate.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(startDate.trim()).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "开始日期格式错误，应为 yyyy-MM-dd");
        }
    }

    private LocalDateTime parseEndDate(String endDate) {
        if (endDate == null || endDate.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(endDate.trim()).atTime(23, 59, 59);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "结束日期格式错误，应为 yyyy-MM-dd");
        }
    }

    /**
     * 获取当前门店ID
     */
    private Long getCurrentStoreId() {
        Long storeId = UserContextHolder.getStoreId();
        if (storeId == null) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您未绑定门店");
        }
        return storeId;
    }
}
