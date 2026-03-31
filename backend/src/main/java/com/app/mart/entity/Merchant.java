package com.app.mart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家实体类
 * 对应数据库表：merchant
 * 功能：存储商家入驻信息、审核状态、店铺信息
 *
 * @author LunaMart
 */
@Data
@TableName("merchant")
@ApiModel("商家信息")
public class Merchant {

    // ====================== 商家状态常量 ======================
    /** 待审核 */
    public static final String PENDING = "PENDING";
    /** 已通过（正常营业） */
    public static final String APPROVED = "APPROVED";
    /** 已拒绝（入驻失败） */
    public static final String REJECTED = "REJECTED";
    /** 已冻结（禁止营业） */
    public static final String FROZEN = "FROZEN";

    /** 商家ID（主键自增） */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("商家ID")
    private Long id;

    /** 关联用户ID，一个用户只能开一个店铺 */
    @ApiModelProperty("关联用户ID（唯一）")
    private Long userId;

    /** 店铺名称 */
    @ApiModelProperty("店铺名称")
    private String shopName;

    /** 营业执照图片地址（OSS存储） */
    @ApiModelProperty("营业执照图片地址")
    private String businessLicense;

    /** 商家申请说明/备注 */
    @ApiModelProperty("商家申请说明")
    private String applyRemark;

    /**
     * 商家状态
     * PENDING：待审核
     * APPROVED：已通过
     * REJECTED：已拒绝
     * FROZEN：已冻结
     */
    @ApiModelProperty("状态：PENDING-待审核 / APPROVED-已通过 / REJECTED-已拒绝 / FROZEN-已冻结")
    private String status;

    /** 创建时间（申请时间） */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /** 更新时间（审核/修改时间） */
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}