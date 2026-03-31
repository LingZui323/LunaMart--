package com.app.mart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单操作日志实体类
 * 记录订单全流程操作日志：创建、支付、发货、收货、取消、退款
 *
 * @author LunaMart
 */
@Data
@TableName("order_log")
@ApiModel("订单操作日志")
public class OrderLog {

    /**
     * 日志主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("日志ID")
    private Long id;

    /**
     * 关联订单ID
     */
    @ApiModelProperty("订单ID")
    private Long orderId;

    /**
     * 操作人类型：USER-用户 / MERCHANT-商家 / ADMIN-管理员
     */
    @ApiModelProperty("操作人类型：USER/MERCHANT/ADMIN")
    private String operatorType;

    /**
     * 操作人ID
     */
    @ApiModelProperty("操作人ID")
    private Long operatorId;

    /**
     * 操作动作
     */
    @ApiModelProperty("操作类型：CREATE/PAY/DELIVER/RECEIVE/CANCEL/REFUND")
    private String action;

    /**
     * 操作备注说明
     */
    @ApiModelProperty("备注说明")
    private String remark;

    /**
     * 操作时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    // ====================== 【展示用：操作人真实姓名】 ======================
    @TableField(exist = false)
    @ApiModelProperty("操作人姓名（前端显示）")
    private String operatorName;
}