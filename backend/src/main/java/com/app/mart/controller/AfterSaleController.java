package com.app.mart.controller;

import com.app.mart.common.result.R;
import com.app.mart.entity.AfterSale;
import com.app.mart.service.AfterSaleService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;

/**
 * 售后模块 Controller
 * 处理：用户申请售后、商家审核、管理员审核、平台介入、分页查询等功能
 * 权限控制：用户 / 商家 / 管理员 / 超级管理员
 *
 * @author LunaMart
 */
@RestController
@RequestMapping("/after/sale")
@Api(tags = "售后管理")
public class AfterSaleController {

    @Resource
    private AfterSaleService afterSaleService;

    // ====================== 用户端接口 ======================

    /**
     * 用户申请售后（退款/退货退款）
     * @param afterSale 售后信息（订单ID、类型、原因）
     * @param userId 当前登录用户ID
     * @return 申请结果
     */
    @PostMapping("/apply")
    @ApiOperation("用户申请售后")
    public R<String> apply(@RequestBody AfterSale afterSale,
                           @ApiIgnore @RequestAttribute Long userId) {
        return afterSaleService.apply(afterSale, userId) ? R.ok("申请成功") : R.fail("申请失败");
    }

    /**
     * 用户确认退货（仅退货退款使用）
     * 商家同意后，用户标记已退货
     */
    @PostMapping("/user/return/{afterSaleId}")
    @ApiOperation("用户确认已退货（退货退款专用）")
    public R<String> userReturnGoods(@PathVariable Long afterSaleId,
                                     @ApiIgnore @RequestAttribute Long userId) {
        return afterSaleService.userReturnGoods(afterSaleId, userId) ? R.ok("已确认退货") : R.fail("操作失败");
    }

    /**
     * 用户申请平台介入（商家拒绝后可申请平台仲裁）
     * @param afterSaleId 售后单ID
     * @param userId 当前登录用户ID
     * @return 申请结果
     */
    @PostMapping("/apply/platform/{afterSaleId}")
    @ApiOperation("用户申请平台介入")
    public R<String> applyPlatform(@PathVariable Long afterSaleId,
                                   @ApiIgnore @RequestAttribute Long userId) {
        return afterSaleService.applyPlatform(afterSaleId, userId) ? R.ok("平台介入申请成功") : R.fail("申请失败");
    }

    /**
     * 用户分页查询自己的售后列表
     * 支持：状态筛选、时间范围、商品名称搜索
     * @param userId 用户ID
     * @param page 当前页
     * @param size 每页条数
     * @param status 状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param goodsName 商品名称
     * @return 分页数据
     */
    @GetMapping("/user/list")
    @ApiOperation("用户售后列表（分页 + 状态 + 时间 + 商品名搜索）")
    public R<IPage<AfterSale>> userList(
            @ApiIgnore @RequestAttribute Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String goodsName
    ) {
        return R.ok(afterSaleService.userList(userId, page, size, status, startTime, endTime, goodsName));
    }

    // ====================== 商家端接口 ======================

    /**
     * 商家查询自己店铺的售后单
     * 支持：状态、时间、商品名搜索
     * @param userId 商家ID
     * @return 分页售后列表
     */
    @GetMapping("/merchant/list")
    @ApiOperation("商家售后列表（分页 + 状态 + 时间 + 商品名搜索）")
    public R<IPage<AfterSale>> merchantList(
            @ApiIgnore @RequestAttribute Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String goodsName
    ) {
        return R.ok(afterSaleService.merchantList(userId, page, size, status, startTime, endTime, goodsName));
    }

    /**
     * 商家审核售后单
     * @param id 售后单ID
     * @param status 审核结果（APPROVED/REJECTED）
     * @param remark 审核备注
     * @param userId 商家ID
     * @return 审核结果
     */
    @PostMapping("/merchant/audit/{id}")
    @ApiOperation("商家审核售后")
    public R<String> merchantAudit(@PathVariable Long id,
                                   @RequestParam String status,
                                   @RequestParam String remark,
                                   @ApiIgnore @RequestAttribute Long userId) {
        return afterSaleService.merchantAudit(id, status, remark, userId)
                ? R.ok("审核成功")
                : R.fail("审核失败");
    }

    /**
     * 商家确认收到退货（仅退货退款使用）
     * 确认收货后自动退款
     */
    @PostMapping("/merchant/confirm/{afterSaleId}")
    @ApiOperation("商家确认收到退货（退货退款专用）")
    public R<String> merchantConfirmReceive(@PathVariable Long afterSaleId,
                                            @ApiIgnore @RequestAttribute Long userId) {
        return afterSaleService.merchantConfirmReceive(afterSaleId, userId) ? R.ok("已确认收货，退款完成") : R.fail("操作失败");
    }

    // ====================== 管理员/平台端接口 ======================

    /**
     * 管理员查询全平台售后单
     * 用于平台仲裁、监督、数据统计
     */
    @GetMapping("/admin/list")
    @ApiOperation("管理员售后列表（分页 + 状态 + 时间 + 商品名搜索）")
    public R<IPage<AfterSale>> adminList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String goodsName
    ) {
        return R.ok(afterSaleService.adminList(page, size, status, startTime, endTime, goodsName));
    }

    /**
     * 管理员（平台）最终审核售后单
     * @param id 售后单ID
     * @param status 最终状态
     * @param remark 处理备注
     * @param userId 管理员ID
     * @return 审核结果
     */
    @PostMapping("/admin/audit/{id}")
    @ApiOperation("管理员审核售后")
    public R<String> adminAudit(@PathVariable Long id,
                                @RequestParam String status,
                                @RequestParam String remark,
                                @ApiIgnore @RequestAttribute Long userId) {
        return afterSaleService.adminAudit(id, status, remark, userId)
                ? R.ok("审核成功")
                : R.fail("审核失败");
    }
}