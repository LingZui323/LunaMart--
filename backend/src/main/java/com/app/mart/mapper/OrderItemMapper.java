package com.app.mart.mapper;

import com.app.mart.entity.OrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细 Mapper
 * @author LunaMart
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}