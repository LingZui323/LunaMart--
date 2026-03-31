package com.app.mart.controller;

import com.app.mart.common.result.R;
import com.app.mart.entity.User;
import com.app.mart.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

/**
 * 用户模块控制层
 * 功能：用户注册、登录、邮箱验证码、管理员用户管理、账号冻结
 * 权限：普通用户、超级管理员
 *
 * @author LunaMart
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户模块")
public class UserController {

    private final UserService userService;

    /**
     * 构造注入：依赖注入用户业务服务
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 发送 QQ 邮箱注册验证码
     * 验证码有效期 5 分钟，用于用户注册验证
     *
     * @param email QQ邮箱
     * @return 发送结果
     */
    @GetMapping("/sendCode")
    @ApiOperation("发送QQ邮箱注册验证码")
    public R<String> sendCode(
            @ApiParam(value = "QQ邮箱", required = true, example = "12345678@qq.com")
            @RequestParam String email) {
        try {
            userService.sendEmailCode(email);
            return R.ok("验证码发送成功");
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 用户注册
     * 支持 QQ邮箱 + 验证码 + 自定义用户名 + 密码
     * 注册成功后可直接登录
     *
     * @param email     QQ邮箱
     * @param code      邮箱验证码
     * @param username  用户名
     * @param password  密码
     * @return 注册结果
     */
    @PostMapping("/register")
    @ApiOperation("用户注册（QQ邮箱+验证码+自定义用户名+密码）")
    public R<Boolean> register(
            @ApiParam(value = "QQ邮箱", required = true) @RequestParam String email,
            @ApiParam(value = "验证码", required = true) @RequestParam String code,
            @ApiParam(value = "自定义用户名", required = true) @RequestParam String username,
            @ApiParam(value = "密码", required = true) @RequestParam String password) {
        return userService.register(email, code, username, password);
    }

    /**
     * 用户登录
     * 支持两种登录方式：用户名 / 邮箱 + 密码
     * 登录成功返回 token 和用户信息
     *
     * @param account  用户名 / 邮箱
     * @param password 密码
     * @return 登录信息（token + user）
     */
    @PostMapping("/login")
    @ApiOperation("用户登录（支持 用户名 或 邮箱 + 密码）")
    public R<Map<String, Object>> login(
            @ApiParam(value = "用户名 / 邮箱", required = true)
            @RequestParam String account,

            @ApiParam(value = "密码", required = true)
            @RequestParam String password) {
        return userService.login(account, password);
    }

    /**
     * 【超级管理员】查询平台所有用户
     * 用于后台用户管理、账号状态查看
     *
     * @return 用户列表
     */
    @GetMapping("/manage/list")
    @ApiOperation("【超级管理员】查询所有用户")
    public R<List<User>> listAllUsers() {
        return R.ok(userService.list());
    }

    /**
     * 【超级管理员】冻结违规用户账号
     * 冻结后用户无法登录、下单、购买商品
     * 不允许冻结自己账号
     *
     * @param userId        要冻结的用户ID
     * @param currentUserId 当前管理员ID
     * @return 冻结结果
     */
    @PutMapping("/manage/freeze/{userId}")
    @ApiOperation("【超级管理员】冻结用户")
    public R<Boolean> freezeUser(
            @PathVariable Long userId,
            @ApiIgnore @RequestAttribute("userId") Long currentUserId) {
        if (userId.equals(currentUserId)) {
            return R.fail("不能冻结自己的账号");
        }
        return userService.freezeUser(userId);
    }
}