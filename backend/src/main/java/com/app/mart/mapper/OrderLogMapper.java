package com.app.mart.mapper;

import com.app.mart.entity.OrderLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单日志 Mapper
 * @author LunaMart
 */
@Mapper
public interface OrderLogMapper extends BaseMapper<OrderLog> {
}