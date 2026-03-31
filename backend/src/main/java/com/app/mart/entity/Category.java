package com.app.mart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 商品分类实体类
 * 支持多级分类（1/2/3级），支持启用/禁用、排序
 *
 * @author LunaMart
 */
@Data
@TableName("category")
@ApiModel("商品分类")
public class Category {

    // ====================== 状态常量 ======================
    /** 禁用 */
    public static final Integer STATUS_DISABLED = 0;
    /** 启用 */
    public static final Integer STATUS_ENABLED = 1;

    /**
     * 分类主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("分类ID")
    private Long id;

    /**
     * 分类名称
     */
    @ApiModelProperty("分类名称")
    private String name;

    /**
     * 父分类ID（0=顶级分类）
     */
    @ApiModelProperty("父分类ID（0为顶级分类）")
    private Long parentId;

    /**
     * 分类层级：1级、2级、3级
     */
    @ApiModelProperty("分类层级：1/2/3级")
    private Integer level;

    /**
     * 排序权重（数字越小越靠前）
     */
    @ApiModelProperty("排序权重")
    private Integer sort;

    /**
     * 状态：0禁用 1启用
     */
    @ApiModelProperty("状态：0-禁用 1-启用")
    private Integer status;
}