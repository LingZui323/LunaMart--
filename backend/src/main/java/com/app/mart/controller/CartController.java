package com.app.mart.controller;

import com.app.mart.common.result.R;
import com.app.mart.entity.Cart;
import com.app.mart.service.CartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * 购物车模块 Controller
 * 功能：加入购物车、修改数量、选中状态、删除、查询我的购物车
 * 权限：所有接口必须登录
 *
 * @author LunaMart
 */
@RestController
@RequestMapping("/cart")
@Api(tags = "购物车模块")
public class CartController {

    private final CartService cartService;

    /**
     * 构造函数注入
     */
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * 加入购物车
     * @param goodsId 商品ID
     * @param quantity 购买数量，默认1
     * @param userId 当前登录用户ID
     * @return 操作结果
     */
    @PostMapping("/add")
    @ApiOperation("加入购物车")
    public R<String> add(
            @RequestParam Long goodsId,
            @RequestParam(defaultValue = "1") Integer quantity,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        boolean success = cartService.addCart(userId, goodsId, quantity);
        return success ? R.ok("加入成功") : R.fail("加入失败");
    }

    /**
     * 修改购物车商品数量
     * @param cartId 购物车ID
     * @param quantity 新的数量
     * @param userId 当前登录用户ID
     * @return 修改结果
     */
    @PutMapping("/quantity/{cartId}")
    @ApiOperation("修改商品数量")
    public R<String> updateQuantity(
            @PathVariable Long cartId,
            @RequestParam Integer quantity,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        boolean success = cartService.updateQuantity(userId, cartId, quantity);
        return success ? R.ok("修改成功") : R.fail("修改失败");
    }

    /**
     * 修改购物车选中状态
     * @param cartId 购物车ID
     * @param selected 0=取消选中 1=选中
     * @param userId 当前登录用户ID
     * @return 修改结果
     */
    @PutMapping("/selected/{cartId}")
    @ApiOperation("修改选中状态 0=取消 1=选中")
    public R<String> updateSelected(
            @PathVariable Long cartId,
            @RequestParam Integer selected,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        boolean success = cartService.updateSelected(userId, cartId, selected);
        return success ? R.ok("修改成功") : R.fail("修改失败");
    }

    /**
     * 删除购物车商品
     * @param cartId 购物车ID
     * @param userId 当前登录用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{cartId}")
    @ApiOperation("删除购物车")
    public R<String> delete(
            @PathVariable Long cartId,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        boolean success = cartService.deleteCart(userId, cartId);
        return success ? R.ok("删除成功") : R.fail("删除失败");
    }

    /**
     * 查询当前用户的购物车列表（已关联商品信息）
     * @param userId 当前登录用户ID
     * @return 购物车列表
     */
    @GetMapping("/my")
    @ApiOperation("我的购物车")
    public R<List<Cart>> myCart(
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        return R.ok(cartService.getMyCart(userId));
    }
}