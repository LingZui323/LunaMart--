package com.app.mart.service;

import com.app.mart.entity.AfterSale;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 售后订单服务接口
 * 提供售后申请、审核、分页查询、平台介入等功能
 * 支持两种售后类型：仅退款、退货退款
 * @author LunaMart
 */
public interface AfterSaleService extends IService<AfterSale> {

    /**
     * 用户提交售后申请
     * @param afterSale 售后信息
     * @param userId 当前用户ID
     * @return 是否成功
     */
    boolean apply(AfterSale afterSale, Long userId);

    /**
     * 用户确认退货（仅退货退款使用）
     * @param afterSaleId 售后单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean userReturnGoods(Long afterSaleId, Long userId);

    /**
     * 用户申请平台介入
     * @param afterSaleId 售后单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean applyPlatform(Long afterSaleId, Long userId);

    // ====================== 分页查询 ======================

    /**
     * 用户端分页查询自己的售后单
     */
    IPage<AfterSale> userList(Long userId, Integer page, Integer size,
                              String status, String startTime, String endTime, String goodsName);

    /**
     * 商家端分页查询自己店铺的售后单
     */
    IPage<AfterSale> merchantList(Long merchantId, Integer page, Integer size,
                                  String status, String startTime, String endTime, String goodsName);

    /**
     * 管理员分页查询所有售后单（平台管理）
     */
    IPage<AfterSale> adminList(Integer page, Integer size,
                               String status, String startTime, String endTime, String goodsName);

    // ====================== 审核 ======================

    /**
     * 商家审核售后单
     * @param id 售后单ID
     * @param status 审核状态
     * @param remark 备注
     * @param merchantId 商家ID
     * @return 是否成功
     */
    boolean merchantAudit(Long id, String status, String remark, Long merchantId);

    /**
     * 商家确认收到退货（仅退货退款使用）
     * @param afterSaleId 售后单ID
     * @param merchantId 商家ID
     * @return 是否成功
     */
    boolean merchantConfirmReceive(Long afterSaleId, Long merchantId);

    /**
     * 管理员审核 / 平台介入处理
     * @param id 售后单ID
     * @param status 最终状态
     * @param remark 处理备注
     * @param adminId 管理员ID
     * @return 是否成功
     */
    boolean adminAudit(Long id, String status, String remark, Long adminId);
}