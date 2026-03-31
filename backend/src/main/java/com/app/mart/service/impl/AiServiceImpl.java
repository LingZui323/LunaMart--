package com.app.mart.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.app.mart.entity.AfterSale;
import com.app.mart.service.AiService;
import com.app.mart.service.UserCenterQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    @Value("${ai.tongyi.api-key}")
    private String tongyiApiKey;

    @Value("${ai.baidu.api-key}")
    private String baiduApiKey;

    @Value("${ai.baidu.secret-key}")
    private String baiduSecretKey;

    @Resource
    private UserCenterQueryService userCenterQueryService;

    private final RestTemplate restTemplate = new RestTemplate();

    // 百度Token缓存（避免频繁申请）
    private String baiduToken;
    private long tokenExpireTime;

    /**
     * AI智能客服（优化：稳定参数+上下文优化）
     */
    @Override
    public String chat(Long userId, String message) {
        try {
            String username = userCenterQueryService.getUsername(userId);
            List<AfterSale> afterSales = userCenterQueryService.getAfterSales(userId);

            // 构建更简洁的Prompt（避免过长）
            String prompt = "你是电商智能客服，友好、简洁、专业。\n"
                    + "用户信息：" + username + "\n"
                    + buildAfterSaleInfo(afterSales)
                    + "用户问题：" + message;

            Map<String, Object> msg = new HashMap<>();
            msg.put("role", "user");
            msg.put("content", prompt);

            Map<String, Object> input = new HashMap<>();
            input.put("messages", new Map[]{msg});

            // 关键：加生成参数，控制回复质量
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("temperature", 0.3); // 低温度=稳定、准确
            parameters.put("max_tokens", 512);  // 限制长度

            Map<String, Object> body = new HashMap<>();
            body.put("model", "qwen-turbo");
            body.put("input", input);
            body.put("parameters", parameters);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + tongyiApiKey);
            headers.add("Content-Type", "application/json");

            Map<String, Object> resp = restTemplate.postForObject(
                    "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation",
                    new org.springframework.http.HttpEntity<>(body, headers),
                    Map.class
            );

            if (resp == null) {
                return "AI服务繁忙，请稍后再试";
            }

            Map<String, Object> output = (Map<String, Object>) resp.get("output");
            return output != null ? output.getOrDefault("text", "回复为空").toString() : "AI返回异常";

        } catch (Exception e) {
            log.error("AI对话异常 userId:{}", userId, e);
            return "AI客服暂时离线，请稍后再试~";
        }
    }

    /**
     * 构建售后信息（保持简洁）
     */
    private String buildAfterSaleInfo(List<AfterSale> list) {
        if (list == null || list.isEmpty()) {
            return "售后：无\n";
        }
        StringBuilder sb = new StringBuilder("售后：");
        for (AfterSale as : list) {
            sb.append("订单").append(as.getOrderId())
                    .append("[").append(convertAfter(as.getStatus())).append("]、");
        }
        return sb.substring(0, sb.length() - 1) + "\n";
    }

    /**
     * 售后状态转换
     */
    private String convertAfter(String s) {
        if (s == null) return "未知";
        switch (s) {
            case AfterSale.PENDING: return "待审核";
            case AfterSale.APPROVED: return "已同意";
            case AfterSale.REJECTED: return "已拒绝";
            case AfterSale.PLATFORM: return "平台介入";
            case AfterSale.COMPLETED: return "已完成";
            default: return s;
        }
    }

    /**
     * 图片内容识别（不变）
     */
    @Override
    public Map<String, Object> imageDetect(String base64) {
        Map<String, Object> result = new HashMap<>();
        try {
            String token = getBaiduToken();
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("image", base64);
            String resp = doBaiduPost(
                    "https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general?access_token=" + token,
                    params
            );
            JSONObject json = JSONObject.parseObject(resp);
            String keyword = json.getJSONArray("result").getJSONObject(0).getString("keyword");
            result.put("keyword", keyword);
        } catch (Exception e) {
            log.error("图片识别失败", e);
            result.put("keyword", "识别失败");
        }
        return result;
    }

    // ====================== 【核心优化】图片审核：宽松模式，不误判 ======================

    /**
     * 图片Base64审核（宽松版：仅明确不合规才拒绝）
     */
    @Override
    public boolean imageCensor(String base64) {
        try {
            String token = getBaiduToken();
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("image", base64);
            return censorInternal(params);
        } catch (Exception e) {
            log.error("图片审核异常(Base64)", e);
            return true; // 异常放行
        }
    }

    /**
     * 图片URL审核（宽松版）
     */
    @Override
    public boolean imageCensorByUrl(String imageUrl) {
        try {
            String token = getBaiduToken();
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("imgUrl", imageUrl);
            return censorInternal(params);
        } catch (Exception e) {
            log.error("图片审核异常(URL):{}", imageUrl, e);
            return true; // 异常放行
        }
    }

    /**
     * 抽取审核逻辑（去重）
     */
    private boolean censorInternal(MultiValueMap<String, String> params) {
        String resp = doBaiduPost(
                "https://aip.baidubce.com/rest/2.0/solution/v1/img_censor/v2/user_defined?access_token=" + getBaiduToken(),
                params
        );
        JSONObject json = JSONObject.parseObject(resp);
        String conclusion = json.getString("conclusion");

        // 【关键】只有明确"不合规"才返回false，其他都通过
        boolean isReject = "不合规".equals(conclusion);
        log.info("图片审核结果:{} 结论:{}", isReject ? "违规" : "通过", conclusion);
        return !isReject;
    }

    // ====================== 百度Token缓存（优化性能） ======================

    /**
     * 获取百度Token（带缓存+过期）
     */
    private String getBaiduToken() {
        // 缓存有效，直接返回
        if (baiduToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return baiduToken;
        }

        try {
            String url = "https://aip.baidubce.com/oauth/2.0/token"
                    + "?client_id=" + baiduApiKey
                    + "&client_secret=" + baiduSecretKey
                    + "&grant_type=client_credentials";

            Map<String, Object> resp = restTemplate.postForObject(url, null, Map.class);
            if (resp == null) return null;

            baiduToken = resp.get("access_token").toString();
            // 过期时间：默认30天，提前1小时过期
            tokenExpireTime = System.currentTimeMillis() + 29 * 24 * 3600 * 1000;
            return baiduToken;

        } catch (RestClientException e) {
            log.error("获取百度Token失败", e);
            return null;
        }
    }

    /**
     * 百度公共POST（加日志）
     */
    private String doBaiduPost(String url, MultiValueMap<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        try {
            return restTemplate.postForObject(url,
                    new org.springframework.http.HttpEntity<>(params, headers),
                    String.class);
        } catch (Exception e) {
            log.error("百度接口调用失败 url:{}", url, e);
            throw new RuntimeException("百度服务异常");
        }
    }
}