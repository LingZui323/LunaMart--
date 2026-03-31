package com.app.mart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车实体类
 * 存储用户加入购物车的商品、数量、选中状态
 *
 * @author LunaMart
 */
@Data
@TableName("cart")
@ApiModel("购物车")
public class Cart {

    // ====================== 选中状态常量 ======================
    /** 未选中 */
    public static final Integer NOT_SELECTED = 0;
    /** 已选中 */
    public static final Integer SELECTED = 1;

    /**
     * 购物车主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("购物车ID")
    private Long id;

    /**
     * 所属用户ID
     */
    @ApiModelProperty("用户ID")
    private Long userId;

    /**
     * 商品ID
     */
    @ApiModelProperty("商品ID")
    private Long goodsId;

    /**
     * 商品购买数量
     */
    @ApiModelProperty("商品数量")
    private Integer quantity;

    /**
     * 是否选中（用于结算）
     */
    @ApiModelProperty("是否选中：0-未选中 1-已选中")
    private Integer selected;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}