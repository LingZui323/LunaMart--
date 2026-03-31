package com.app.mart.service.impl;

import cn.hutool.core.util.StrUtil;
import com.app.mart.common.result.R;
import com.app.mart.entity.*;
import com.app.mart.mapper.OrderMapper;
import com.app.mart.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 * 处理订单创建、支付、发货、收货、取消、分页查询等全流程业务
 * 支持管理员查询：用户名、商家名、收货地址、商品名称
 *
 * @author LunaMart
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    /**
     * 注入商家服务：用于查询店铺名称
     */
    @Resource
    private MerchantService merchantService;

    /**
     * 注入订单项服务
     */
    @Resource
    private OrderItemService orderItemService;

    /**
     * 注入用户服务：用于查询下单用户名（管理员展示）
     */
    @Resource
    private UserService userService;

    /**
     * 构造器注入依赖
     */
    private final CartService cartService;
    private final GoodsService goodsService;
    private final OrderLogService orderLogService;
    private final JdbcTemplate jdbcTemplate;
    private final UserAddressService userAddressService;

    public OrderServiceImpl(CartService cartService,
                            GoodsService goodsService,
                            OrderLogService orderLogService,
                            JdbcTemplate jdbcTemplate,
                            UserAddressService userAddressService) {
        this.cartService = cartService;
        this.goodsService = goodsService;
        this.orderLogService = orderLogService;
        this.jdbcTemplate = jdbcTemplate;
        this.userAddressService = userAddressService;
    }

    /**
     * 生成唯一订单号：日期 + 当日自增序号
     *
     * @return 唯一订单号
     */
    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sql = "SELECT COUNT(*) FROM `order` WHERE DATE(create_time) = CURDATE()";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        int seq = (count == null ? 0 : count) + 1;
        String seqStr = String.format("%08d", seq);
        return date + seqStr;
    }

    /**
     * 拼接省市区+详细地址为完整收货地址
     *
     * @param address 用户地址
     * @return 完整地址字符串
     */
    private String buildFullAddress(UserAddress address) {
        return address.getProvince()
                + address.getCity()
                + address.getDistrict()
                + address.getDetailAddress();
    }

    /**
     * 构建基础订单对象（抽公共逻辑）
     */
    private Order buildBaseOrder(Long userId, Long merchantId, BigDecimal totalAmount,
                                 Long addressId, UserAddress address) {
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setMerchantId(merchantId);
        order.setTotalAmount(totalAmount);
        order.setStatus(Order.PENDING_PAY);
        order.setAddressId(addressId);
        order.setReceiver(address.getReceiver());
        order.setPhone(address.getPhone());
        order.setFullAddress(buildFullAddress(address));
        order.setPayExpireTime(LocalDateTime.now().plusMinutes(30));
        return order;
    }

    /**
     * 扣减商品库存
     *
     * @param goods    商品
     * @param quantity 购买数量
     */
    private void deductGoodsStock(Goods goods, int quantity) {
        goods.setStock(goods.getStock() - quantity);
        goodsService.updateById(goods);
    }

    /**
     * 给订单项填充商品信息
     *
     * @param itemList 订单项集合
     */
    private void fillOrderItemsWithGoods(List<OrderItem> itemList) {
        for (OrderItem item : itemList) {
            Goods goods = goodsService.getById(item.getGoodsId());
            item.setGoods(goods);
        }
    }

    /**
     * 填充订单扩展信息（前端/管理员展示专用）
     * 1. 商家名称
     * 2. 下单用户名
     * 3. 收货地址展示
     * 4. 商品名称拼接
     *
     * @param order 订单对象
     */
    private void fillOrderExtraInfo(Order order) {
        // 商家名称
        Merchant merchant = merchantService.getById(order.getMerchantId());
        order.setMerchantName(merchant != null ? merchant.getShopName() : "商家");

        // 下单用户名（管理员可见）
        User user = userService.getById(order.getUserId());
        order.setUsername(user != null ? user.getUsername() : "未知用户");

        // 地址展示字段
        order.setAddressText(order.getFullAddress());

        // 拼接商品名称
        List<OrderItem> items = orderItemService.list(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        String goodsNames = items.stream()
                .map(OrderItem::getGoodsTitle)
                .collect(Collectors.joining(" / "));
        order.setGoodsNames(goodsNames);
    }

    /**
     * 构建订单公共查询条件：状态、时间、商品名
     */
    private void buildOrderQueryWrapper(LambdaQueryWrapper<Order> wrapper,
                                        String status, String startTime, String endTime, String goodsName) {
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(Order::getStatus, status);
        }
        if (StrUtil.isNotBlank(startTime)) {
            wrapper.ge(Order::getCreateTime, startTime);
        }
        if (StrUtil.isNotBlank(endTime)) {
            wrapper.le(Order::getCreateTime, endTime);
        }
        if (StrUtil.isNotBlank(goodsName)) {
            wrapper.inSql(Order::getId, "SELECT DISTINCT order_id FROM order_item WHERE goods_title LIKE '%" + goodsName + "%'");
        }
        wrapper.orderByDesc(Order::getCreateTime);
    }

    /**
     * 统一分页查询 + 填充扩展信息（消除重复代码）
     */
    private Page<Order> executeOrderPageQuery(Integer page, Integer size, LambdaQueryWrapper<Order> wrapper) {
        Page<Order> orderPage = page(new Page<>(page, size), wrapper);
        orderPage.getRecords().forEach(this::fillOrderExtraInfo);
        return orderPage;
    }

    /**
     * 创建订单（购物车结算）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Long> createOrder(Long userId, Long addressId) {
        List<Cart> cartList = cartService.getMyCart(userId);
        BigDecimal totalAmount = BigDecimal.ZERO;
        Long merchantId = null;

        for (Cart cart : cartList) {
            if (cart.getSelected() == 1) {
                Goods goods = goodsService.getById(cart.getGoodsId());
                if (goods == null) {
                    throw new RuntimeException("商品不存在");
                }
                totalAmount = totalAmount.add(goods.getPrice().multiply(new BigDecimal(cart.getQuantity())));
                merchantId = goods.getMerchantId();
            }
        }
        if (merchantId == null) {
            throw new RuntimeException("请选择要结算的商品");
        }

        UserAddress address = userAddressService.getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在或无权使用");
        }

        Order order = buildBaseOrder(userId, merchantId, totalAmount, addressId, address);
        save(order);

        for (Cart cart : cartList) {
            if (cart.getSelected() != 1) {
                continue;
            }

            Goods goods = goodsService.getById(cart.getGoodsId());
            if (!"ON_SALE".equals(goods.getStatus())) {
                throw new RuntimeException("商品已下架");
            }
            if (goods.getStock() < cart.getQuantity()) {
                throw new RuntimeException("库存不足");
            }

            deductGoodsStock(goods, cart.getQuantity());

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setUserId(userId);
            item.setGoodsId(goods.getId());
            item.setGoodsTitle(goods.getTitle());
            item.setPrice(goods.getPrice());
            item.setQuantity(cart.getQuantity());
            orderItemService.save(item);
        }

        // 删除已结算的购物车项
        cartList.stream()
                .filter(cart -> cart.getSelected() == 1)
                .forEach(cart -> cartService.removeById(cart.getId()));

        orderLogService.log(order.getId(), "USER", userId, "CREATE", "创建订单");
        return R.ok(order.getId());
    }

    /**
     * 创建立即购买订单（不经过购物车）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Long> createBuyNowOrder(Long userId, Long goodsId, Integer buyNum, Long addressId) {
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!"ON_SALE".equals(goods.getStatus())) {
            throw new RuntimeException("商品已下架");
        }
        if (buyNum == null || buyNum <= 0 || buyNum > 100) {
            throw new RuntimeException("购买数量不合法");
        }
        if (goods.getStock() < buyNum) {
            throw new RuntimeException("库存不足");
        }

        UserAddress address = userAddressService.getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在");
        }

        BigDecimal totalAmount = goods.getPrice().multiply(new BigDecimal(buyNum));

        Order order = buildBaseOrder(userId, goods.getMerchantId(), totalAmount, addressId, address);
        save(order);

        deductGoodsStock(goods, buyNum);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setUserId(userId);
        item.setGoodsId(goods.getId());
        item.setGoodsTitle(goods.getTitle());
        item.setPrice(goods.getPrice());
        item.setQuantity(buyNum);
        orderItemService.save(item);

        orderLogService.log(order.getId(), "USER", userId, "CREATE", "立即购买创建订单");
        return R.ok(order.getId());
    }

    /**
     * 用户取消未支付订单
     */
    @Override
    @Transactional
    public R<Boolean> cancelOrder(Long userId, Long orderId) {
        Order order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return R.fail("订单不存在");
        }
        if (!Order.PENDING_PAY.equals(order.getStatus())) {
            return R.fail("仅未支付订单可取消");
        }

        order.setStatus(Order.CANCELLED);
        updateById(order);
        orderLogService.log(orderId, "USER", userId, "CANCEL", "取消订单");
        return R.ok(true);
    }

    /**
     * 用户支付订单
     */
    @Override
    @Transactional
    public R<Boolean> payOrder(Long userId, Long orderId) {
        Order order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return R.fail("订单不存在");
        }
        if (!Order.PENDING_PAY.equals(order.getStatus())) {
            return R.fail("订单状态错误");
        }
        if (LocalDateTime.now().isAfter(order.getPayExpireTime())) {
            order.setStatus(Order.CANCELLED);
            updateById(order);
            return R.fail("订单已超时取消");
        }

        order.setStatus(Order.PAID);
        order.setPayTime(LocalDateTime.now());
        updateById(order);
        orderLogService.log(orderId, "USER", userId, "PAY", "支付订单");
        return R.ok(true);
    }

    /**
     * 商家发货（已支付订单才能发货）
     */
    @Override
    @Transactional
    public R<Boolean> deliverOrder(Long merchantId, Long orderId) {
        Order order = getById(orderId);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            return R.fail("无权操作");
        }
        if (!Order.PAID.equals(order.getStatus())) {
            return R.fail("订单未支付");
        }

        order.setStatus(Order.DELIVERED);
        order.setDeliverTime(LocalDateTime.now());
        updateById(order);
        orderLogService.log(orderId, "MERCHANT", merchantId, "DELIVER", "商家发货");
        return R.ok(true);
    }

    /**
     * 用户确认收货
     */
    @Override
    @Transactional
    public R<Boolean> receiveOrder(Long userId, Long orderId) {
        Order order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return R.fail("无权操作");
        }
        if (!Order.DELIVERED.equals(order.getStatus())) {
            return R.fail("订单未发货");
        }

        order.setStatus(Order.RECEIVED);
        order.setReceiveTime(LocalDateTime.now());
        updateById(order);
        orderLogService.log(orderId, "USER", userId, "RECEIVE", "确认收货");
        return R.ok(true);
    }

    /**
     * 获取订单详情（含订单项+商品）
     */
    @Override
    public R<Order> getOrderDetail(Long orderId, Long userId) {
        Order order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return R.fail("订单不存在");
        }

        List<OrderItem> itemList = orderItemService.list(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));
        fillOrderItemsWithGoods(itemList);
        order.setItemList(itemList);
        return R.ok(order);
    }

    /**
     * 用户分页查询自己的订单
     */
    @Override
    public Page<Order> myOrders(Long userId, Integer page, Integer size,
                                String status, String startTime, String endTime, String goodsName) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        buildOrderQueryWrapper(wrapper, status, startTime, endTime, goodsName);
        return executeOrderPageQuery(page, size, wrapper);
    }

    /**
     * 商家分页查询自己店铺的订单
     */
    @Override
    public Page<Order> merchantOrders(Long merchantId, Integer page, Integer size,
                                      String status, String startTime, String endTime, String goodsName) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getMerchantId, merchantId);
        buildOrderQueryWrapper(wrapper, status, startTime, endTime, goodsName);
        return executeOrderPageQuery(page, size, wrapper);
    }

    /**
     * 管理员分页查询平台所有订单
     */
    @Override
    public Page<Order> adminOrders(Integer page, Integer size,
                                   String status, String startTime, String endTime, String goodsName) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        buildOrderQueryWrapper(wrapper, status, startTime, endTime, goodsName);
        return executeOrderPageQuery(page, size, wrapper);
    }
}