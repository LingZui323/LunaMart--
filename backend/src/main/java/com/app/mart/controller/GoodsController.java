package com.app.mart.controller;

import com.app.mart.common.result.R;
import com.app.mart.entity.Goods;
import com.app.mart.entity.Merchant;
import com.app.mart.service.GoodsImageService;
import com.app.mart.service.GoodsService;
import com.app.mart.service.MerchantService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * 商品模块控制层
 * 负责商品的发布、编辑、审核、上下架、列表查询、详情查询、统计数据等接口
 * 涵盖用户端、商家端、管理员端、AI自动审核相关业务
 *
 * @author LunaMart
 */
@RestController
@RequestMapping("/goods")
@Api(tags = "商品模块")
public class GoodsController {

    private final GoodsService goodsService;
    private final GoodsImageService goodsImageService;
    private final MerchantService merchantService;

    /**
     * 构造注入依赖
     */
    public GoodsController(GoodsService goodsService,
                           GoodsImageService goodsImageService,
                           MerchantService merchantService) {
        this.goodsService = goodsService;
        this.goodsImageService = goodsImageService;
        this.merchantService = merchantService;
    }

    // ==================== 商家：发布/修改商品 ====================

    /**
     * 【商家】保存商品信息（保存为草稿状态）
     * 商品未提交审核前可多次编辑
     */
    @PostMapping("/save")
    @ApiOperation("【商家】保存商品（草稿DRAFT）")
    public R<String> save(
            @RequestBody Goods goods,
            @ApiIgnore @RequestAttribute Long userId) {
        goodsService.saveGoods(goods, userId);
        return R.ok("保存成功");
    }

    /**
     * 【商家】修改商品信息
     * 仅草稿状态的商品允许编辑
     */
    @PostMapping("/update")
    @ApiOperation("【商家】修改商品（仅草稿可编辑）")
    public R<String> update(
            @RequestBody Goods goods,
            @ApiIgnore @RequestAttribute Long userId) {
        goodsService.updateGoods(goods, userId);
        return R.ok("修改成功");
    }

    // ==================== 商家：提交AI审核 ====================

    /**
     * 【商家】将商品提交给AI进行自动审核
     * 审核通过后进入待人工审核或直接上架
     */
    @PostMapping("/submit-ai-audit/{goodsId}")
    @ApiOperation("【商家】提交商品到AI审核")
    public R<String> submitToAiAudit(
            @PathVariable Long goodsId,
            @ApiIgnore @RequestAttribute Long userId) {
        goodsService.submitToAiAudit(goodsId, userId);
        return R.ok("提交AI审核成功");
    }

    // ==================== 商家：下架商品 ====================

    /**
     * 【商家】手动下架已上架的商品
     * 下架后用户端无法购买和查看
     */
    @PostMapping("/offline/{goodsId}")
    @ApiOperation("【商家】下架商品")
    public R<String> offline(
            @PathVariable Long goodsId,
            @ApiIgnore @RequestAttribute Long userId) {
        goodsService.offlineGoods(goodsId, userId);
        return R.ok("下架成功");
    }

    /**
     * 【商家】手动上架商品（从下架状态重新上架）
     */
    @PostMapping("/online/{goodsId}")
    @ApiOperation("【商家】重新上架商品")
    public R<String> online(
            @PathVariable Long goodsId,
            @ApiIgnore @RequestAttribute Long userId) {
        goodsService.onlineGoods(goodsId, userId);
        return R.ok("上架成功");
    }

    // ==================== 管理员：审核商品 ====================

    /**
     * 【管理员】人工审核商品
     * 可设置为通过、驳回、下架等状态
     */
    @PostMapping("/audit/{goodsId}")
    @ApiOperation("【管理员】审核商品")
    public R<String> audit(
            @PathVariable Long goodsId,
            @RequestParam String status) {
        goodsService.auditGoods(goodsId, status);
        return R.ok("审核成功");
    }

