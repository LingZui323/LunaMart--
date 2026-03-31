package com.app.mart.service;

import com.app.mart.common.result.R;
import com.app.mart.entity.Order;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 订单服务接口
 * 提供订单创建、支付、发货、收货、取消、分页查询等功能
 *
 * @author LunaMart
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单（从购物车结算）
     *
     * @param userId    用户ID
     * @param addressId 收货地址ID
     * @return 订单ID
     */
    R<Long> createOrder(Long userId, Long addressId);

    /**
     * 创建立即购买订单（直接购买商品）
     *
     * @param userId    用户ID
     * @param goodsId   商品ID
     * @param buyNum    购买数量
     * @param addressId 收货地址ID
     * @return 订单ID
     */
    R<Long> createBuyNowOrder(Long userId, Long goodsId, Integer buyNum, Long addressId);

    /**
     * 用户取消未支付订单
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @return 操作结果
     */
    R<Boolean> cancelOrder(Long userId, Long orderId);

    /**
     * 用户支付订单
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @return 操作结果
     */
    R<Boolean> payOrder(Long userId, Long orderId);

    /**
     * 商家对已支付订单进行发货
     *
     * @param merchantId 商家ID
     * @param orderId    订单ID
     * @return 操作结果
     */
    R<Boolean> deliverOrder(Long merchantId, Long orderId);

    /**
     * 用户确认收货
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @return 操作结果
     */
    R<Boolean> receiveOrder(Long userId, Long orderId);

    /**
     * 获取订单详情（含订单项）
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 订单详情
     */
    R<Order> getOrderDetail(Long orderId, Long userId);

    /**
     * 用户分页查询自己的订单列表
     *
     * @param userId     用户ID
     * @param page       页码
     * @param size       每页条数
     * @param status     订单状态
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param goodsName  商品名称
     * @return 分页订单列表
     */
    IPage<Order> myOrders(Long userId, Integer page, Integer size,
                          String status, String startTime, String endTime, String goodsName);

    /**
     * 商家分页查询自己店铺的订单列表
     *
     * @param merchantId 商家ID
     * @param page       页码
     * @param size       每页条数
     * @param status     订单状态
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param goodsName  商品名称
     * @return 分页订单列表
     */
    IPage<Order> merchantOrders(Long merchantId, Integer page, Integer size,
                                String status, String startTime, String endTime, String goodsName);

    /**
     * 管理员分页查询平台所有订单
     *
     * @param page      页码
     * @param size      每页条数
     * @param status    订单状态
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param goodsName 商品名称
     * @return 分页订单列表
     */
    IPage<Order> adminOrders(Integer page, Integer size,
                             String status, String startTime, String endTime, String goodsName);
}