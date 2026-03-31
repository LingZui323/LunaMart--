package com.app.mart.service;

import com.app.mart.entity.Cart;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 购物车服务接口
 * 提供购物车添加、修改数量、选中状态、删除、查询等功能
 *
 * @author LunaMart
 */
public interface CartService extends IService<Cart> {

    /**
     * 添加商品到购物车
     *
     * @param userId    用户ID
     * @param goodsId   商品ID
     * @param quantity  购买数量
     * @return 是否添加成功
     */
    boolean addCart(Long userId, Long goodsId, Integer quantity);

    /**
     * 修改购物车中商品的数量
     *
     * @param userId    用户ID
     * @param cartId    购物车项ID
     * @param quantity  新数量
     * @return 是否修改成功
     */
    boolean updateQuantity(Long userId, Long cartId, Integer quantity);

    /**
     * 修改购物车商品的选中状态
     *
     * @param userId    用户ID
     * @param cartId    购物车项ID
     * @param selected  选中状态 0-未选中 1-选中
     * @return 是否修改成功
     */
    boolean updateSelected(Long userId, Long cartId, Integer selected);

    /**
     * 删除购物车中的商品
     *
     * @param userId    用户ID
     * @param cartId    购物车项ID
     * @return 是否删除成功
     */
    boolean deleteCart(Long userId, Long cartId);

    /**
     * 查询当前用户的购物车列表
     *
     * @param userId    用户ID
     * @return 购物车列表
     */
    List<Cart> getMyCart(Long userId);
}