    /**
     * 【管理员】查询所有待人工审核的商品列表
     * 包含商品信息及关联图片
     */
    @GetMapping("/admin/pending-audit")
    @ApiOperation("【管理员】查看待人工审核商品（带图片）")
    public R<List<Goods>> pendingAuditList() {
        return R.ok(goodsService.listPendingAudit());
    }

    // ==================== 前台查询 ====================

    /**
     * 用户端商品列表查询
     * 支持关键词搜索、分类筛选、排序方式选择
     */
    @GetMapping("/list")
    @ApiOperation("商品列表（前台）")
    public R<List<Goods>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sort) {
        return R.ok(goodsService.listGoods(keyword, categoryId, sort));
    }

    /**
     * 【商家】查询自己店铺下的所有商品
     * 包含草稿、审核中、上架、下架等全部状态
     */
    @GetMapping("/my")
    @ApiOperation("【商家】查询自己的商品列表")
    public R<List<Goods>> myGoods(
            @ApiIgnore @RequestAttribute Long userId) {
        List<Goods> goodsList = goodsService.listByUserId(userId);
        return R.ok(goodsList);
    }

    /**
     * 商品详情接口
     * 查询单条商品完整信息，同时封装商品图片和商家信息
     */
    @GetMapping("/{id}")
    @ApiOperation("商品详情（带图片 + 商家信息）")
    public R<Goods> getGoods(@PathVariable Long id) {
        Goods goods = goodsService.getById(id);
        if (goods == null) {
            return R.fail("商品不存在");
        }

        // 封装商品图片列表
        goods.setImageList(goodsImageService.listByGoodsId(id));

        // 封装对应商家信息
        Merchant merchant = merchantService.getById(goods.getMerchantId());
        goods.setMerchant(merchant);

        return R.ok(goods);
    }

    /**
     * 用户端查看指定商家的店铺商品
     * 只展示已上架商品，按发布时间倒序
     */
    @GetMapping("/merchant/{merchantId}")
    @ApiOperation("查看某个商家的店铺商品（用户端）")
    public R<List<Goods>> merchantGoods(@PathVariable Long merchantId) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getMerchantId, merchantId);
        wrapper.eq(Goods::getStatus, "ON_SALE");
        wrapper.orderByDesc(Goods::getCreateTime);

        List<Goods> goodsList = goodsService.list(wrapper);
        // 为每个商品封装图片信息
        for (Goods goods : goodsList) {
            goods.setImageList(goodsImageService.listByGoodsId(goods.getId()));
        }
        return R.ok(goodsList);
    }

    // ==================== 商品统计：浏览数 + 点赞 + 收藏 + 评论 ====================

    /**
     * 增加商品浏览量
     * 用户每次打开商品详情时调用
     */
    @GetMapping("/incr/view/{goodsId}")
    @ApiOperation("增加商品浏览数")
    public R<String> incrView(@PathVariable Long goodsId) {
        goodsService.incrViewCount(goodsId);
        return R.ok("浏览量+1");
    }

    /**
     * 用户点赞商品
     * 同时更新商品点赞数量
     */
    @PostMapping("/incr/like/{goodsId}")
    @ApiOperation("【用户】点赞商品")
    public R<String> incrLike(@PathVariable Long goodsId) {
        goodsService.incrLikeCount(goodsId);
        return R.ok("点赞成功");
    }

    /**
     * 用户取消点赞商品
     */
    @PostMapping("/decr/like/{goodsId}")
    @ApiOperation("【用户】取消点赞")
    public R<String> decrLike(@PathVariable Long goodsId) {
        goodsService.decrLikeCount(goodsId);
        return R.ok("取消点赞成功");
    }

    /**
     * 商品评论数+1
     * 用户发布商品评论后调用
     */
    @PostMapping("/incr/comment/{goodsId}")
    @ApiOperation("【用户】发布评论后调用：评论数+1")
    public R<String> incrComment(@PathVariable Long goodsId) {
        goodsService.incrCommentCount(goodsId);
        return R.ok("评论数+1");
    }
}