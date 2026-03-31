package com.app.mart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 * 存储单条聊天消息内容、状态、发送时间
 *
 * @author LunaMart
 */
@Data
@TableName("chat_message")
@ApiModel("聊天消息")
public class ChatMessage {

    // ====================== 已读状态常量 ======================
    /** 未读 */
    public static final Integer NOT_READ = 0;
    /** 已读 */
    public static final Integer READ = 1;

    /**
     * 消息主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("消息ID")
    private Long id;

    /**
     * 所属会话ID
     */
    @ApiModelProperty("会话ID")
    private Long sessionId;

    /**
     * 发送者用户ID
     */
    @ApiModelProperty("发送人ID")
    private Long senderId;

    /**
     * 消息内容
     */
    @ApiModelProperty("消息内容")
    private String content;

    /**
     * 是否已读状态
     */
    @ApiModelProperty("是否已读：0-未读 1-已读")
    private Integer isRead;

    /**
     * 消息发送时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    // ====================== 【新加：发送者显示名称】 ======================
    /**
     * 发送者姓名（用户名 / 商家店铺名）
     * 【非数据库字段】仅用于前端展示
     */
    @ApiModelProperty("发送者姓名（前端展示用）")
    private transient String senderName;

}