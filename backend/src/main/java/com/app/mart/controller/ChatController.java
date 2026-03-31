package com.app.mart.controller;

import com.app.mart.common.result.R;
import com.app.mart.entity.ChatMessage;
import com.app.mart.entity.ChatSession;
import com.app.mart.service.ChatMessageService;
import com.app.mart.service.ChatSessionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.List;

/**
 * 实时聊天（IM）Controller
 * 功能：会话管理、消息发送、消息记录、已读状态、订单绑定聊天
 * 权限：所有接口必须登录
 * 支持：同一用户与同一商家，不同订单 → 多个独立聊天会话
 *
 * @author LunaMart
 */
@RestController
@RequestMapping("/chat")
@Api(tags = "IM 实时聊天模块")
public class ChatController {

    @Resource
    private ChatSessionService sessionService;

    @Resource
    private ChatMessageService messageService;

    /**
     * 创建或获取聊天会话（支持绑定订单/商品）
     * 同一用户 + 同一商家 + 不同订单 = 自动创建独立会话
     */
    @PostMapping("/create")
    @ApiOperation("创建/获取会话（支持订单/商品绑定）")
    public R<Long> create(
            @RequestParam Long toUserId,
            @RequestParam(defaultValue = "ORDER") String targetType,
            @RequestParam Long targetId,
            @ApiIgnore @RequestAttribute Long userId
    ) {
        Long sessionId = sessionService.createSession(userId, toUserId, targetType, targetId);
        return R.ok(sessionId);
    }

    /**
     * 我的会话列表（带对方名称 + 最后消息 + 未读数量）
     */
    @GetMapping("/session/list")
    @ApiOperation("我的会话列表")
    public R<List<ChatSession>> sessionList(@ApiIgnore @RequestAttribute Long userId) {
        return R.ok(sessionService.getMySessionsWithUsername(userId));
    }

    /**
     * 发送聊天消息
     */
    @PostMapping("/send")
    @ApiOperation("发送消息")
    public R<String> send(
            @RequestParam Long sessionId,
            @RequestParam String content,
            @ApiIgnore @RequestAttribute Long userId
    ) {
        boolean success = messageService.sendMsg(sessionId, userId, content);
        return success ? R.ok("发送成功") : R.fail("发送失败");
    }

    /**
     * 消息记录（进入聊天自动标记已读 + 带用户名/商家名）
     */
    @GetMapping("/msg/list")
    @ApiOperation("消息记录（进入聊天自动已读）")
    public R<List<ChatMessage>> msgList(
            @RequestParam Long sessionId,
            @ApiIgnore @RequestAttribute Long userId
    ) {
        messageService.readMsg(sessionId, userId);
        return R.ok(messageService.getMsgListWithUsername(sessionId));
    }
}