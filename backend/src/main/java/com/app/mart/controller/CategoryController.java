package com.app.mart.controller;

import com.app.mart.common.result.R;
import com.app.mart.entity.Category;
import com.app.mart.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类 Controller
 * 功能：商品分类的新增、修改、删除、查询全部分类
 * 权限：查询开放 / 增删改仅超级管理员
 *
 * @author LunaMart
 */
@RestController
@RequestMapping("/category")
@Api(tags = "商品分类模块")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 构造注入
     */
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 新增商品分类
     * @param category 分类信息
     * @return 成功/失败
     */
    @PostMapping
    @ApiOperation("新增分类")
    public R<Boolean> add(@RequestBody Category category) {
        return R.ok(categoryService.save(category));
    }

    /**
     * 修改商品分类
     * @param category 分类信息
     * @return 成功/失败
     */
    @PutMapping
    @ApiOperation("修改分类")
    public R<Boolean> update(@RequestBody Category category) {
        return R.ok(categoryService.updateById(category));
    }

    /**
     * 查询所有商品分类（开放接口，前台展示使用）
     * @return 分类列表
     */
    @GetMapping("/list")
    @ApiOperation("查询所有分类")
    public R<List<Category>> list() {
        return R.ok(categoryService.list());
    }

    /**
     * 删除商品分类
     * @param id 分类ID
     * @return 成功/失败
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除分类")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(categoryService.removeById(id));
    }
}