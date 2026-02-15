package com.snackchain.snack_platform_backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置
 *
 * What: 基于 STOMP 协议的 WebSocket 消息推送
 * Why: 订单状态变更（新订单/接单/备货完成/拒单）需要实时通知对应用户或门店管理员
 * Why good: 双屏演示效果——消费者下单后门店端实时弹出通知，直观展示"多门店协同"
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 客户端订阅前缀：/topic（广播）和 /queue（点对点）
        config.enableSimpleBroker("/topic", "/queue");
        // 客户端发送消息前缀
        config.setApplicationDestinationPrefixes("/app");
        // 用户目标前缀（点对点推送）
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
