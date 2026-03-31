package com.app.mart.controller;

import com.app.mart.common.result.R;
import com.app.mart.service.UserFavoriteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

/**
 * 用户收藏控制器
 * @author LunaMart
 */
@RestController
@RequestMapping("/favorite")
@Api(tags = "用户收藏模块")
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;

    public UserFavoriteController(UserFavoriteService userFavoriteService) {
        this.userFavoriteService = userFavoriteService;
    }

    @PostMapping("/add")
    @ApiOperation("添加收藏")
    public R<Boolean> addFavorite(
            @ApiParam(value = "收藏类型：GOODS/MERCHANT") @RequestParam String targetType,
            @ApiParam(value = "收藏目标ID") @RequestParam Long targetId,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        return userFavoriteService.addFavorite(userId, targetType, targetId);
    }

    @DeleteMapping("/remove")
    @ApiOperation("取消收藏")
    public R<Boolean> removeFavorite(
            @ApiParam(value = "收藏类型：GOODS/MERCHANT") @RequestParam String targetType,
            @ApiParam(value = "收藏目标ID") @RequestParam Long targetId,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        return userFavoriteService.removeFavorite(userId, targetType, targetId);
    }

    @GetMapping("/list")
    @ApiOperation("查询用户收藏列表")
    public R<List<Map<String, Object>>> getFavorites(
            @ApiParam(value = "收藏类型：GOODS/MERCHANT") @RequestParam String targetType,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        return R.ok(userFavoriteService.getFavorites(userId, targetType));
    }

    @GetMapping("/check")
    @ApiOperation("判断是否已收藏")
    public R<Boolean> isFavorite(
            @ApiParam(value = "收藏类型：GOODS/MERCHANT") @RequestParam String targetType,
            @ApiParam(value = "收藏目标ID") @RequestParam Long targetId,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        return R.ok(userFavoriteService.isFavorite(userId, targetType, targetId));
    }
}