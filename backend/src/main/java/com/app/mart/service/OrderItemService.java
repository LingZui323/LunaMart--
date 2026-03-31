package com.app.mart.service;

import com.app.mart.entity.OrderItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 订单项服务接口
 * 处理订单商品明细相关业务
 *
 * @author LunaMart
 */
public interface OrderItemService extends IService<OrderItem> {

    /**
     * 根据订单ID查询订单项列表
     *
     * @param orderId 订单ID
     * @return 订单项集合
     */
    List<OrderItem> getByOrderId(Long orderId);

    /**
     * 根据订单项ID删除订单项
     *
     * @param itemId 订单项ID
     */
    void deleteByItemId(Long itemId);
}