package com.app.mart.service;

import com.app.mart.common.result.R;
import com.app.mart.entity.Merchant;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商家服务接口
 * 提供商家入驻申请、资料修改、审核、冻结、查询等功能
 *
 * @author LunaMart
 */
public interface MerchantService extends IService<Merchant> {

    /**
     * 用户申请成为商家
     *
     * @param merchant 商家信息
     * @param userId   申请人ID
     * @return 申请结果
     */
    R<Boolean> applyMerchant(Merchant merchant, Long userId);

    /**
     * 商家修改自己的资料
     *
     * @param merchant 商家信息
     * @param userId   当前登录商家用户ID
     * @return 修改结果
     */
    R<Boolean> updateMerchant(Merchant merchant, Long userId);

    /**
     * 根据用户ID获取对应的商家信息
     *
     * @param userId 用户ID
     * @return 商家实体
     */
    Merchant getMyMerchant(Long userId);

    /**
     * 管理员根据状态查询商家列表
     *
     * @param status 审核状态
     * @return 商家列表
     */
    List<Merchant> listMerchants(String status);

    /**
     * 管理员审核商家入驻申请
     *
     * @param merchantId 商家ID
     * @param status     审核结果
     * @return 审核操作结果
     */
    R<Boolean> auditMerchant(Long merchantId, String status);

    /**
     * 冻结商家账号（同时自动下架店铺所有商品）
     *
     * @param merchantId 商家ID
     * @return 冻结结果
     */
    R<Boolean> freezeMerchant(Long merchantId);
}