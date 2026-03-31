package com.app.mart.service.impl;

import com.app.mart.entity.OrderItem;
import com.app.mart.mapper.OrderItemMapper;
import com.app.mart.service.OrderItemService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单项服务实现类
 * 负责订单商品明细的查询、删除等业务处理
 *
 * @author LunaMart
 */
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {

    /**
     * 根据订单ID查询该订单的所有商品明细
     *
     * @param orderId 订单ID
     * @return 订单项列表
     */
    @Override
    public List<OrderItem> getByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        return list(wrapper);
    }

    /**
     * 根据订单项ID删除订单项
     *
     * @param itemId 订单项ID
     */
    @Override
    public void deleteByItemId(Long itemId) {
        removeById(itemId);
    }
}