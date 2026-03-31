package com.app.mart.service.impl;

import com.app.mart.controller.ChatWebSocket;
import com.app.mart.entity.ChatMessage;
import com.app.mart.entity.ChatSession;
import com.app.mart.entity.Merchant;
import com.app.mart.entity.User;
import com.app.mart.mapper.ChatMessageMapper;
import com.app.mart.service.ChatMessageService;
import com.app.mart.service.ChatSessionService;
import com.app.mart.service.MerchantService;
import com.app.mart.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息服务实现类
 * 实现消息发送、存储、会话更新、已读标记、WebSocket 实时推送
 *
 * @author LunaMart
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Resource
    private ChatSessionService sessionService;

    @Resource
    private UserService userService;

    @Resource
    private MerchantService merchantService;

    /**
     * 发送消息：保存消息 + 更新会话最后时间 + WebSocket 推送
     */
    @Override
    public boolean sendMsg(Long sessionId, Long senderId, String content) {
        // 保存消息记录
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setSenderId(senderId);
        msg.setContent(content);
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        boolean ok = save(msg);

        // 更新会话最后消息时间
        ChatSession session = sessionService.getById(sessionId);
        session.setLastMsgTime(LocalDateTime.now());
        sessionService.updateById(session);

        // 获取接收方 ID
        Long toUserId = senderId.equals(session.getUser1Id())
                ? session.getUser2Id()
                : session.getUser1Id();

        // WebSocket 实时推送新消息通知
        ChatWebSocket.sendToUser(toUserId, "NEW_MSG");

        return ok;
    }

    /**
     * 获取会话内消息列表（按时间正序）
     */
    @Override
    public List<ChatMessage> getMsgList(Long sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getCreateTime);
        return list(wrapper);
    }

    /**
     * 获取消息列表 + 自动绑定用户名 / 商家名
     */
    @Override
    public List<ChatMessage> getMsgListWithUsername(Long sessionId) {
        List<ChatMessage> msgList = getMsgList(sessionId);

        for (ChatMessage msg : msgList) {
            Long senderId = msg.getSenderId();

            // 查用户
            User user = userService.getById(senderId);
            if (user != null) {
                msg.setSenderName(user.getUsername());
                continue;
            }

            // 查商家
            Merchant merchant = merchantService.getById(senderId);
            if (merchant != null) {
                msg.setSenderName(merchant.getShopName());
            }
        }
        return msgList;
    }

    /**
     * 标记会话中对方消息为已读
     */
    @Override
    public void readMsg(Long sessionId, Long userId) {
        LambdaUpdateWrapper<ChatMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .ne(ChatMessage::getSenderId, userId)
                .set(ChatMessage::getIsRead, 1);
        update(wrapper);
    }

    // ====================== 【新增：前端会话列表必须的 2 个方法】 ======================

    /**
     * 获取会话最后一条消息（用于会话列表预览）
     */
    @Override
    public ChatMessage getLastMessage(Long sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .orderByDesc(ChatMessage::getCreateTime)
                .last("LIMIT 1");
        return getOne(wrapper);
    }

    /**
     * 统计当前用户在该会话的未读消息数量（小红点）
     */
    @Override
    public long countUnread(Long sessionId, Long userId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .ne(ChatMessage::getSenderId, userId)
                .eq(ChatMessage::getIsRead, ChatMessage.NOT_READ);
        return count(wrapper);
    }
}