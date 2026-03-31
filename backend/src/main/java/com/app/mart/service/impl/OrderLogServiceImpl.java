package com.app.mart.service.impl;

import com.app.mart.entity.OrderLog;
import com.app.mart.entity.User;
import com.app.mart.mapper.OrderLogMapper;
import com.app.mart.service.OrderLogService;
import com.app.mart.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单日志服务实现类
 * 负责记录订单全流程操作日志，并支持按订单ID查询
 * 支持自动填充操作人姓名（用户/商家/管理员均从User表获取）
 *
 * @author LunaMart
 */
@Service
public class OrderLogServiceImpl extends ServiceImpl<OrderLogMapper, OrderLog> implements OrderLogService {

    /**
     * 注入用户服务：用户、商家、管理员均存在User表中
     */
    @Resource
    private UserService userService;

    /**
     * 记录订单操作日志
     *
     * @param orderId       订单ID
     * @param operatorType  操作者类型（USER/MERCHANT/ADMIN）
     * @param operatorId    操作者ID
     * @param action        操作动作
     * @param remark        备注说明
     */
    @Override
    public void log(Long orderId, String operatorType, Long operatorId, String action, String remark) {
        OrderLog log = new OrderLog();
        log.setOrderId(orderId);
        log.setOperatorType(operatorType);
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        save(log);
    }

    /**
     * 根据订单ID查询操作日志列表
     *
     * @param orderId 订单ID
     * @return 日志列表
     */
    @Override
    public List<OrderLog> getLogByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderLog::getOrderId, orderId);
        return list(wrapper);
    }

    /**
     * 查询订单日志，并自动填充操作人真实姓名
     * 统一从User表获取：用户、商家、管理员名称
     * 用于管理员后台、订单详情页展示
     *
     * @param orderId 订单ID
     * @return 带操作人姓名的日志列表
     */
    @Override
    public List<OrderLog> getLogWithOperatorName(Long orderId) {
        List<OrderLog> logList = getLogByOrderId(orderId);

        for (OrderLog log : logList) {
            // 默认操作人名称
            String operatorName = "系统";

            // 根据操作人ID查询用户信息
            User operator = userService.getById(log.getOperatorId());
            if (operator != null) {
                operatorName = operator.getUsername();
            }

            // 设置操作人姓名（瞬时字段，前端展示）
            log.setOperatorName(operatorName);
        }

        return logList;
    }
}