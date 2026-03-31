package com.app.mart.mapper;

import com.app.mart.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单主表 Mapper
 * @author LunaMart
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}