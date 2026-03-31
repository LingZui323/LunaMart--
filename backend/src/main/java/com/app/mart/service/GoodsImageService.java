package com.app.mart.service;

import com.app.mart.entity.GoodsImage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品图片服务接口
 * 负责商品图片的上传、查询、封面设置等管理
 *
 * @author LunaMart
 */
public interface GoodsImageService extends IService<GoodsImage> {

    /**
     * 根据商品ID获取该商品的所有图片
     *
     * @param goodsId 商品ID
     * @return 图片列表
     */
    List<GoodsImage> listByGoodsId(Long goodsId);

    /**
     * 保存商品图片（带商家权限校验）
     *
     * @param image  图片信息
     * @param userId 当前登录商家ID
     */
    void saveImage(GoodsImage image, Long userId);

    /**
     * 将某张图片设置为商品封面
     *
     * @param imageId 图片ID
     * @param userId  商家用户ID
     */
    void setCover(Long imageId, Long userId);

    /**
     * 获取商品的封面图片URL
     *
     * @param goodsId 商品ID
     * @return 封面URL
     */
    String getGoodsCoverImage(Long goodsId);
}