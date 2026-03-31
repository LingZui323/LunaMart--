package com.app.mart.service.impl;

import cn.hutool.core.util.StrUtil;
import com.app.mart.entity.AfterSale;
import com.app.mart.entity.Merchant;
import com.app.mart.entity.Order;
import com.app.mart.entity.OrderItem;
import com.app.mart.mapper.AfterSaleMapper;
import com.app.mart.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 售后服务实现类
 * 实现售后申请、商家/平台审核、分页查询、订单退款、关联信息填充
 * 支持两种模式：仅退款、退货退款（无快递流程）
 *
 * @author LunaMart
 */
@Service
public class AfterSaleServiceImpl extends ServiceImpl<AfterSaleMapper, AfterSale> implements AfterSaleService {

    @Resource
    private OrderService orderService;

    @Resource
    private OrderItemService orderItemService;

    @Resource
    private MerchantService merchantService;

    @Resource
    private OrderLogService orderLogService;

    /**
     * 用户申请售后
     * 支持两种类型：仅退款 / 退货退款
     */
    @Override
    public boolean apply(AfterSale afterSale, Long userId) {
        Order order = orderService.getById(afterSale.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            return false;
        }

        // 已退款订单不可再次申请
        if (Order.REFUNDED.equals(order.getStatus())) {
            return false;
        }

        // 仅允许已支付/已发货/已收货订单申请
        String status = order.getStatus();
        if (!Order.PAID.equals(status) && !Order.DELIVERED.equals(status) && !Order.RECEIVED.equals(status)) {
            return false;
        }

        // 一个订单只能申请一次售后
        LambdaQueryWrapper<AfterSale> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(AfterSale::getOrderId, afterSale.getOrderId());
        if (count(checkWrapper) > 0) {
            return false;
        }

        afterSale.setUserId(userId);
        afterSale.setMerchantId(order.getMerchantId());
        afterSale.setStatus(AfterSale.PENDING);
        afterSale.setCreateTime(LocalDateTime.now());
        afterSale.setUpdateTime(LocalDateTime.now());
        boolean saveResult = save(afterSale);

        // ====================== 记录售后日志 ======================
        if (saveResult) {
            String typeText = "RETURN".equals(afterSale.getType()) ? "退货退款" : "仅退款";
            orderLogService.log(order.getId(), "USER", userId, "AFTER_SALE_APPLY", "用户申请售后（" + typeText + "）");
        }

        return saveResult;
    }

    /**
     * 用户确认退货（仅退货退款模式使用）
     * 商家同意后，用户标记已退货
     */
    @Override
    public boolean userReturnGoods(Long afterSaleId, Long userId) {
        AfterSale afterSale = getById(afterSaleId);
        if (afterSale == null || !afterSale.getUserId().equals(userId)) {
            return false;
        }
        // 仅退货退款 且 商家已同意才能操作
        if (!"RETURN".equals(afterSale.getType()) || !AfterSale.APPROVED.equals(afterSale.getStatus())) {
            return false;
        }
        // 更新为已退货状态
        afterSale.setStatus("RETURNED");
        afterSale.setUpdateTime(LocalDateTime.now());
        return updateById(afterSale);
    }

    /**
     * 用户申请平台介入
     */
    @Override
    public boolean applyPlatform(Long afterSaleId, Long userId) {
        AfterSale afterSale = getById(afterSaleId);
        if (afterSale == null || !afterSale.getUserId().equals(userId)) {
            return false;
        }
        // 仅商家拒绝后可申请平台介入
        if (!AfterSale.REJECTED.equals(afterSale.getStatus())) {
            return false;
        }
        afterSale.setStatus(AfterSale.PLATFORM);
        afterSale.setUpdateTime(LocalDateTime.now());
        boolean updateResult = updateById(afterSale);

        // ====================== 记录日志 ======================
        if (updateResult) {
            orderLogService.log(afterSale.getOrderId(), "USER", userId, "AFTER_SALE_PLATFORM", "用户申请平台介入");
        }

        return updateResult;
    }

