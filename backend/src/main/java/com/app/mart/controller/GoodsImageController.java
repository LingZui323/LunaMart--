package com.app.mart.controller;

import com.app.mart.common.result.R;
import com.app.mart.common.util.AliOssUtil;
import com.app.mart.entity.Goods;
import com.app.mart.entity.GoodsImage;
import com.app.mart.service.GoodsImageService;
import com.app.mart.service.GoodsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * 商品图片模块控制层
 * 功能：商品图片上传、删除、查询、设置封面、OSS云存储管理
 * 权限：商家可进行上传/删除/设置封面，普通用户仅可查看
 *
 * @author LunaMart
 */
@RestController
@RequestMapping("/goods/image")
@Api(tags = "商品图片模块")
public class GoodsImageController {

    private final GoodsImageService goodsImageService;
    private final AliOssUtil aliOssUtil;
    private final GoodsService goodsService;

    /**
     * 构造注入：依赖注入图片服务、OSS工具、商品服务
     */
    public GoodsImageController(GoodsImageService goodsImageService,
                                AliOssUtil aliOssUtil,
                                GoodsService goodsService) {
        this.goodsImageService = goodsImageService;
        this.aliOssUtil = aliOssUtil;
        this.goodsService = goodsService;
    }

    /**
     * 【商家】上传商品图片到阿里云OSS
     * 上传成功后自动保存图片记录到数据库
     *
     * @param file    图片文件
     * @param goodsId 商品ID
     * @param userId  当前登录商家ID
     * @return 图片访问URL
     */
    @PostMapping("/upload")
    @ApiOperation("【商家】上传商品图片到阿里云OSS")
    public R<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("goodsId") Long goodsId,
            @ApiIgnore @RequestAttribute Long userId) {
        try {
            // 上传图片到阿里云OSS
            String imageUrl = aliOssUtil.upload(file);

            // 构建图片实体并保存到数据库
            GoodsImage image = new GoodsImage();
            image.setGoodsId(goodsId);
            image.setImageUrl(imageUrl);
            image.setSort(0);
            image.setIsCover(0);
            goodsImageService.saveImage(image, userId);

            return R.ok(imageUrl);
        } catch (Exception e) {
            return R.fail("上传失败：" + e.getMessage());
        }
    }

    /**
     * 公开接口：查询指定商品的所有图片列表
     * 用于商品详情页、商品列表页展示图片
     *
     * @param goodsId 商品ID
     * @return 图片列表
     */
    @GetMapping("/list/{goodsId}")
    @ApiOperation("查询商品图片列表（公开）")
    public R<List<GoodsImage>> list(@PathVariable Long goodsId) {
        return R.ok(goodsImageService.listByGoodsId(goodsId));
    }

    /**
     * 【商家】删除商品图片
     * 同时删除 OSS 云端文件 + 数据库记录
     * 会校验当前登录用户是否为商品所属商家
     *
     * @param id     图片ID
     * @param userId 登录商家ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @ApiOperation("【商家】删除商品图片")
    public R<String> delete(
            @PathVariable Long id,
            @ApiIgnore @RequestAttribute Long userId) {
        // 查询图片记录是否存在
        GoodsImage image = goodsImageService.getById(id);
        if (image == null) {
            return R.fail("图片不存在");
        }

        // 校验操作权限：只能删除自己店铺的商品图片
        Goods goods = goodsService.getById(image.getGoodsId());
        if (goods == null || !goods.getMerchantId().equals(userId)) {
            return R.fail("无权删除该图片");
        }

        // 1. 删除OSS云端文件
        aliOssUtil.delete(image.getImageUrl());
        // 2. 删除数据库图片记录
        goodsImageService.removeById(id);

        return R.ok("删除成功");
    }

    /**
     * 【商家】将某张图片设置为商品封面
     * 同一商品只能有一张封面
     *
     * @param id     图片ID
     * @param userId 登录商家ID
     * @return 设置结果
     */
    @PutMapping("/set-cover/{id}")
    @ApiOperation("【商家】设置商品封面图")
    public R<String> setCover(
            @PathVariable Long id,
            @ApiIgnore @RequestAttribute Long userId) {
        goodsImageService.setCover(id, userId);
        return R.ok("设置封面成功");
    }
}