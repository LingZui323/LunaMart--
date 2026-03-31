package com.app.mart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户收藏实体类
 * 支持收藏商品、收藏商家
 *
 * @author LunaMart
 */
@Data
@TableName("user_favorite")
@ApiModel("用户收藏")
public class UserFavorite {

    /**
     * 收藏目标类型：商品
     */
    public static final String TARGET_GOODS = "GOODS";

    /**
     * 收藏目标类型：商家
     */
    public static final String TARGET_MERCHANT = "MERCHANT";

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("收藏ID")
    private Long id;

    /**
     * 操作用户ID
     */
    @ApiModelProperty("用户ID")
    private Long userId;

    /**
     * 收藏类型：GOODS-商品 / MERCHANT-商家
     */
    @ApiModelProperty("收藏类型：GOODS-商品 / MERCHANT-商家")
    private String targetType;

    /**
     * 目标ID：商品ID 或 商家ID
     */
    @ApiModelProperty("收藏目标ID（商品ID/商家ID）")
    private Long targetId;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}