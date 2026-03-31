package com.app.mart.service.impl;

import com.app.mart.entity.AfterSale;
import com.app.mart.entity.User;
import com.app.mart.service.AfterSaleService;
import com.app.mart.service.UserCenterQueryService;
import com.app.mart.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户中心查询服务实现类
 * 专供 AI 智能客服查询用户信息、售后单等数据
 *
 * @author LunaMart
 */
@Service
public class UserCenterQueryServiceImpl implements UserCenterQueryService {

    @Resource
    private UserService userService;

    @Resource
    private AfterSaleService afterSaleService;

    /**
     * 获取用户全部售后单（不分页，AI 客服专用）
     *
     * @param userId 用户ID
     * @return 售后单列表
     */
    @Override
    public List<AfterSale> getAfterSales(Long userId) {
        return afterSaleService.userList(userId, 1, Integer.MAX_VALUE, null, null, null, null).getRecords();
    }

    /**
     * 根据用户ID获取用户名（空值安全处理）
     *
     * @param userId 用户ID
     * @return 用户名
     */
    @Override
    public String getUsername(Long userId) {
        User user = userService.getById(userId);
        return user == null ? "用户" : user.getUsername();
    }
}