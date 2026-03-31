package com.app.mart.mapper;

import com.app.mart.entity.Cart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车 Mapper
 * @author LunaMart
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}