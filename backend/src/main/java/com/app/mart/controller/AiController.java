package com.app.mart.controller;

import com.app.mart.common.result.R;
import com.app.mart.entity.Goods;
import com.app.mart.service.AiService;
import com.app.mart.service.GoodsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.var;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/**
 * AI 智能模块 Controller
 * 功能：AI 智能客服、图片审核、图片识别搜商品
 * 权限：登录用户可用
 *
 * @author LunaMart
 */
@RestController
@RequestMapping("/ai")
@Api(tags = "AI 智能模块")
public class AiController {

    @Resource
    private AiService aiService;

    @Resource
    private GoodsService goodsService;

    /**
     * AI 智能客服（用户聊天）
     * @param message 用户提问内容
     * @param userId 当前登录用户ID
     * @return AI 回答内容
     */
    @PostMapping("/chat")
    @ApiOperation("AI 智能客服")
    public R<String> chat(@RequestParam String message,
                          @ApiIgnore @RequestAttribute Long userId) {
        String reply = aiService.chat(userId, message);
        return R.ok(reply);
    }

    /**
     * AI 图片识图搜索商品（拍照搜同款/相似商品）
     * 上传图片 → AI识别内容关键词 → 自动搜索商品并返回
     * @param file 商品图片
     * @return 商品列表 + 识别关键词
     */
    @PostMapping("/image/searchGoods")
    @ApiOperation("AI图片识图搜索商品")
    public R<Map<String, Object>> searchGoodsByImage(@RequestParam MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail("图片不能为空");
        }
        try {
            // 1. 图片转base64
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());

            // 2. AI识别图片关键词
            Map<String, Object> detectResult = aiService.imageDetect(base64);
            String keyword = (String) detectResult.get("keyword");

            // 3. 识别失败处理
            if ("识别失败".equals(keyword)) {
                return R.fail("图片识别失败，请更换图片");
            }

            // 4. 根据关键词搜索已上架商品
            Goods goodsQuery = new Goods();
            goodsQuery.setTitle(keyword);
            var goodsList = goodsService.listGoods(keyword, null, null);

            // 5. 封装返回结果（包含关键词 + 商品列表）
            Map<String, Object> result = detectResult;
            result.put("goodsList", goodsList);

            return R.ok(result);
        } catch (IOException e) {
            return R.fail("图片搜索失败");
        }
    }
}