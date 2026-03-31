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

/**
 * 售后申请实体类
 * 记录订单退款、退货退款申请，支持商家审核 + 平台介入
 *
 * @author LunaMart
 */
@Data
@TableName("after_sale")
@ApiModel("售后申请")
public class AfterSale {

    // ====================== 售后状态常量 ======================
    /** 待商家审核 */
    public static final String PENDING = "PENDING";
    /** 商家同意 */
    public static final String APPROVED = "APPROVED";
    /** 商家拒绝 */
    public static final String REJECTED = "REJECTED";
    /** 用户已退货 */
    public static final String RETURNED = "RETURNED";
    /** 平台介入 */
    public static final String PLATFORM = "PLATFORM";
    /** 已完成 */
    public static final String COMPLETED = "COMPLETED";

    // ====================== 售后类型常量 ======================
    /** 仅退款 */
    public static final String REFUND = "REFUND";
    /** 退货退款 */
    public static final String RETURN = "RETURN";

    /**
     * 售后主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("售后ID")
    private Long id;

    /**
     * 关联订单ID
     */
    @ApiModelProperty("订单ID")
    private Long orderId;

    /**
     * 申请人用户ID
     */
    @ApiModelProperty("申请用户ID")
    private Long userId;

    /**
     * 所属商家ID
     */
    @ApiModelProperty("商家ID")
    private Long merchantId;

    /**
     * 售后类型
     */
    @ApiModelProperty("售后类型：REFUND-仅退款 / RETURN-退货退款")
    private String type;

    /**
     * 售后申请原因
     */
    @ApiModelProperty("售后原因")
    private String reason;

    /**
     * 售后处理状态
     */
    @ApiModelProperty("状态：PENDING-待审核 / APPROVED-商家同意 / REJECTED-商家拒绝 / PLATFORM-平台介入 / COMPLETED-已完成")
    private String status;

    /**
     * 处理管理员ID（平台审核时使用）
     */
    @ApiModelProperty("处理管理员ID")
    private Long adminId;

    /**
     * 处理备注/说明
     */
    @ApiModelProperty("处理备注")
    private String handleRemark;

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

    // ====================== 前端展示专用字段 ======================
    @TableField(exist = false)
    @ApiModelProperty("订单编号")
    private String orderNo;

    @TableField(exist = false)
    @ApiModelProperty("订单金额")
    private BigDecimal totalAmount;

    @TableField(exist = false)
    @ApiModelProperty("商家名称")
    private String merchantName;

    @TableField(exist = false)
    @ApiModelProperty("商品名称（多个用/分隔）")
    private String goodsName;
}