package com.app.mart.service;

import com.app.mart.common.result.R;
import com.app.mart.entity.UserAddress;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 用户收货地址服务接口
 *
 * @author LunaMart
 */
public interface UserAddressService extends IService<UserAddress> {

    /**
     * 新增用户收货地址
     *
     * @param address 地址信息
     * @param userId  当前登录用户ID
     * @return 操作结果
     */
    R<Boolean> addAddress(UserAddress address, Long userId);

    /**
     * 修改用户收货地址
     *
     * @param address 待更新的地址信息
     * @param userId  当前登录用户ID
     * @return 操作结果
     */
    R<Boolean> updateAddress(UserAddress address, Long userId);

    /**
     * 删除用户收货地址
     *
     * @param id     地址ID
     * @param userId 当前登录用户ID
     * @return 操作结果
     */
    R<Boolean> deleteAddress(Long id, Long userId);

    /**
     * 根据用户ID查询收货地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<UserAddress> getByUserId(Long userId);

    /**
     * 设置默认收货地址
     *
     * @param id     地址ID
     * @param userId 用户ID
     * @return 操作结果
     */
    R<Boolean> setDefault(Long id, Long userId);
}