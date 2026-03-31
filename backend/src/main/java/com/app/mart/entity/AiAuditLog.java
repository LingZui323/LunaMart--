package com.app.mart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 内容审核日志实体类
 * 记录商品、图片等内容的 AI 安全审核记录
 *
 * @author LunaMart
 */
@Data
@TableName("ai_audit_log")
@ApiModel("AI审核日志")
public class AiAuditLog {

    // ====================== 审核目标类型 ======================
    /** 商品审核 */
    public static final String TARGET_GOODS = "GOODS";

    // ====================== 审核结果 ======================
    /** 审核通过 */
    public static final String RESULT_PASS = "PASS";
    /** 审核拒绝 */
    public static final String RESULT_REJECT = "REJECT";

    /**
     * 日志主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Long id;

    /**
     * 审核目标类型
     */
    @ApiModelProperty("审核类型：GOODS-商品")
    private String targetType;

    /**
     * 审核目标ID
     */
    @ApiModelProperty("审核目标ID")
    private Long targetId;

    /**
     * 审核内容快照
     */
    @ApiModelProperty("审核内容快照")
    private String content;

    /**
     * 审核结果
     */
    @ApiModelProperty("审核结果：PASS-通过 / REJECT-拒绝")
    private String result;

    /**
     * 审核原因/备注
     */
    @ApiModelProperty("审核原因")
    private String reason;

    /**
     * 审核时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}