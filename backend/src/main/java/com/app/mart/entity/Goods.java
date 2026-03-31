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
 * 商品实体类
 * 商品核心信息：包含状态、库存、价格、图片、商家关联、统计数据
 *
 * @author LunaMart
 */
@Data
@TableName("goods")
@ApiModel("商品信息")
public class Goods {

    // ====================== 商品状态常量 ======================
    /** 草稿 */
    public static final String DRAFT = "DRAFT";
    /** 待审核 */
    public static final String PENDING_AUDIT = "PENDING_AUDIT";
    /** 销售中 */
    public static final String ON_SALE = "ON_SALE";
    /** 已售罄 */
    public static final String OUT_OF_STOCK = "OUT_OF_STOCK";
    /** 审核拒绝 */
    public static final String REJECTED = "REJECTED";

    /**
     * 商品主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("商品ID")
    private Long id;

    /**
     * 所属商家ID
     */
    @ApiModelProperty("商家ID")
    private Long merchantId;

    /**
     * 商品分类ID
     */
    @ApiModelProperty("分类ID")
    private Long categoryId;

    /**
     * 商品标题
     */
    @ApiModelProperty("商品标题")
    private String title;

    /**
     * 商品描述详情
     */
    @ApiModelProperty("商品描述")
    private String description;

    /**
     * 商品单价
     */
    @ApiModelProperty("商品价格")
    private BigDecimal price;

    /**
     * 库存数量
     */
    @ApiModelProperty("库存数量")
    private Integer stock;

    /**
     * 商品状态
     */
    @ApiModelProperty("状态：DRAFT-草稿 / PENDING_AUDIT-待审核 / ON_SALE-销售中 / OUT_OF_STOCK-售罄 / REJECTED-拒绝")
    private String status;

    /**
     * 点赞数
     */
    @ApiModelProperty("点赞数")
    private Integer likeCount;

    /**
     * 收藏数
     */
    @ApiModelProperty("收藏数")
    private Integer collectCount;

    /**
     * 评论数
     */
    @ApiModelProperty("评论数")
    private Integer commentCount;

    /**
     * 浏览量
     */
    @ApiModelProperty("浏览数")
    private Integer viewCount;

    /**
     * 热度评分（用于排序推荐）
     */
    @ApiModelProperty("热度分")
    private BigDecimal hotScore;

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

    // ====================== 非数据库字段（前端展示） ======================
    /**
     * 商品图片列表
     */
    @TableField(exist = false)
    @ApiModelProperty("商品图片列表")
    private List<GoodsImage> imageList;

    /**
     * 所属商家信息
     */
    @TableField(exist = false)
    @ApiModelProperty("商家信息")
    private Merchant merchant;

    /**
     * 商品封面图URL
     */
    @TableField(exist = false)
    @ApiModelProperty("商品封面图")
    private String coverImage;
}