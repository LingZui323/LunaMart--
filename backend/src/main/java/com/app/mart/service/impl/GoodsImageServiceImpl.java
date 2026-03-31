package com.app.mart.service.impl;

import com.app.mart.entity.Goods;
import com.app.mart.entity.GoodsImage;
import com.app.mart.mapper.GoodsImageMapper;
import com.app.mart.service.GoodsImageService;
import com.app.mart.service.GoodsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 商品图片服务实现类
 * 实现商品图片的查询、上传、封面设置、权限校验等功能
 *
 * @author LunaMart
 */
@Service
public class GoodsImageServiceImpl extends ServiceImpl<GoodsImageMapper, GoodsImage> implements GoodsImageService {

    @Resource
    private GoodsService goodsService;

    /**
     * 根据商品ID获取图片列表（按排序升序）
     */
    @Override
    public List<GoodsImage> listByGoodsId(Long goodsId) {
        if (goodsId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<GoodsImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsImage::getGoodsId, goodsId)
                .orderByAsc(GoodsImage::getSort);
        return list(wrapper);
    }

    /**
     * 保存商品图片（带商家权限校验）
     */
    @Override
    public void saveImage(GoodsImage image, Long userId) {
        if (image.getGoodsId() == null) {
            throw new RuntimeException("商品ID不能为空");
        }

        Goods goods = goodsService.getById(image.getGoodsId());
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!goods.getMerchantId().equals(userId)) {
            throw new RuntimeException("无权限操作该商品");
        }

        save(image);
    }

    /**
     * 设置指定图片为商品封面（先清空其他封面，再设置）
     */
    @Override
    public void setCover(Long imageId, Long userId) {
        // 查询图片信息
        GoodsImage image = getById(imageId);
        if (image == null) {
            throw new RuntimeException("图片不存在");
        }

        // 商家权限校验
        Goods goods = goodsService.getById(image.getGoodsId());
        if (goods == null || !goods.getMerchantId().equals(userId)) {
            throw new RuntimeException("无权限操作");
        }

        // 取消该商品所有图片的封面标记
        LambdaUpdateWrapper<GoodsImage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GoodsImage::getGoodsId, image.getGoodsId());
        updateWrapper.set(GoodsImage::getIsCover, 0);
        update(updateWrapper);

        // 将当前图片设为封面
        image.setIsCover(1);
        updateById(image);
    }

    /**
     * 获取商品的封面图片URL（优先取isCover=1）
     */
    @Override
    public String getGoodsCoverImage(Long goodsId) {
        if (goodsId == null) {
            return null;
        }

        LambdaQueryWrapper<GoodsImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsImage::getGoodsId, goodsId)
                .eq(GoodsImage::getIsCover, 1)
                .last("LIMIT 1");

        GoodsImage image = getOne(wrapper);
        return image != null ? image.getImageUrl() : null;
    }
}