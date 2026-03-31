package com.app.mart.service;

import com.app.mart.entity.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 聊天消息服务接口
 * 提供消息发送、查询、已读标记、最后消息、未读统计等即时通讯功能
 *
 * @author LunaMart
 */
public interface ChatMessageService extends IService<ChatMessage> {

    /**
     * 发送聊天消息
     */
    boolean sendMsg(Long sessionId, Long senderId, String content);

    /**
     * 获取会话的历史消息列表（按时间升序）
     */
    List<ChatMessage> getMsgList(Long sessionId);

    /**
     * 获取消息列表（带用户名/商家名）
     */
    List<ChatMessage> getMsgListWithUsername(Long sessionId);

    /**
     * 将会话中所有消息标记为已读
     */
    void readMsg(Long sessionId, Long userId);

    /**
     * 获取会话最后一条消息（用于会话列表预览）
     */
    ChatMessage getLastMessage(Long sessionId);

    /**
     * 统计会话中未读消息数量（小红点）
     */
    long countUnread(Long sessionId, Long userId);
}