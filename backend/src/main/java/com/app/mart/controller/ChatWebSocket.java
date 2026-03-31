package com.app.mart.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 实时聊天服务
 * 功能：处理用户连接、断开、消息推送、在线用户管理
 * 提供静态方法 sendToUser() 实现后台主动推送消息
 *
 * @author LunaMart
 */
@Component
@ServerEndpoint("/chat/ws/{userId}")
@SuppressWarnings({"unused", "resource"}) // 压制 IDE 对 WebSocket 生命周期方法的误报
public class ChatWebSocket {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocket.class);

    /**
     * 在线用户会话存储（线程安全）
     * key: userId
     * value: WebSocket Session
     */
    private static final Map<Long, Session> ONLINE_USERS = new ConcurrentHashMap<>();

    /**
     * 用户建立 WebSocket 连接
     *
     * @param session WebSocket 会话
     * @param userId  当前连接的用户ID
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        ONLINE_USERS.put(userId, session);
        log.info("用户 {} 建立 WebSocket 连接", userId);
    }

    /**
     * 用户断开 WebSocket 连接
     *
     * @param userId 断开连接的用户ID
     */
    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        ONLINE_USERS.remove(userId);
        log.info("用户 {} 断开 WebSocket 连接", userId);
    }

    /**
     * 接收客户端消息（本项目主要用于后台推送，前端较少主动发消息）
     *
     * @param message 接收到的消息内容
     * @param userId  发送消息的用户ID
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") Long userId) {
        log.debug("收到用户 {} 的消息：{}", userId, message);
    }

    /**
     * WebSocket 异常处理
     *
     * @param session 当前发生异常的会话
     * @param error   异常信息
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 连接发生错误", error);
    }

    /**
     * 【静态方法】后台主动发送消息给指定用户
     *
     * @param toUserId 目标用户ID
     * @param message  消息内容（JSON格式）
     */
    public static void sendToUser(Long toUserId, String message) {
        Session session = ONLINE_USERS.get(toUserId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                log.debug("成功发送消息给用户 {}", toUserId);
            } catch (IOException e) {
                log.error("发送消息给用户 {} 失败", toUserId, e);
            }
        } else {
            log.warn("用户 {} 不在线或连接已关闭", toUserId);
        }
    }
}