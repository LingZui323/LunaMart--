package com.app.mart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单主表实体类
 * 存储订单核心信息，包含状态流转、地址快照、金额、关联订单项等
 *
 * @author LunaMart
 */
@Data
@TableName("`order`")
@ApiModel("订单主表")
public class Order {

    // ====================== 订单状态常量 ======================
    /** 待支付 */
    public static final String PENDING_PAY = "PENDING_PAY";
    /** 已支付 */
    public static final String PAID = "PAID";
    /** 已发货 */
    public static final String DELIVERED = "DELIVERED";
    /** 已完成（确认收货） */
    public static final String RECEIVED = "RECEIVED";
    /** 已取消 */
    public static final String CANCELLED = "CANCELLED";
    /** 已退款（售后成功） */
    public static final String REFUNDED = "REFUNDED";

    /**
     * 订单主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("订单ID")
    private Long id;

    /**
     * 订单编号
     */
    @ApiModelProperty("订单号")
    private String orderNo;

    /**
     * 下单用户ID
     */
    @ApiModelProperty("用户ID")
    private Long userId;

    /**
     * 所属商家ID
     */
    @ApiModelProperty("商家ID")
    private Long merchantId;

    /**
     * 订单总金额
     */
    @ApiModelProperty("总金额")
    private BigDecimal totalAmount;

    /**
     * 订单状态
     */
    @ApiModelProperty("状态：PENDING_PAY-待支付 / PAID-已支付 / DELIVERED-已发货 / RECEIVED-已完成 / CANCELLED-已取消 / REFUNDED-已退款")
    private String status;

    /**
     * 收货地址ID
     */
    @ApiModelProperty("收货地址ID")
    private Long addressId;

    /**
     * 支付时间
     */
    @ApiModelProperty("支付时间")
    private LocalDateTime payTime;

    /**
     * 支付过期时间
     */
    @ApiModelProperty("支付过期时间")
    private LocalDateTime payExpireTime;

    /**
     * 发货时间
     */
    @ApiModelProperty("发货时间")
    private LocalDateTime deliverTime;

    /**
     * 确认收货时间
     */
    @ApiModelProperty("收货时间")
    private LocalDateTime receiveTime;

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

    /**
     * 收货人姓名（快照）
     */
    @ApiModelProperty("收货人")
    private String receiver;

    /**
     * 联系电话（快照）
     */
    @ApiModelProperty("联系电话")
    private String phone;

    /**
     * 完整收货地址（快照）
     */
    @ApiModelProperty("完整收货地址")
    private String fullAddress;

    // ====================== 非数据库字段（前端展示） ======================
    /**
     * 订单商品明细列表
     */
    @TableField(exist = false)
    @ApiModelProperty("订单项列表")
    private List<OrderItem> itemList;

    /**
     * 商家名称
     */
    @TableField(exist = false)
    @ApiModelProperty("商家名称")
    private String merchantName;

    /**
     * 商品名称（多个用 / 分隔）
     */
    @TableField(exist = false)
    @ApiModelProperty("商品名称（多个用 / 分隔）")
    private String goodsNames;

    // ====================== 【管理员专用：用户名】 ======================
    /**
     * 下单用户名（管理员后台显示）
     */
    @TableField(exist = false)
    @ApiModelProperty("下单用户名")
    private String username;

    /**
     * 地址展示文字（同 fullAddress，兼容前端展示）
     */
    @TableField(exist = false)
    @ApiModelProperty("收货地址展示")
    private String addressText;
}