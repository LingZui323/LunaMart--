package com.app.mart.service;

import com.app.mart.entity.AfterSale;

import java.util.List;

/**
 * 用户中心查询服务（专供 AI 客服使用）
 * @author LunaMart
 */
public interface UserCenterQueryService {

    /**
     * 获取用户的所有售后单
     * @param userId 用户ID
     * @return 售后单列表
     */
    List<AfterSale> getAfterSales(Long userId);

    /**
     * 根据用户ID获取用户名
     * @param userId 用户ID
     * @return 用户名
     */
    String getUsername(Long userId);
}