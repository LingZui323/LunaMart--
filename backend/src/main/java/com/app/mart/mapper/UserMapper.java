package com.app.mart.mapper;

import com.app.mart.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 * @author LunaMart
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}