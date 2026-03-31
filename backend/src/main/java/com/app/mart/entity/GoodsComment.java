package com.app.mart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品评论实体类
 * 支持一级评论、二级回复，记录点赞数与发布时间
 *
 * @author LunaMart
 */
@Data
@TableName("goods_comment")
@ApiModel("商品评论")
public class GoodsComment {

    /**
     * 评论主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("评论ID")
    private Long id;

    /**
     * 所属商品ID
     */
    @ApiModelProperty("商品ID")
    private Long goodsId;

    /**
     * 发布评论的用户ID
     */
    @ApiModelProperty("评论用户ID")
    private Long userId;

    /**
     * 评论内容
     */
    @ApiModelProperty("评论内容")
    private String content;

    /**
     * 父评论ID（0 = 一级评论）
     */
    @ApiModelProperty("父评论ID（0为一级评论）")
    private Long parentId;

    /**
     * 评论点赞数量
     */
    @ApiModelProperty("点赞数")
    private Integer likeCount;

    /**
     * 评论发布时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    // ====================== 仅增加：前端显示用户名 ======================
    /**
     * 发布评论的用户名（前端显示）
     * 非数据库字段，只用于展示
     */
    @ApiModelProperty("发布用户名")
    @TableField(exist = false)
    private String username;
}