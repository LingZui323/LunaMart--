package com.app.mart.mapper;

import com.app.mart.entity.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天消息 Mapper
 * @author LunaMart
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}