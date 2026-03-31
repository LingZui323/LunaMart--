package com.app.mart.mapper;

import com.app.mart.entity.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类 Mapper
 * @author LunaMart
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}