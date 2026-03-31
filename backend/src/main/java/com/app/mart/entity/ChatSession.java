package com.app.mart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天会话实体类
 * 记录用户之间的聊天会话，可关联商品/订单
 * 支持：最后一条消息、未读消息数、对方名称展示
 *
 * @author LunaMart
 */
@Data
@TableName("chat_session")
@ApiModel("聊天会话")
public class ChatSession {

    // ====================== 关联类型常量 ======================
    /** 关联商品 */
    public static final String TARGET_GOODS = "GOODS";
    /** 关联订单 */
    public static final String TARGET_ORDER = "ORDER";

    /**
     * 会话主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("会话ID")
    private Long id;

    /**
     * 聊天用户1ID（通常是普通用户）
     */
    @ApiModelProperty("用户1ID")
    private Long user1Id;

    /**
     * 聊天用户2ID（通常是商家）
     */
    @ApiModelProperty("用户2ID")
    private Long user2Id;

    /**
     * 关联目标类型
     */
    @ApiModelProperty("关联类型：GOODS-商品 / ORDER-订单")
    private String targetType;

    /**
     * 关联目标ID
     */
    @ApiModelProperty("关联目标ID")
    private Long targetId;

    /**
     * 最后一条消息时间
     */
    @ApiModelProperty("最后消息时间")
    private LocalDateTime lastMsgTime;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    // ====================== 前端展示专用字段 ======================

    /**
     * 对方显示名称（用户名 / 商家店铺名）
     * 非数据库字段，仅用于前端展示
     */
    @ApiModelProperty("对方显示名称（前端展示）")
    private transient String targetName;

    /**
     * 最后一条消息内容
     * 非数据库字段，仅用于前端会话列表展示
     */
    @ApiModelProperty("最后一条消息内容")
    private transient String lastMessage;

    /**
     * 未读消息数量
     * 非数据库字段，仅用于前端小红点展示
     */
    @ApiModelProperty("未读消息数量")
    private transient Long unreadCount;

}