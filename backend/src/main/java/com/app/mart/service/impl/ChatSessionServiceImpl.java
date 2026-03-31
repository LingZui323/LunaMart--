package com.app.mart.service.impl;

import com.app.mart.entity.ChatMessage;
import com.app.mart.entity.ChatSession;
import com.app.mart.entity.Merchant;
import com.app.mart.entity.User;
import com.app.mart.mapper.ChatSessionMapper;
import com.app.mart.service.ChatMessageService;
import com.app.mart.service.ChatSessionService;
import com.app.mart.service.MerchantService;
import com.app.mart.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天会话服务实现类
 * 实现会话创建、查询、去重、关联订单/商品等功能
 * 支持：显示对方名称、最后一条消息、未读消息数
 *
 * @author LunaMart
 */
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    @Resource
    private UserService userService;

    @Resource
    private MerchantService merchantService;

    @Resource
    private ChatMessageService chatMessageService;

    /**
     * 创建或获取已有会话（支持普通聊天 + 订单/商品绑定聊天）
     * 会自动判断用户对 + 关联目标是否重复，避免重复创建会话
     * 支持：同一用户 ↔ 同一商家，不同订单 = 多个独立会话
     */
    @Override
    public Long createSession(Long user1Id, Long user2Id, String targetType, Long targetId) {
        // 查询是否已存在相同用户 + 相同关联目标的会话
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w ->
                w.eq(ChatSession::getUser1Id, user1Id).eq(ChatSession::getUser2Id, user2Id)
                        .or()
                        .eq(ChatSession::getUser1Id, user2Id).eq(ChatSession::getUser2Id, user1Id)
        );

        // 仅当关联目标不为空时，才加入查询条件（兼容普通聊天）
        if (targetType != null && targetId != null) {
            wrapper.eq(ChatSession::getTargetType, targetType);
            wrapper.eq(ChatSession::getTargetId, targetId);
        }

        ChatSession exist = getOne(wrapper);
        if (exist != null) {
            return exist.getId();
        }

        // 新建会话
        ChatSession session = new ChatSession();
        session.setUser1Id(user1Id);
        session.setUser2Id(user2Id);
        session.setTargetType(targetType);
        session.setTargetId(targetId);
        session.setLastMsgTime(LocalDateTime.now());
        save(session);

        return session.getId();
    }

    /**
     * 查询当前用户的所有会话列表，按最后消息时间倒序
     */
    @Override
    public List<ChatSession> getMySessions(Long userId) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w ->
                w.eq(ChatSession::getUser1Id, userId).or().eq(ChatSession::getUser2Id, userId)
        );
        wrapper.orderByDesc(ChatSession::getLastMsgTime);
        return list(wrapper);
    }

    /**
     * 获取会话列表 + 自动绑定对方【用户名/商家名】
     * + 【最后一条消息】
     * + 【未读消息数量】
     * 前端可直接渲染会话列表
     */
    @Override
    public List<ChatSession> getMySessionsWithUsername(Long userId) {
        List<ChatSession> sessions = getMySessions(userId);

        for (ChatSession session : sessions) {
            // 1. 找到对方ID并设置显示名称
            Long targetId = session.getUser1Id().equals(userId)
                    ? session.getUser2Id()
                    : session.getUser1Id();

            User user = userService.getById(targetId);
            if (user != null) {
                session.setTargetName(user.getUsername());
            }

            Merchant merchant = merchantService.getById(targetId);
            if (merchant != null) {
                session.setTargetName(merchant.getShopName());
            }

            // 2. 设置最后一条消息内容
            ChatMessage lastMsg = chatMessageService.getLastMessage(session.getId());
            if (lastMsg != null) {
                session.setLastMessage(lastMsg.getContent());
            } else {
                session.setLastMessage("暂无消息");
            }

            // 3. 设置未读消息数量（小红点）
            long unread = chatMessageService.countUnread(session.getId(), userId);
            session.setUnreadCount(unread);
        }
        return sessions;
    }
}