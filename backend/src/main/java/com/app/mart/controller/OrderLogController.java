package com.app.mart.controller;

import com.app.mart.common.result.R;
import com.app.mart.entity.OrderLog;
import com.app.mart.service.OrderLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单日志模块控制层
 * 功能：记录订单状态流转日志，用于订单追踪、操作审计
 * 权限：仅管理员可查询，用于后台订单审核与问题排查
 *
 * @author LunaMart
 */
@RestController
@RequestMapping("/order/log")
@Api(tags = "订单日志（仅管理员）")
public class OrderLogController {

    private final OrderLogService orderLogService;

    /**
     * 构造注入：依赖注入订单日志服务
     */
    public OrderLogController(OrderLogService orderLogService) {
        this.orderLogService = orderLogService;
    }

    /**
     * 【管理员】根据订单ID查询订单操作日志
     * 展示订单从创建、支付、发货、收货到完成/取消的全部流转记录
     *
     * @param orderId 订单ID
     * @return 订单操作日志列表
     */
    @GetMapping("/{orderId}")
    @ApiOperation("管理员：根据订单ID查询日志")
    public R<List<OrderLog>> getLog(@PathVariable Long orderId) {
        // 调用 【带操作人真实姓名】 的日志方法
        return R.ok(orderLogService.getLogWithOperatorName(orderId));
    }
}