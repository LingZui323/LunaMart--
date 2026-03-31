package com.app.mart.service.impl;

import com.app.mart.entity.GoodsComment;
import com.app.mart.entity.User;
import com.app.mart.mapper.GoodsCommentMapper;
import com.app.mart.service.GoodsCommentService;
import com.app.mart.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 商品评论服务实现类
 * 实现商品评论的查询、发布、删除、点赞、用户名填充等功能
 *
 * @author LunaMart
 */
@Service
public class GoodsCommentServiceImpl extends ServiceImpl<GoodsCommentMapper, GoodsComment> implements GoodsCommentService {

    /**
     * 注入用户服务：用于查询评论发布者的用户名
     */
    @Resource
    private UserService userService;

    /**
     * 根据商品ID查询评论列表
     * 按创建时间 最新 → 最早 排序
     *
     * @param goodsId 商品ID
     * @return 该商品的所有评论
     */
    @Override
    public List<GoodsComment> listByGoodsId(Long goodsId) {
        LambdaQueryWrapper<GoodsComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsComment::getGoodsId, goodsId)
                .orderByDesc(GoodsComment::getCreateTime);
        return list(wrapper);
    }

    /**
     * 查询商品评论，并自动填充【发布者用户名】
     * 用于前端展示评论人名称
     *
     * @param goodsId 商品ID
     * @return 带用户名的评论列表
     */
    @Override
    public List<GoodsComment> listByGoodsIdWithUsername(Long goodsId) {
        // 先查询基础评论列表
        List<GoodsComment> list = listByGoodsId(goodsId);

        // 遍历每条评论，设置对应的用户名
        for (GoodsComment comment : list) {
            User user = userService.getById(comment.getUserId());
            if (user != null) {
                comment.setUsername(user.getUsername());
            }
        }
        return list;
    }

    /**
     * 删除评论（权限控制）
     * 只有评论发布者本人可以删除
     *
     * @param id     评论ID
     * @param userId 当前登录用户ID
     * @return 删除成功/失败
     */
    @Override
    public boolean deleteComment(Long id, Long userId) {
        // 查询评论是否存在
        GoodsComment comment = getById(id);
        if (comment == null) {
            return false;
        }

        // 校验：只能删除自己发布的评论
        if (!comment.getUserId().equals(userId)) {
            return false;
        }

        // 执行删除
        return removeById(id);
    }

    /**
     * 评论点赞 / 取消点赞
     * 极简实现：每次调用点赞数 +1
     *
     * @param commentId 评论ID
     * @param userId    操作用户ID
     * @return  always true（点赞成功）
     */
    @Override
    public boolean toggleLike(Long commentId, Long userId) {
        // 校验评论是否存在
        GoodsComment comment = getById(commentId);
        if (comment == null) {
            return false;
        }

        // 构建更新条件
        LambdaUpdateWrapper<GoodsComment> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(GoodsComment::getId, commentId);

        // 点赞数 +1
        wrapper.setSql("like_count = like_count + 1");
        update(wrapper);

        return true;
    }
}