package com.app.mart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 商品图片实体类
 * 存储商品的多张图片信息，支持设置封面图与排序
 *
 * @author LunaMart
 */
@Data
@TableName("goods_image")
@ApiModel("商品图片")
public class GoodsImage {

    // ====================== 封面常量 ======================
    /** 非封面图 */
    public static final Integer NOT_COVER = 0;
    /** 封面图 */
    public static final Integer COVER = 1;

    /**
     * 图片主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("图片ID")
    private Long id;

    /**
     * 所属商品ID
     */
    @ApiModelProperty("商品ID")
    private Long goodsId;

    /**
     * 图片访问URL
     */
    @ApiModelProperty("图片访问地址")
    private String imageUrl;

    /**
     * 排序序号（升序）
     */
    @ApiModelProperty("排序序号")
    private Integer sort;

    /**
     * 是否为商品封面
     */
    @ApiModelProperty("是否封面：0-否 1-是")
    private Integer isCover;
}