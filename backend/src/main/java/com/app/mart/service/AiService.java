package com.app.mart.service;

import java.util.Map;

/**
 * AI 服务接口
 * 提供智能客服、图片检索、图片内容审核等 AI 能力
 *
 * @author LunaMart
 */
public interface AiService {

    /**
     * AI 智能客服对话
     *
     * @param userId  用户ID
     * @param message 用户提问内容
     * @return AI 回复内容
     */
    String chat(Long userId, String message);

    /**
     * 图片识别检索（用于拍照搜商品）
     *
     * @param base64 图片Base64编码
     * @return 识别结果与推荐商品
     */
    Map<String, Object> imageDetect(String base64);

    /**
     * 图片内容安全审核（Base64方式）
     *
     * @param base64 图片Base64编码
     * @return true=合规，false=违规
     */
    boolean imageCensor(String base64);

    /**
     * 图片内容安全审核（URL方式）
     *
     * @param imageUrl 图片网络地址
     * @return true=合规，false=违规
     */
    boolean imageCensorByUrl(String imageUrl);
}