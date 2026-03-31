package com.app.mart.mapper;

import com.app.mart.entity.UserFavorite;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收藏 Mapper
 * @author LunaMart
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
}