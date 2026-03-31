package com.app.mart.service;

import com.app.mart.common.result.R;
import com.app.mart.entity.UserFavorite;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

/**
 * 用户收藏服务接口
 * 支持收藏商品 / 商家
 * @author LunaMart
 */
public interface UserFavoriteService extends IService<UserFavorite> {

    /**
     * 添加收藏（商品/商家）
     * @param userId 用户ID
     * @param targetType 收藏类型 GOODS/MERCHANT
     * @param targetId 目标ID
     * @return 操作结果
     */
    R<Boolean> addFavorite(Long userId, String targetType, Long targetId);

    /**
     * 取消收藏
     * @param userId 用户ID
     * @param targetType 收藏类型
     * @param targetId 目标ID
     * @return 操作结果
     */
    R<Boolean> removeFavorite(Long userId, String targetType, Long targetId);

    /**
     * 获取用户收藏列表
     * @param userId 用户ID
     * @param targetType 收藏类型
     * @return 封装后的收藏数据（含商品/商家信息）
     */
    List<Map<String, Object>> getFavorites(Long userId, String targetType);

    /**
     * 判断是否已收藏
     * @param userId 用户ID
     * @param targetType 收藏类型
     * @param targetId 目标ID
     * @return 是否已收藏
     */
    boolean isFavorite(Long userId, String targetType, Long targetId);

}