package com.app.mart.controller;

import com.app.mart.common.result.R;
import com.app.mart.entity.Merchant;
import com.app.mart.service.MerchantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * 商家模块控制层
 * 功能：商家入驻申请、资料修改、信息查询、管理员审核、冻结管理
 * 权限：普通用户、商家、管理员、超级管理员
 *
 * @author LunaMart
 */
@RestController
@RequestMapping("/merchant")
@Api(tags = "商家模块")
public class MerchantController {

    private final MerchantService merchantService;

    /**
     * 构造注入：依赖注入商家业务服务
     */
    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    /**
     * 【用户端】根据商家ID查询公开店铺信息
     * 只返回脱敏后的公开信息，不返回敏感数据
     *
     * @param merchantId 商家ID
     * @return 商家公开信息
     */
    @GetMapping("/{merchantId}")
    @ApiOperation("【用户端】根据商家ID获取商家公开信息")
    public R<Merchant> getMerchantById(@PathVariable Long merchantId) {
        Merchant merchant = merchantService.getById(merchantId);
        if (merchant == null) {
            return R.fail("商家不存在");
        }

        // 只返回公开信息
        Merchant result = new Merchant();
        result.setId(merchant.getId());
        result.setShopName(merchant.getShopName());
        result.setStatus(merchant.getStatus());
        result.setCreateTime(merchant.getCreateTime());
        return R.ok(result);
    }

    /**
     * 用户申请成为商家，提交店铺资料
     * 提交后状态变为待审核，需要管理员审核通过
     *
     * @param merchant 商家信息
     * @param userId   当前登录用户ID
     * @return 申请结果
     */
    @PostMapping("/apply")
    @ApiOperation("申请成为商家（提交店铺资料）")
    public R<Boolean> applyMerchant(
            @RequestBody Merchant merchant,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        return merchantService.applyMerchant(merchant, userId);
    }

    /**
     * 【商家】修改自己的店铺资料
     * 必须是店铺所属商家才能操作，接口会进行权限校验
     *
     * @param merchant 店铺资料
     * @param userId   登录商家ID
     * @return 修改结果
     */
    @PutMapping("/update")
    @ApiOperation("修改店铺资料")
    public R<Boolean> updateMerchant(
            @RequestBody Merchant merchant,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        return merchantService.updateMerchant(merchant, userId);
    }

    /**
     * 【商家】获取当前登录用户对应的商家信息
     * 用于商家中心展示店铺信息
     *
     * @param userId 登录用户ID
     * @return 商家信息
     */
    @GetMapping("/my")
    @ApiOperation("查询我的商家信息")
    public R<Merchant> getMyMerchant(@ApiIgnore @RequestAttribute("userId") Long userId) {
        return R.ok(merchantService.getMyMerchant(userId));
    }

    /**
     * 【管理员】查询所有商家列表
     * 可按审核状态筛选：待审核、已通过、已驳回、已冻结等
     *
     * @param status 商家状态
     * @return 商家列表
     */
    @GetMapping("/list")
    @ApiOperation("管理员：查询所有商家（可按状态筛选）")
    public R<List<Merchant>> listMerchants(@RequestParam(required = false) String status) {
        return R.ok(merchantService.listMerchants(status));
    }

    /**
     * 【管理员】审核商家入驻申请
     * 可操作：通过 / 驳回 / 冻结
     *
     * @param merchantId 商家ID
     * @param status     审核后状态
     * @return 审核结果
     */
    @PutMapping("/audit/{merchantId}")
    @ApiOperation("管理员：审核商家")
    public R<Boolean> auditMerchant(
            @PathVariable Long merchantId,
            @RequestParam String status) {
        return merchantService.auditMerchant(merchantId, status);
    }

    /**
     * 【超级管理员】冻结违规商家
     * 冻结后商家无法上架商品、接收订单、经营店铺
     *
     * @param merchantId     商家ID
     * @param currentUserId  当前管理员ID
     * @return 冻结结果
     */
    @PutMapping("/freeze/{merchantId}")
    @ApiOperation("【超级管理员】冻结商家")
    public R<Boolean> freezeMerchant(
            @PathVariable Long merchantId,
            @ApiIgnore @RequestAttribute("userId") Long currentUserId) {
        // 校验：不允许冻结自己账号
        if (merchantId.equals(currentUserId)) {
            return R.fail("不能冻结自己的账号");
        }
        return merchantService.freezeMerchant(merchantId);
    }
}