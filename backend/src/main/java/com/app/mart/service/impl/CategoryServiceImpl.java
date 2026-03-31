package com.app.mart.service.impl;

import com.app.mart.entity.Category;
import com.app.mart.mapper.CategoryMapper;
import com.app.mart.service.CategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 商品分类服务实现类
 * 提供商品分类的基础 CRUD 操作
 *
 * @author LunaMart
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

}