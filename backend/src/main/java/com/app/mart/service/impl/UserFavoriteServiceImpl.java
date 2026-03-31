package com.app.mart.service.impl;

import com.app.mart.common.result.R;
import com.app.mart.entity.Goods;
import com.app.mart.entity.Merchant;
import com.app.mart.entity.UserFavorite;
import com.app.mart.mapper.UserFavoriteMapper;
import com.app.mart.service.GoodsImageService;
import com.app.mart.service.GoodsService;
import com.app.mart.service.MerchantService;
import com.app.mart.service.UserFavoriteService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户收藏服务实现类
 * 实现商品收藏、商家收藏、取消收藏、查询收藏列表等功能
 *
 * @author LunaMart
 */
@Service
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite> implements UserFavoriteService {

    private final GoodsService goodsService;
    private final MerchantService merchantService;
    private final GoodsImageService goodsImageService;

    /**
     * 构造器注入依赖
     */
    public UserFavoriteServiceImpl(GoodsService goodsService,
                                   MerchantService merchantService,
                                   GoodsImageService goodsImageService) {
        this.goodsService = goodsService;
        this.merchantService = merchantService;
        this.goodsImageService = goodsImageService;
    }

    /**
     * 允许的收藏类型
     */
    private static final String[] ALLOWED_TYPES = {"GOODS", "MERCHANT"};

    /**
     * 添加收藏（商品/商家）
     * 校验参数合法性、防止重复收藏、更新商品收藏数量
     */
    @Override
    public R<Boolean> addFavorite(Long userId, String targetType, Long targetId) {
        if (userId == null || !StringUtils.hasText(targetType) || targetId == null || targetId <= 0) {
            return R.fail("参数不合法");
        }

        // 校验收藏类型是否合法
        boolean validType = false;
        for (String type : ALLOWED_TYPES) {
            if (type.equals(targetType)) {
                validType = true;
                break;
            }
        }
        if (!validType) {
            return R.fail("不支持的收藏类型");
        }

        // 判断是否重复收藏
        if (isFavorite(userId, targetType, targetId)) {
            return R.fail("您已收藏过该内容，请勿重复收藏");
        }

        // 保存收藏记录
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setTargetType(targetType);
        favorite.setTargetId(targetId);
        save(favorite);

        // 商品收藏数 +1
        if ("GOODS".equals(targetType)) {
            goodsService.incrCollectCount(targetId);
        }

        return R.ok(true);
    }

    /**
     * 取消收藏
     * 同时更新商品收藏数量
     */
    @Override
    public R<Boolean> removeFavorite(Long userId, String targetType, Long targetId) {
        if (userId == null || !StringUtils.hasText(targetType) || targetId == null || targetId <= 0) {
            return R.fail("参数不合法");
        }

        boolean success = remove(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getTargetType, targetType)
                .eq(UserFavorite::getTargetId, targetId));

        if (!success) {
            return R.fail("未找到该收藏记录");
        }

        // 商品收藏数 -1
        if ("GOODS".equals(targetType)) {
            goodsService.decrCollectCount(targetId);
        }

        return R.ok(true);
    }

    /**
     * 获取用户收藏列表
     * 自动封装商品/商家详情及商品图片信息
     */
    @Override
    public List<Map<String, Object>> getFavorites(Long userId, String targetType) {
        if (userId == null || !StringUtils.hasText(targetType)) {
            return new ArrayList<>();
        }

        List<UserFavorite> favorites = list(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getTargetType, targetType)
                .orderByDesc(UserFavorite::getCreateTime));

        List<Map<String, Object>> result = new ArrayList<>();

        for (UserFavorite fav : favorites) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", fav.getId());
            item.put("targetType", fav.getTargetType());
            item.put("targetId", fav.getTargetId());
            item.put("createTime", fav.getCreateTime());

            // 封装商品详情 + 图片
            if ("GOODS".equals(targetType)) {
                Goods goods = goodsService.getById(fav.getTargetId());
                goods.setImageList(goodsImageService.listByGoodsId(fav.getTargetId()));
                item.put("goods", goods);
            }

            // 封装商家详情
            if ("MERCHANT".equals(targetType)) {
                Merchant merchant = merchantService.getById(fav.getTargetId());
                item.put("merchant", merchant);
            }

            result.add(item);
        }

        return result;
    }

    /**
     * 判断用户是否已收藏指定目标
     */
    @Override
    public boolean isFavorite(Long userId, String targetType, Long targetId) {
        if (userId == null || !StringUtils.hasText(targetType) || targetId == null || targetId <= 0) {
            return false;
        }

        return count(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getTargetType, targetType)
                .eq(UserFavorite::getTargetId, targetId)) > 0;
    }
}