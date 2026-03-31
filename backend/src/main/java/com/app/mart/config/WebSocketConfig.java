package com.app.mart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket 配置类
 * 开启 WebSocket 支持，用于实时聊天
 *
 * @author liangtong
 */
@Configuration
public class WebSocketConfig {

    /**
     * 自动注册使用 @ServerEndpoint 注解的 WebSocket 端点
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}