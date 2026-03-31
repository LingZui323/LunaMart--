package com.app.mart.mapper;

import com.app.mart.entity.UserAddress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收货地址 Mapper
 * @author LunaMart
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}