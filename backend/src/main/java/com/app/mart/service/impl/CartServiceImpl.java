package com.app.mart.service.impl;

import com.app.mart.entity.Cart;
import com.app.mart.entity.Goods;
import com.app.mart.mapper.CartMapper;
import com.app.mart.service.CartService;
import com.app.mart.service.GoodsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车服务实现类
 * 实现购物车添加、数量修改、状态切换、删除、查询等功能
 *
 * @author LunaMart
 */
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final GoodsService goodsService;

    /**
     * 构造器注入商品服务
     */
    public CartServiceImpl(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    /**
     * 添加商品到购物车（同一商品叠加数量，不重复创建）
     * 包含库存校验
     */
    @Override
    public boolean addCart(Long userId, Long goodsId, Integer quantity) {
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) {
            return false;
        }

        // 库存校验
        if (goods.getStock() <= 0) {
            throw new RuntimeException("商品已售罄");
        }
        if (quantity > goods.getStock()) {
            throw new RuntimeException("库存不足，最多可加入 " + goods.getStock() + " 件");
        }

        // 查询是否已存在该商品
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.eq(Cart::getGoodsId, goodsId);
        Cart exist = getOne(wrapper);

        if (exist != null) {
            // 叠加数量并再次校验库存
            int newQuantity = exist.getQuantity() + quantity;
            if (newQuantity > goods.getStock()) {
                throw new RuntimeException("库存不足");
            }
            exist.setQuantity(newQuantity);
            return updateById(exist);
        }

        // 新增购物车记录
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setGoodsId(goodsId);
        cart.setQuantity(quantity);
        cart.setSelected(1);
        return save(cart);
    }

    /**
     * 修改购物车商品数量（带库存校验）
     */
    @Override
    public boolean updateQuantity(Long userId, Long cartId, Integer quantity) {
        if (quantity < 1) {
            return false;
        }

        // 校验购物车归属
        Cart cart = getById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            return false;
        }

        // 库存校验
        Goods goods = goodsService.getById(cart.getGoodsId());
        if (goods == null || quantity > goods.getStock()) {
            throw new RuntimeException("库存不足");
        }

        return update(Cart::getQuantity, quantity, userId, cartId);
    }

    /**
     * 修改购物车选中状态（选中/取消选中）
     */
    @Override
    public boolean updateSelected(Long userId, Long cartId, Integer selected) {
        return update(Cart::getSelected, selected, userId, cartId);
    }

    /**
     * 公共更新方法：抽取重复代码，支持动态字段更新
     */
    private boolean update(SFunction<Cart, ?> column, Object value, Long userId, Long cartId) {
        LambdaUpdateWrapper<Cart> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Cart::getId, cartId);
        wrapper.eq(Cart::getUserId, userId);
        wrapper.set(column, value);
        return update(wrapper);
    }

    /**
     * 删除购物车项（仅本人可删）
     */
    @Override
    public boolean deleteCart(Long userId, Long cartId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getId, cartId);
        wrapper.eq(Cart::getUserId, userId);
        return remove(wrapper);
    }

    /**
     * 查询当前用户购物车（按更新时间倒序）
     */
    @Override
    public List<Cart> getMyCart(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.orderByDesc(Cart::getUpdateTime);
        return list(wrapper);
    }
}