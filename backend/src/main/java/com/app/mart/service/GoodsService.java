package com.app.mart.service;

import com.app.mart.entity.Goods;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品服务接口
 * 提供商品发布、修改、上下架、查询、统计、人工/AI审核等功能
 *
 * @author LunaMart
 */
public interface GoodsService extends IService<Goods> {

    /**
     * 商家发布商品
     *
     * @param goods  商品信息
     * @param userId 商家用户ID
     */
    void saveGoods(Goods goods, Long userId);

    /**
     * 商家修改商品信息
     *
     * @param goods  商品信息
     * @param userId 商家用户ID
     */
    void updateGoods(Goods goods, Long userId);

    /**
     * 商家自主下架商品
     *
     * @param goodsId 商品ID
     * @param userId  商家用户ID
     */
    void offlineGoods(Long goodsId, Long userId);

    /**
     * 商家重新上架商品
     *
     * @param goodsId 商品ID
     * @param userId  商家用户ID
     */
    void onlineGoods(Long goodsId, Long userId);

    /**
     * 查询当前商家的所有商品
     *
     * @param userId 商家用户ID
     * @return 商品列表
     */
    List<Goods> listByUserId(Long userId);

    /**
     * 前台商品列表查询（支持关键词、分类、排序）
     *
     * @param keyword    搜索关键词
     * @param categoryId 分类ID
     * @param sort       排序方式
     * @return 商品列表
     */
    List<Goods> listGoods(String keyword, Long categoryId, String sort);

    /**
     * 增加商品浏览量
     *
     * @param goodsId 商品ID
     */
    void incrViewCount(Long goodsId);

    /**
     * 增加商品点赞数
     *
     * @param goodsId 商品ID
     */
    void incrLikeCount(Long goodsId);

    /**
     * 减少商品点赞数
     *
     * @param goodsId 商品ID
     */
    void decrLikeCount(Long goodsId);

    /**
     * 增加商品收藏数
     *
     * @param goodsId 商品ID
     */
    void incrCollectCount(Long goodsId);

    /**
     * 减少商品收藏数
     *
     * @param goodsId 商品ID
     */
    void decrCollectCount(Long goodsId);

    /**
     * 增加商品评论数
     *
     * @param goodsId 商品ID
     */
    void incrCommentCount(Long goodsId);

    /**
     * 管理员人工审核商品
     *
     * @param goodsId 商品ID
     * @param status  审核状态
     */
    void auditGoods(Long goodsId, String status);

    // ==================== AI 审核相关 ====================

    /**
     * 提交商品进行AI内容审核
     *
     * @param goodsId 商品ID
     * @param userId  操作用户ID
     */
    void submitToAiAudit(Long goodsId, Long userId);

    /**
     * 处理AI审核结果并更新商品状态
     *
     * @param goodsId 商品ID
     * @param pass    是否审核通过
     */
    void aiAuditGoods(Long goodsId, boolean pass);

    /**
     * 查询待人工审核的商品列表
     *
     * @return 待审核商品列表
     */
    List<Goods> listPendingAudit();
}