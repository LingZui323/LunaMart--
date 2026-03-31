package com.app.mart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细实体类
 * 存储订单中的商品明细信息，与订单是一对多关系
 *
 * @author LunaMart
 */
@Data
@TableName("order_item")
@ApiModel("订单明细")
public class OrderItem {

    /**
     * 订单项主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("订单项ID")
    private Long id;

    /**
     * 所属订单ID
     */
    @ApiModelProperty("订单ID")
    private Long orderId;

    /**
     * 下单用户ID
     */
    @ApiModelProperty("用户ID")
    private Long userId;

    /**
     * 商品ID
     */
    @ApiModelProperty("商品ID")
    private Long goodsId;

    /**
     * 商品名称（快照）
     */
    @ApiModelProperty("商品标题")
    private String goodsTitle;

    /**
     * 商品单价（快照）
     */
    @ApiModelProperty("商品单价")
    private BigDecimal price;

    /**
     * 购买数量
     */
    @ApiModelProperty("购买数量")
    private Integer quantity;

    /**
     * 商品扩展信息（非数据库字段，前端展示使用）
     */
    @TableField(exist = false)
    @ApiModelProperty("商品信息（前端展示专用）")
    private Goods goods;
}