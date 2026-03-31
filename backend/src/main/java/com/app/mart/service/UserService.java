package com.app.mart.service;

import com.app.mart.common.result.R;
import com.app.mart.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Map;

/**
 * 用户服务接口
 * 提供注册、登录、邮箱验证码、账号冻结等功能
 * @author LunaMart
 */
public interface UserService extends IService<User> {

    /**
     * 发送邮箱验证码
     * @param email 接收邮箱
     */
    void sendEmailCode(String email);

    /**
     * 用户注册
     * @param email 邮箱
     * @param code 验证码
     * @param username 用户名
     * @param password 密码
     * @return 注册结果
     */
    R<Boolean> register(String email, String code, String username, String password);

    /**
     * 用户登录（支持用户名/邮箱登录）
     * @param account 账号
     * @param password 密码
     * @return token + 用户信息
     */
    R<Map<String, Object>> login(String account, String password);

    /**
     * 冻结用户账号
     * @param userId 用户ID
     * @return 操作结果
     */
    R<Boolean> freezeUser(Long userId);
}