    /**
     * 用户分页查询售后列表
     */
    @Override
    public IPage<AfterSale> userList(Long userId, Integer page, Integer size,
                                     String status, String startTime, String endTime, String goodsName) {
        LambdaQueryWrapper<AfterSale> wrapper = buildCommonQueryWrapper(status, startTime, endTime, goodsName);
        wrapper.eq(AfterSale::getUserId, userId);
        return queryAfterSalePage(page, size, wrapper);
    }

    /**
     * 商家分页查询售后列表
     */
    @Override
    public IPage<AfterSale> merchantList(Long merchantId, Integer page, Integer size,
                                         String status, String startTime, String endTime, String goodsName) {
        LambdaQueryWrapper<AfterSale> wrapper = buildCommonQueryWrapper(status, startTime, endTime, goodsName);
        wrapper.eq(AfterSale::getMerchantId, merchantId);
        return queryAfterSalePage(page, size, wrapper);
    }

    /**
     * 管理员分页查询所有售后
     */
    @Override
    public IPage<AfterSale> adminList(Integer page, Integer size,
                                      String status, String startTime, String endTime, String goodsName) {
        LambdaQueryWrapper<AfterSale> wrapper = buildCommonQueryWrapper(status, startTime, endTime, goodsName);
        return queryAfterSalePage(page, size, wrapper);
    }

    /**
     * 商家审核售后申请
     * 仅退款：同意后直接退款
     * 退货退款：同意后等待用户退货，不直接退款
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean merchantAudit(Long id, String status, String remark, Long merchantId) {
        AfterSale afterSale = getById(id);
        if (afterSale == null || !AfterSale.PENDING.equals(afterSale.getStatus())) {
            return false;
        }
        if (!afterSale.getMerchantId().equals(merchantId)) {
            return false;
        }

        afterSale.setStatus(status);
        afterSale.setHandleRemark(remark);
        afterSale.setUpdateTime(LocalDateTime.now());
        boolean updateResult = updateById(afterSale);

        // 记录商家审核日志
        if (AfterSale.APPROVED.equals(status)) {
            orderLogService.log(afterSale.getOrderId(), "MERCHANT", merchantId, "AFTER_SALE_APPROVE", "商家同意售后申请");
        } else if (AfterSale.REJECTED.equals(status)) {
            orderLogService.log(afterSale.getOrderId(), "MERCHANT", merchantId, "AFTER_SALE_REJECT", "商家拒绝售后申请");
        }

        // ====================== 仅退款：审核通过直接退款 ======================
        if (AfterSale.APPROVED.equals(status) && "REFUND".equals(afterSale.getType())) {
            refundOrder(afterSale.getOrderId());
            // 仅退款完成后直接标记完成
            afterSale.setStatus(AfterSale.COMPLETED);
            updateById(afterSale);
            orderLogService.log(afterSale.getOrderId(), "SYSTEM", 0L, "REFUND_SUCCESS", "订单仅退款成功");
        }

        // ====================== 退货退款：审核通过不退款，等待用户退货 ======================

        return updateResult;
    }

    /**
     * 商家确认收到退货（仅退货退款）
     * 确认收货后执行退款
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean merchantConfirmReceive(Long afterSaleId, Long merchantId) {
        AfterSale afterSale = getById(afterSaleId);
        if (afterSale == null || !"RETURN".equals(afterSale.getType())) {
            return false;
        }
        if (!"RETURNED".equals(afterSale.getStatus()) || !afterSale.getMerchantId().equals(merchantId)) {
            return false;
        }

        // 确认收货 → 售后完成
        afterSale.setStatus(AfterSale.COMPLETED);
        afterSale.setUpdateTime(LocalDateTime.now());
        updateById(afterSale);

        // 退货退款最终确认 → 退款
        refundOrder(afterSale.getOrderId());
        orderLogService.log(afterSale.getOrderId(), "MERCHANT", merchantId, "RETURN_CONFIRM", "商家确认收到退货，订单退款成功");

        return true;
    }

    /**
     * 管理员审核平台介入售后
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adminAudit(Long id, String status, String remark, Long adminId) {
        AfterSale afterSale = getById(id);
        if (afterSale == null) {
            return false;
        }

        // 仅平台介入中的售后可被管理员终审为完成
        if (!AfterSale.PLATFORM.equals(afterSale.getStatus()) || !AfterSale.COMPLETED.equals(status)) {
            return false;
        }

        afterSale.setStatus(status);
        afterSale.setAdminId(adminId);
        afterSale.setHandleRemark("[平台审核] " + remark);
        afterSale.setUpdateTime(LocalDateTime.now());
        boolean res = updateById(afterSale);

        // 记录平台审核日志
        orderLogService.log(afterSale.getOrderId(), "ADMIN", adminId, "AFTER_SALE_PLATFORM_AUDIT", "平台终审通过，售后完成");

        // 平台终审通过 → 退款
        refundOrder(afterSale.getOrderId());
        orderLogService.log(afterSale.getOrderId(), "SYSTEM", 0L, "REFUND_SUCCESS", "平台介入后订单已退款");

        return res;
    }

    /**
     * 分页查询 + 填充关联信息
     */
    private IPage<AfterSale> queryAfterSalePage(Integer page, Integer size, LambdaQueryWrapper<AfterSale> wrapper) {
        IPage<AfterSale> iPage = page(new Page<>(page, size), wrapper);
        fillAllInfo(iPage.getRecords());
        return iPage;
    }

