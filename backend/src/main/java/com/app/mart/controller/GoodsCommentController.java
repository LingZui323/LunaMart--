package com.app.mart.controller;

import com.app.mart.common.result.R;
import com.app.mart.entity.GoodsComment;
import com.app.mart.service.GoodsCommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

/**
 * 商品评论 Controller
 * 负责接收前端商品评论相关请求，调用Service层实现业务逻辑
 * 功能涵盖：发布评论、查询评论（带发布人名称）、删除本人评论、评论点赞/取消点赞
 * 权限控制：所有接口需登录，删除评论仅允许本人操作
 *
 * @author LunaMart
 */
@RestController
@RequestMapping("/goods/comment")
@Api(tags = "商品评论模块") // Swagger接口分组标签
public class GoodsCommentController {

    /**
     * 注入商品评论服务，调用其业务方法实现功能
     */
    private final GoodsCommentService goodsCommentService;

    /**
     * 构造函数注入（Spring推荐注入方式，避免循环依赖，提升代码规范性）
     * @param goodsCommentService 商品评论服务实现类
     */
    public GoodsCommentController(GoodsCommentService goodsCommentService) {
        this.goodsCommentService = goodsCommentService;
    }

    /**
     * 发布商品评论
     * 接收前端传递的评论信息，绑定当前登录用户ID，调用Service保存评论
     *
     * @param comment 评论实体（包含商品ID、评论内容、评分、父评论ID等），@Valid 校验请求参数合法性
     * @param userId 当前登录用户ID（由拦截器解析Token获取，前端无需传递，@ApiIgnore 隐藏Swagger显示）
     * @return 发布结果（成功/失败）
     */
    @PostMapping
    @ApiOperation("发布商品评论") // Swagger接口说明
    public R<Boolean> add(
            @RequestBody @Valid GoodsComment comment,
            @ApiIgnore @RequestAttribute Long userId) {

        // 绑定当前登录用户为评论发布者
        comment.setUserId(userId);
        // 调用Service保存评论，返回保存结果
        return R.ok(goodsCommentService.save(comment));
    }

    /**
     * 查询商品的所有评论（带发布人名称）
     * 接收商品ID，调用Service查询评论并自动填充发布者用户名，返回给前端展示
     *
     * @param goodsId 商品ID（路径参数，对应要查询评论的商品）
     * @return 带发布人名称的评论列表（按创建时间倒序）
     */
    @GetMapping("/list/{goodsId}")
    @ApiOperation("查询商品评论（带发布人名称）")
    public R<List<GoodsComment>> list(@PathVariable Long goodsId) {
        // 调用Service查询带用户名的评论列表，返回给前端
        return R.ok(goodsCommentService.listByGoodsIdWithUsername(goodsId));
    }

    /**
     * 删除商品评论（仅本人可删）
     * 接收评论ID和当前登录用户ID，校验权限后，调用Service删除评论
     *
     * @param id 评论ID（路径参数，对应要删除的评论）
     * @param userId 当前登录用户ID（拦截器解析获取，前端无需传递）
     * @return 删除结果提示（成功/失败，失败说明无权限或评论不存在）
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除评论（仅本人可操作）")
    public R<String> delete(
            @PathVariable Long id,
            @ApiIgnore @RequestAttribute Long userId) {

        // 调用Service删除评论，校验权限（仅本人可删）
        boolean ok = goodsCommentService.deleteComment(id, userId);
        // 根据删除结果返回对应提示信息
        return ok ? R.ok("删除成功") : R.fail("无权限或评论不存在");
    }

    /**
     * 评论点赞/取消点赞
     * 接收评论ID和当前登录用户ID，调用Service实现点赞逻辑，返回操作结果
     *
     * @param commentId 评论ID（路径参数，对应要点赞/取消点赞的评论）
     * @param userId 当前登录用户ID（拦截器解析获取，前端无需传递）
     * @return 操作结果提示（点赞成功/取消点赞成功）
     */
    @PostMapping("/like/{commentId}")
    @ApiOperation("评论点赞或取消点赞")
    public R<String> like(
            @PathVariable Long commentId,
            @ApiIgnore @RequestAttribute Long userId) {

        // 调用Service实现点赞/取消点赞逻辑，返回操作状态
        boolean isLiked = goodsCommentService.toggleLike(commentId, userId);
        // 根据操作状态返回对应提示
        return isLiked ? R.ok("点赞成功") : R.ok("已取消点赞");
    }
}