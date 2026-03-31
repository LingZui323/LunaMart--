package com.app.mart.mapper;

import com.app.mart.entity.ChatSession;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天会话 Mapper
 * @author LunaMart
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}