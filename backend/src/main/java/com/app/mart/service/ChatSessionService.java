package com.app.mart.service;

import com.app.mart.entity.ChatSession;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 聊天会话服务接口
 * 支持普通对话、绑定订单、绑定商品等多场景会话管理
 * 提供会话创建、查询、展示名称、最后消息、未读统计功能
 *
 * @author LunaMart
 */
public interface ChatSessionService extends IService<ChatSession> {

    /**
     * 创建或获取已有会话（支持绑定订单/商品）
     * 同一用户 + 同一商家 + 不同订单 = 创建多个独立会话
     *
     * @param user1Id 用户1ID
     * @param user2Id 用户2ID
     * @param targetType 关联类型 GOODS/ORDER
     * @param targetId 关联ID
     * @return 会话ID
     */
    Long createSession(Long user1Id, Long user2Id, String targetType, Long targetId);

    /**
     * 获取当前用户的所有聊天会话列表
     *
     * @param userId 当前用户ID
     * @return 会话列表
     */
    List<ChatSession> getMySessions(Long userId);

    /**
     * 获取会话列表（带对方名称 + 最后一条消息 + 未读消息数）
     * 用于前端会话列表展示
     *
     * @param userId 当前用户ID
     * @return 带展示信息的会话列表
     */
    List<ChatSession> getMySessionsWithUsername(Long userId);
}