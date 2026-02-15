package com.snackchain.snack_platform_backend.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知推送服务
 *
 * What: 基于 WebSocket STOMP 的实时消息推送
 * Why: 订单状态变更需要实时通知消费者和门店管理员
 * Why good: 零延迟通知体验 + 答辩双屏演示效果拉满
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 向指定门店推送新订单通知
     */
    public void notifyNewOrder(Long storeId, String orderNo, Double amount) {
        Map<String, Object> message = buildMessage(
                "NEW_ORDER",
                "收到新订单 #" + orderNo + "，金额 ¥" + String.format("%.2f", amount),
                orderNo
        );
        messagingTemplate.convertAndSend("/topic/store/" + storeId + "/orders", message);
        log.info("推送新订单通知到门店 {}: {}", storeId, orderNo);
    }

    /**
     * 向消费者推送订单状态更新
     */
    public void notifyOrderStatusChange(Long userId, String orderNo, String status, String statusText) {
        Map<String, Object> message = buildMessage(
                "ORDER_STATUS",
                getStatusMessage(status, orderNo),
                orderNo
        );
        message.put("status", status);
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/notifications", message);
        log.info("推送订单状态通知到用户 {}: {} -> {}", userId, orderNo, status);
    }

    /**
     * 向指定门店推送通知
     */
    public void notifyStore(Long storeId, String type, String content) {
        Map<String, Object> message = buildMessage(type, content, null);
        messagingTemplate.convertAndSend("/topic/store/" + storeId + "/notifications", message);
    }

    private Map<String, Object> buildMessage(String type, String content, String orderNo) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.put("content", content);
        message.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (orderNo != null) {
            message.put("orderNo", orderNo);
        }
        return message;
    }

    private String getStatusMessage(String status, String orderNo) {
        return switch (status) {
            case "CONFIRMED" -> "您的订单 #" + orderNo + " 已被门店接受，正在备货中";
            case "READY" -> "您的订单 #" + orderNo + " 已备好，请凭取货码到店取货";
            case "REJECTED" -> "很抱歉，您的订单 #" + orderNo + " 已被门店拒绝";
            case "COMPLETED" -> "您的订单 #" + orderNo + " 已完成，感谢您的光顾！";
            case "CANCELLED" -> "您的订单 #" + orderNo + " 已取消";
            default -> "您的订单 #" + orderNo + " 状态已更新为 " + status;
        };
    }
}
