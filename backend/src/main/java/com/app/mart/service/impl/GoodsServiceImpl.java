package com.app.mart.service.impl;

import com.app.mart.entity.AiAuditLog;
import com.app.mart.entity.Goods;
import com.app.mart.entity.GoodsImage;
import com.app.mart.mapper.GoodsMapper;
import com.app.mart.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品服务实现类
 * 实现商品发布、修改、上下架、AI审核、人工审核、热度计算、统计计数等功能
 *
 * @author LunaMart
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Resource
    @Lazy
    private AiService aiService;

    @Resource
    private GoodsImageService goodsImageService;

    @Resource
    private AiAuditLogService aiAuditLogService;

    /**
     * 商家保存商品草稿
     */
    @Override
    public void saveGoods(Goods goods, Long userId) {
        goods.setMerchantId(userId);
        goods.setStatus("DRAFT");
        save(goods);
        //保存商品图片（修复无ID问题）
        List<GoodsImage> imageList = goods.getImageList();
        if (imageList != null && !imageList.isEmpty()) {
            for (GoodsImage image : imageList) {
                image.setGoodsId(goods.getId());
                goodsImageService.save(image);
            }
        }
    }



    /**
     * 提交商品进行AI内容审核
     */
    @Override
    public void submitToAiAudit(Long goodsId, Long userId) {
        Goods goods = getById(goodsId);
        if (goods == null || !goods.getMerchantId().equals(userId)) {
            return;
        }
        if (!"DRAFT".equals(goods.getStatus())) {
            return;
        }

        // 更新状态为AI审核中
        LambdaUpdateWrapper<Goods> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Goods::getId, goodsId).set(Goods::getStatus, "PENDING_AI_AUDIT");
        update(wrapper);

        // 获取商品第一张图片
        List<GoodsImage> images = goodsImageService.listByGoodsId(goodsId);
        String imageUrl = null;
        if (images != null && !images.isEmpty()) {
            imageUrl = images.get(0).getImageUrl();
        }

        // ====================== 真实AI审核 ======================
        boolean pass = false;
        if (StringUtils.hasText(imageUrl)) {
            pass = aiService.imageCensorByUrl(imageUrl);
        } else {
            // 无图片，默认AI通过
            pass = true;
        }

        // ====================== 真实记录审核日志 ======================
        AiAuditLog log = new AiAuditLog();
        log.setTargetType("GOODS");
        log.setTargetId(goodsId);
        log.setContent("商品：" + goods.getTitle() + "，图片：" + imageUrl);
        log.setResult(pass ? "PASS" : "REJECT");
        log.setReason(pass ? "AI审核通过" : "AI审核疑似违规，转入人工复核");
        log.setCreateTime(LocalDateTime.now());
        aiAuditLogService.insertLog(log);

        // AI无论结果如何，都进入人工审核
        aiAuditGoods(goodsId, pass);
    }

    /**
     * 处理AI审核结果，更新商品状态
     * AI无论通过/不通过，都进入【待人工审核】
     */
    @Override
    public void aiAuditGoods(Long goodsId, boolean pass) {
        Goods goods = getById(goodsId);
        if (goods == null || !"PENDING_AI_AUDIT".equals(goods.getStatus())) {
            return;
        }

        LambdaUpdateWrapper<Goods> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Goods::getId, goodsId);

        // 关键：AI无论成功失败，都进入人工审核
        wrapper.set(Goods::getStatus, "PENDING_AUDIT");

        update(wrapper);
    }

    /**
     * 商家修改商品（草稿 & 已上架均可编辑，编辑后重新审核）
     */
    @Override
    public void updateGoods(Goods goods, Long userId) {
        Goods old = getById(goods.getId());
        if (old == null || !old.getMerchantId().equals(userId)) {
            return;
        }

        // 允许修改：草稿 或 已上架
        boolean canEdit = "DRAFT".equals(old.getStatus()) || "ON_SALE".equals(old.getStatus());
        if (!canEdit) {
            return;
        }

        // 覆盖信息
        goods.setMerchantId(userId);

        // ====================== 核心逻辑 ======================
        // 修改后 → 强制回到草稿，等待重新提交审核
        goods.setStatus("DRAFT");

        // 更新商品
        updateById(goods);

        // ====================== 自动重新提交审核 ======================
        submitToAiAudit(goods.getId(), userId);
    }

    /**
     * 商家自主下架商品
     */
    @Override
    public void offlineGoods(Long goodsId, Long userId) {
        Goods goods = getById(goodsId);
        if (goods == null || !goods.getMerchantId().equals(userId)) {
            return;
        }
        LambdaUpdateWrapper<Goods> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Goods::getId, goodsId).set(Goods::getStatus, "OUT_OF_STOCK");
        update(wrapper);
    }

    /**
     * 商家重新上架商品
     */
    @Override
    public void onlineGoods(Long goodsId, Long userId) {
        Goods goods = getById(goodsId);
        if (goods == null || !goods.getMerchantId().equals(userId)) {
            return;
        }
        // 只有 下架/草稿/驳回 的商品可以重新上架
        if (!"OUT_OF_STOCK".equals(goods.getStatus()) &&
                !"DRAFT".equals(goods.getStatus()) &&
                !"REJECTED".equals(goods.getStatus())) {
            return;
        }

        // 重新上架 → 直接回到上架状态（如果你想重新审核，就设为 DRAFT 然后提交审核）
        LambdaUpdateWrapper<Goods> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Goods::getId, goodsId)
                .set(Goods::getStatus, "ON_SALE");
        update(wrapper);
    }

    /**
     * 查询当前商家的所有商品
     */
    @Override
    public List<Goods> listByUserId(Long userId) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getMerchantId, userId);
        wrapper.orderByDesc(Goods::getCreateTime);
        List<Goods> list = list(wrapper);
        list.forEach(this::fillGoodsImages);
        return list;
    }

    /**
     * 前台商品列表查询（关键词、分类、排序）
     */
    @Override
    public List<Goods> listGoods(String keyword, Long categoryId, String sort) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Goods::getTitle, keyword);
        }
        if (categoryId != null) {
            wrapper.eq(Goods::getCategoryId, categoryId);
        }
        wrapper.eq(Goods::getStatus, "ON_SALE");

        // 排序逻辑
        if ("price_asc".equals(sort)) {
            wrapper.orderByAsc(Goods::getPrice);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(Goods::getPrice);
        } else if ("hot".equals(sort)) {
            wrapper.orderByDesc(Goods::getHotScore);
        } else {
            wrapper.orderByDesc(Goods::getCreateTime);
        }

        List<Goods> goodsList = list(wrapper);
        goodsList.forEach(this::fillGoodsImages);
        return goodsList;
    }

    /**
     * 查询待人工审核的商品列表
     */
    @Override
    public List<Goods> listPendingAudit() {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getStatus, "PENDING_AUDIT");
        wrapper.orderByDesc(Goods::getCreateTime);
        List<Goods> goodsList = list(wrapper);
        goodsList.forEach(this::fillGoodsImages);
        return goodsList;
    }

    /**
     * 填充商品图片列表 + 封面图
     */
    private void fillGoodsImages(Goods goods) {
        if (goods == null) {
            return;
        }
        Long goodsId = goods.getId();
        goods.setImageList(goodsImageService.listByGoodsId(goodsId));
        goods.setCoverImage(goodsImageService.getGoodsCoverImage(goodsId));
    }

    /**
     * 重写getById，自动填充图片信息
     */
    @Override
    public Goods getById(Serializable id) {
        Goods goods = super.getById(id);
        if (goods != null && id instanceof Long) {
            fillGoodsImages(goods);
        }
        return goods;
    }

    /**
     * 商品浏览量 +1
     */
    @Override
    public void incrViewCount(Long goodsId) {
        if (getById(goodsId) == null) {
            return;
        }
        update(new LambdaUpdateWrapper<Goods>()
                .eq(Goods::getId, goodsId)
                .setSql("view_count = view_count + 1"));
        calculateHotScore(goodsId);
    }

    /**
     * 商品点赞数 +1
     */
    @Override
    public void incrLikeCount(Long goodsId) {
        if (getById(goodsId) == null) {
            return;
        }
        update(new LambdaUpdateWrapper<Goods>()
                .eq(Goods::getId, goodsId)
                .setSql("like_count = like_count + 1"));
        calculateHotScore(goodsId);
    }

    /**
     * 商品点赞数 -1
     */
    @Override
    public void decrLikeCount(Long goodsId) {
        Goods goods = getById(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }

        if (goods.getLikeCount() > 0) {
            LambdaUpdateWrapper<Goods> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(Goods::getId, goodsId);
            wrapper.setSql("like_count = like_count - 1");
            update(wrapper);
        }
    }

    /**
     * 商品收藏数 +1
     */
    @Override
    public void incrCollectCount(Long goodsId) {
        if (getById(goodsId) == null) {
            return;
        }
        update(new LambdaUpdateWrapper<Goods>()
                .eq(Goods::getId, goodsId)
                .setSql("collect_count = collect_count + 1"));
        calculateHotScore(goodsId);
    }

    /**
     * 商品收藏数 -1
     */
    @Override
    public void decrCollectCount(Long goodsId) {
        Goods goods = getById(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }

        if (goods.getCollectCount() > 0) {
            LambdaUpdateWrapper<Goods> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(Goods::getId, goodsId);
            wrapper.setSql("collect_count = collect_count - 1");
            update(wrapper);
        }
    }

    /**
     * 商品评论数 +1
     */
    @Override
    public void incrCommentCount(Long goodsId) {
        if (getById(goodsId) == null) {
            return;
        }
        update(new LambdaUpdateWrapper<Goods>()
                .eq(Goods::getId, goodsId)
                .setSql("comment_count = comment_count + 1"));
        calculateHotScore(goodsId);
    }

    /**
     * 计算商品热度评分
     */
    private void calculateHotScore(Long goodsId) {
        Goods goods = getById(goodsId);
        if (goods == null) {
            return;
        }

        double score = goods.getLikeCount() * 1.0
                + goods.getCollectCount() * 2.0
                + goods.getCommentCount() * 3.0
                + goods.getViewCount() * 0.1;

        update(new LambdaUpdateWrapper<Goods>()
                .eq(Goods::getId, goodsId)
                .set(Goods::getHotScore, BigDecimal.valueOf(score)));
    }

    /**
     * 管理员人工审核商品
     */
    @Override
    public void auditGoods(Long goodsId, String status) {
        if (!"ON_SALE".equals(status) && !"REJECTED".equals(status)) {
            return;
        }

        Goods goods = getById(goodsId);
        if (goods == null) {
            return;
        }

        if (!"PENDING_AUDIT".equals(goods.getStatus())) {
            return;
        }

        update(new LambdaUpdateWrapper<Goods>()
                .eq(Goods::getId, goodsId)
                .set(Goods::getStatus, status));
    }
}