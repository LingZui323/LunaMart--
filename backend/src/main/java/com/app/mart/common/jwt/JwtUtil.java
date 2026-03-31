package com.app.mart.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import java.util.Date;

/**
 * JWT 工具类
 * 功能：实现 Token 生成、解析、用户ID与角色提取、令牌校验
 * 用于用户登录认证、接口权限拦截、用户身份识别
 *
 * @author LunaMart
 */
@Component
public class JwtUtil {

    /**
     * 生成 JWT 令牌
     * 将用户ID、角色信息存入 Token，设置过期时间与签名密钥
     *
     * @param userId 用户ID
     * @param role   角色标识（user/admin/merchant）
     * @return 加密后的 JWT 字符串
     */
    public String generateToken(Long userId, String role) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setExpiration(new Date(System.currentTimeMillis() + JwtConstant.EXPIRATION))
                .signWith(SignatureAlgorithm.HS512, JwtConstant.SECRET)
                .compact();
    }

    /**
     * 解析 Token 获取载荷信息（Claims）
     * 解析失败（过期/篡改/无效）时返回 null
     *
     * @param token JWT字符串
     * @return Claims 载荷数据
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(JwtConstant.SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Token 中提取用户ID
     *
     * @param token JWT字符串
     * @return userId 用户ID
     */
    public Long getUserId(String token) {
        Claims claims = getClaims(token);
        return claims == null ? null : Long.valueOf(claims.getSubject());
    }

    /**
     * 从 Token 中提取用户角色
     *
     * @param token JWT字符串
     * @return role 角色（user/admin/merchant）
     */
    public String getRole(String token) {
        Claims claims = getClaims(token);
        return claims == null ? null : claims.get("role", String.class);
    }
}