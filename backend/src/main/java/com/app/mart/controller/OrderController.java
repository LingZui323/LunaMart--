package com.app.mart.controller;

import com.app.mart.common.result.R;
import com.app.mart.entity.Order;
import com.app.mart.service.OrderService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * 订单模块控制层
 * 功能：订单创建、支付、发货、收货、取消、订单列表、订单详情
 * 权限：普通用户、商家、管理员
 *
 * @author LunaMart
 */
@RestController
@RequestMapping("/order")
@Api(tags = "订单模块")
public class OrderController {

    private final OrderService orderService;

    /**
     * 构造注入：依赖注入订单业务服务
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建订单（从购物车选中商品结算）
     * 结算后生成订单，清空购物车选中商品
     *
     * @param addressId 收货地址ID
     * @param userId    当前登录用户ID
     * @return 订单ID
     */
    @PostMapping("/create")
    @ApiOperation("创建订单（从购物车选中商品）")
    public R<Long> create(
            @RequestParam Long addressId,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        return orderService.createOrder(userId, addressId);
    }

    /**
     * 立即购买（直接购买单个商品，不经过购物车）
     * 适用于商品详情页直接购买
     *
     * @param goodsId   商品ID
     * @param buyNum    购买数量
     * @param addressId 收货地址ID
     * @param userId    用户ID
     * @return 订单ID
     */
    @PostMapping("/create/buyNow")
    @ApiOperation("【立即购买】直接购买单个商品创建订单")
    public R<Long> createBuyNow(
            @RequestParam Long goodsId,
            @RequestParam Integer buyNum,
            @RequestParam Long addressId,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        return orderService.createBuyNowOrder(userId, goodsId, buyNum, addressId);
    }

    /**
     * 取消订单（仅未支付订单可取消）
     * 取消后库存自动恢复，订单状态变为已取消
     *
     * @param id     订单ID
     * @param userId 当前用户ID
     * @return 取消结果
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单（仅未支付）")
    public R<Boolean> cancel(
            @PathVariable Long id,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        return orderService.cancelOrder(userId, id);
    }

    /**
     * 支付订单（模拟支付）
     * 支付成功后订单状态变为待发货
     *
     * @param id     订单ID
     * @param userId 用户ID
     * @return 支付结果
     */
    @PutMapping("/pay/{id}")
    @ApiOperation("支付订单")
    public R<Boolean> pay(
            @PathVariable Long id,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        return orderService.payOrder(userId, id);
    }

    /**
     * 商家发货
     * 发货后订单状态变为待收货
     *
     * @param id         订单ID
     * @param merchantId 商家ID
     * @return 发货结果
     */
    @PutMapping("/deliver/{id}")
    @ApiOperation("商家：发货")
    public R<Boolean> deliver(
            @PathVariable Long id,
            @ApiIgnore @RequestAttribute("userId") Long merchantId) {
        return orderService.deliverOrder(merchantId, id);
    }

    /**
     * 用户确认收货
     * 收货后订单完成，交易结束
     *
     * @param id     订单ID
     * @param userId 用户ID
     * @return 收货结果
     */
    @PutMapping("/receive/{id}")
    @ApiOperation("确认收货")
    public R<Boolean> receive(
            @PathVariable Long id,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        return orderService.receiveOrder(userId, id);
    }

    /**
     * 订单详情（包含：订单项、商品信息、地址信息、商家信息）
     * 用于订单详情页展示完整信息
     *
     * @param id     订单ID
     * @param userId 用户ID
     * @return 订单完整信息
     */
    @GetMapping("/detail/{id}")
    @ApiOperation("订单详情（包含订单项+地址+商品）")
    public R<Order> detail(
            @PathVariable Long id,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        return orderService.getOrderDetail(id, userId);
    }

    /**
     * 我的订单列表（分页 + 状态 + 时间 + 商品名搜索）
     * 用户端查看自己所有订单
     *
     * @param userId      用户ID
     * @param page        页码
     * @param size        每页条数
     * @param status      订单状态
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param goodsName   商品名称
     * @return 分页订单列表
     */
    @GetMapping("/my")
    @ApiOperation("我的订单（分页 + 筛选）")
    public R<IPage<Order>> my(
            @ApiIgnore @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String goodsName
    ) {
        return R.ok(orderService.myOrders(userId, page, size, status, startTime, endTime, goodsName));
    }

    /**
     * 商家查看自己店铺的订单
     * 只能查看属于自己店铺的商品订单
     *
     * @param merchantId  商家ID
     * @param page        页码
     * @param size        每页条数
     * @param status      订单状态
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param goodsName   商品名称
     * @return 店铺订单列表
     */
    @GetMapping("/merchant/list")
    @ApiOperation("商家订单（分页 + 筛选）")
    public R<IPage<Order>> merchantList(
            @ApiIgnore @RequestAttribute("userId") Long merchantId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String goodsName
    ) {
        return R.ok(orderService.merchantOrders(merchantId, page, size, status, startTime, endTime, goodsName));
    }

    /**
     * 管理员查看全平台所有订单
     * 可查看所有用户、所有商家订单，用于平台管理
     *
     * @param page        页码
     * @param size        每页条数
     * @param status      订单状态
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param goodsName   商品名称
     * @return 全平台订单列表
     */
    @GetMapping("/admin/list")
    @ApiOperation("管理员订单（分页 + 筛选）")
    public R<IPage<Order>> adminList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String goodsName
    ) {
        return R.ok(orderService.adminOrders(page, size, status, startTime, endTime, goodsName));
    }

}