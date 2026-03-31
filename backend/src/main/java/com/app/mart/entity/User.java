package com.app.mart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 系统核心用户信息，包含普通用户、商家、管理员三种角色
 *
 * @author LunaMart
 */
@Data
@TableName("user")
@ApiModel("用户实体")
public class User {

    // ====================== 角色常量 ======================
    /** 普通用户 */
    public static final String ROLE_USER = "USER";
    /** 商家 */
    public static final String ROLE_MERCHANT = "MERCHANT";
    /** 管理员 */
    public static final String ROLE_ADMIN = "ADMIN";
    /** 超级管理员 */
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    // ====================== 状态常量 ======================
    /** 禁用状态 */
    public static final Integer STATUS_DISABLED = 0;
    /** 启用状态 */
    public static final Integer STATUS_ENABLED = 1;

    /**
     * 用户主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("用户ID")
    private Long id;

    /**
     * 登录用户名
     */
    @ApiModelProperty("用户名")
    private String username;

    /**
     * 登录密码（BCrypt加密存储）
     */
    @ApiModelProperty("密码(BCrypt加密)")
    private String password;

    /**
     * 绑定邮箱（支持QQ邮箱）
     */
    @ApiModelProperty("QQ邮箱")
    private String email;

    /**
     * 用户角色
     */
    @ApiModelProperty("角色：USER-用户 / MERCHANT-商家 / ADMIN-管理员")
    private String role;

    /**
     * 微信登录OpenID
     */
    @ApiModelProperty("微信openid")
    private String wxOpenid;

    /**
     * QQ登录OpenID
     */
    @ApiModelProperty("QQ openid")
    private String qqOpenid;

    /**
     * 账号状态
     */
    @ApiModelProperty("状态：0-禁用 1-启用")
    private Integer status;

    /**
     * 商家申请审核状态
     */
    @ApiModelProperty("商家申请状态")
    private String merchantApplyStatus;

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