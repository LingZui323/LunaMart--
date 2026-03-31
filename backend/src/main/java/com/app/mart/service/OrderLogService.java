package com.app.mart.service;

import com.app.mart.entity.OrderLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 订单日志服务接口
 * 用于记录订单的创建、支付、发货、收货、取消、售后等操作轨迹
 *
 * @author LunaMart
 */
public interface OrderLogService extends IService<OrderLog> {

    /**
     * 记录订单操作日志
     *
     * @param orderId       订单ID
     * @param operatorType  操作者类型（USER/MERCHANT/ADMIN）
     * @param operatorId    操作者ID
     * @param action        操作动作
     * @param remark        备注/说明
     */
    void log(Long orderId, String operatorType, Long operatorId, String action, String remark);

    /**
     * 根据订单ID查询订单操作日志列表
     *
     * @param orderId 订单ID
     * @return 日志列表（按时间升序）
     */
    List<OrderLog> getLogByOrderId(Long orderId);

    /**
     * 查询订单日志 + 自动填充操作人真实姓名
     * 用于管理员后台、订单详情页展示
     */
    List<OrderLog> getLogWithOperatorName(Long orderId);
}