package com.app.mart.common.jwt;

/**
 * JWT 常量配置类
 * 功能：定义 JWT 令牌生成所需的密钥、过期时间等固定常量
 * 用于用户登录认证、令牌解析、接口权限校验
 *
 * @author LunaMart
 */
public class JwtConstant {

    /**
     * JWT 签名密钥（必须足够复杂，不可泄露）
     */
    public static final String SECRET = "lunaMartSecretKey12345678901234567890";

    /**
     * JWT 令牌过期时间
     * 默认为 1 天：1000ms * 60s * 60m * 24h
     */
    public static final long EXPIRATION = 1000 * 60 * 60 * 24;
}