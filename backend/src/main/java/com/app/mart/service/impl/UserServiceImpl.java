package com.app.mart.service.impl;

import com.app.mart.common.jwt.JwtUtil;
import com.app.mart.common.result.R;
import com.app.mart.entity.User;
import com.app.mart.mapper.UserMapper;
import com.app.mart.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 * 处理用户注册、登录、验证码、账号管理等业务
 *
 * @author LunaMart
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    /**
     * 构造器注入依赖
     */
    public UserServiceImpl(JavaMailSender mailSender, JwtUtil jwtUtil, UserMapper userMapper) {
        this.mailSender = mailSender;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }

    /**
     * 邮箱验证码本地缓存
     */
    private final Map<String, String> emailCodeMap = new HashMap<>();

    /**
     * 密码加密工具
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 发送QQ邮箱注册验证码
     */
    @Override
    public void sendEmailCode(String email) {
        if (!email.matches("[1-9]\\d{5,10}@qq\\.com")) {
            throw new RuntimeException("请输入正确的QQ邮箱格式");
        }

        // 生成6位数字验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        emailCodeMap.put(email, code);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("2870170241@qq.com", "月光商城");
            helper.setTo(email);
            helper.setSubject("注册验证码");
            helper.setText("您的验证码：" + code + "，5分钟内有效");
            mailSender.send(message);
            log.info("邮件发送成功：{}，验证码：{}", email, code);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            throw new RuntimeException("邮件发送失败：" + e.getMessage());
        }
    }

    /**
     * 用户注册逻辑（校验验证码、账号重复、密码加密）
     */
    @Override
    public R<Boolean> register(String email, String code, String username, String password) {
        String cacheCode = emailCodeMap.get(email);

        // 验证码校验
        if (!StringUtils.hasText(cacheCode) || !cacheCode.equals(code)) {
            return R.fail("验证码错误或已过期");
        }

        // 用户名格式校验
        if (!StringUtils.hasText(username) || username.length() < 2 || username.length() > 20) {
            return R.fail("用户名长度必须在 2-20 位之间");
        }

        // 密码格式校验
        if (!StringUtils.hasText(password) || password.length() < 6) {
            return R.fail("密码长度不能少于 6 位");
        }

        // 邮箱重复校验
        User existEmail = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (existEmail != null) {
            return R.fail("该邮箱已注册");
        }

        // 用户名重复校验
        User existUsername = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (existUsername != null) {
            return R.fail("用户名已被占用");
        }

        // 构建用户并加密密码
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);

        // 注册成功清除验证码
        emailCodeMap.remove(email);

        return R.ok(true);
    }

    /**
     * 用户登录（支持用户名/邮箱）
     */
    @Override
    public R<Map<String, Object>> login(String account, String password) {
        User user;

        // 判断是邮箱还是用户名登录
        if (account.contains("@")) {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, account));
        } else {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, account));
        }

        if (user == null) {
            return R.fail("账号或密码错误");
        }

        // 密码校验
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return R.fail("账号或密码错误");
        }

        // 账号禁用校验
        if (user.getStatus() == 0) {
            return R.fail("账号已禁用，请联系管理员");
        }

        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("user", user);

        return R.ok(map);
    }

    /**
     * 管理员冻结用户（保护超级管理员）
     */
    @Override
    public R<Boolean> freezeUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            return R.fail("用户不存在");
        }

        // 禁止冻结超级管理员
        if ("SUPER_ADMIN".equals(user.getRole())) {
            return R.fail("无法冻结超级管理员账号");
        }

        if (user.getStatus() == 0) {
            return R.fail("该用户已处于冻结状态");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setStatus(0);
        updateById(updateUser);

        return R.ok(true);
    }
}