    /**
     * 统一退款逻辑：更新订单状态为已退款
     */
    private void refundOrder(Long orderId) {
        Order order = orderService.getById(orderId);
        if (order != null) {
            order.setStatus(Order.REFUNDED);
            orderService.updateById(order);
        }
    }

    /**
     * 构建公共查询条件（状态、时间、商品名）
     */
    private LambdaQueryWrapper<AfterSale> buildCommonQueryWrapper(String status, String startTime, String endTime, String goodsName) {
        LambdaQueryWrapper<AfterSale> wrapper = new LambdaQueryWrapper<>();

        appendStatusCondition(wrapper, status);
        appendTimeRangeCondition(wrapper, startTime, endTime);
        appendGoodsNameCondition(wrapper, goodsName);
        wrapper.orderByDesc(AfterSale::getCreateTime);

        return wrapper;
    }

    /**
     * 状态筛选
     */
    private void appendStatusCondition(LambdaQueryWrapper<AfterSale> wrapper, String status) {
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(AfterSale::getStatus, status);
        }
    }

    /**
     * 时间范围筛选
     */
    private void appendTimeRangeCondition(LambdaQueryWrapper<AfterSale> wrapper, String startTime, String endTime) {
        if (StrUtil.isNotBlank(startTime)) {
            wrapper.ge(AfterSale::getCreateTime, startTime);
        }
        if (StrUtil.isNotBlank(endTime)) {
            wrapper.le(AfterSale::getCreateTime, endTime);
        }
    }

    /**
     * 商品名称模糊筛选
     */
    private void appendGoodsNameCondition(LambdaQueryWrapper<AfterSale> wrapper, String goodsName) {
        if (StrUtil.isNotBlank(goodsName)) {
            wrapper.inSql(AfterSale::getOrderId,
                    "SELECT id FROM `order` WHERE id IN (" +
                            "SELECT DISTINCT order_id FROM order_item " +
                            "WHERE goods_title LIKE '%" + goodsName + "%'" +
                            ")");
        }
    }

    /**
     * 填充订单、商家、商品信息到售后列表
     */
    private void fillAllInfo(List<AfterSale> list) {
        for (AfterSale sale : list) {
            Order order = orderService.getById(sale.getOrderId());
            if (order != null) {
                sale.setOrderNo(order.getOrderNo());
                sale.setTotalAmount(order.getTotalAmount());

                Merchant merchant = merchantService.getById(order.getMerchantId());
                sale.setMerchantName(merchant != null ? merchant.getShopName() : "未知商家");
            }

            List<OrderItem> items = orderItemService.list(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, sale.getOrderId())
            );
            String goodsName = items.stream()
                    .map(OrderItem::getGoodsTitle)
                    .collect(Collectors.joining(" / "));
            sale.setGoodsName(goodsName);
        }
    }
}