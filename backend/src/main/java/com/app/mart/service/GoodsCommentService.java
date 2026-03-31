package com.app.mart.service;

import com.app.mart.entity.GoodsComment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品评论服务接口
 * 定义商品评论的核心业务方法：
 * 包括评论查询、带用户名查询、权限删除、点赞/取消点赞等功能
 *
 * @author LunaMart
 */
public interface GoodsCommentService extends IService<GoodsComment> {

    /**
     * 根据商品ID查询该商品的所有评论
     * 按创建时间 最新 → 最早 倒序排列
     *
     * @param goodsId 商品ID
     * @return 评论列表
     */
    List<GoodsComment> listByGoodsId(Long goodsId);

    /**
     * 查询商品评论，并自动填充发布者的用户名
     * 用于前端展示评论人昵称
     *
     * @param goodsId 商品ID
     * @return 带用户名的评论列表
     */
    List<GoodsComment> listByGoodsIdWithUsername(Long goodsId);

    /**
     * 删除商品评论（权限校验）
     * 只有评论的发布者本人才能删除
     *
     * @param id     评论ID
     * @param userId 当前登录用户ID
     * @return 删除成功返回 true，失败返回 false
     */
    boolean deleteComment(Long id, Long userId);

    /**
     * 评论点赞 / 取消点赞
     * 实现评论点赞数 +1 或 -1
     *
     * @param commentId 评论ID
     * @param userId    操作用户ID
     * @return 操作成功状态
     */
    boolean toggleLike(Long commentId, Long userId);
}