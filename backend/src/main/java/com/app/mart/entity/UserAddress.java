package com.app.mart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户收货地址实体类
 * 管理用户的收货地址信息，支持设置默认地址
 *
 * @author LunaMart
 */
@Data
@TableName("user_address")
@ApiModel("用户收货地址")
public class UserAddress {

    /**
     * 非默认地址
     */
    public static final Integer NOT_DEFAULT = 0;

    /**
     * 默认地址
     */
    public static final Integer DEFAULT = 1;

    /**
     * 地址主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("地址ID")
    private Long id;

    /**
     * 所属用户ID
     */
    @ApiModelProperty("用户ID")
    private Long userId;

    /**
     * 收货人姓名
     */
    @ApiModelProperty("收货人")
    private String receiver;

    /**
     * 联系电话
     */
    @ApiModelProperty("联系电话")
    private String phone;

    /**
     * 省份
     */
    @ApiModelProperty("省份")
    private String province;

    /**
     * 城市
     */
    @ApiModelProperty("城市")
    private String city;

    /**
     * 区/县
     */
    @ApiModelProperty("区/县")
    private String district;

    /**
     * 详细地址
     */
    @ApiModelProperty("详细地址")
    private String detailAddress;

    /**
     * 是否默认地址：0-否 1-是
     */
    @ApiModelProperty("是否默认：0-否 1-是")
    private Integer isDefault;

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