package com.app.mart.service.impl;

import com.app.mart.common.result.R;
import com.app.mart.entity.UserAddress;
import com.app.mart.mapper.UserAddressMapper;
import com.app.mart.service.UserAddressService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户收货地址服务实现类
 * 实现地址新增、修改、删除、查询、设置默认地址等功能
 *
 * @author LunaMart
 */
@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    /**
     * 新增用户收货地址
     */
    @Override
    public R<Boolean> addAddress(UserAddress address, Long userId) {
        if (!StringUtils.hasText(address.getReceiver()) ||
                !StringUtils.hasText(address.getPhone()) ||
                !StringUtils.hasText(address.getDetailAddress())) {
            return R.fail("收货人、手机号、详细地址不能为空");
        }

        address.setUserId(userId);
        if (address.getIsDefault() == null) {
            address.setIsDefault(0);
        }

        save(address);
        return R.ok(true);
    }

    /**
     * 修改用户收货地址（权限校验）
     */
    @Override
    public R<Boolean> updateAddress(UserAddress address, Long userId) {
        UserAddress exist = getById(address.getId());
        if (exist == null) {
            return R.fail("地址不存在");
        }

        if (!exist.getUserId().equals(userId)) {
            return R.fail("无权限修改该地址");
        }

        if (!StringUtils.hasText(address.getReceiver()) ||
                !StringUtils.hasText(address.getPhone()) ||
                !StringUtils.hasText(address.getDetailAddress())) {
            return R.fail("收货人、手机号、详细地址不能为空");
        }

        address.setUserId(userId);
        updateById(address);
        return R.ok(true);
    }

    /**
     * 删除收货地址（本人可删）
     */
    @Override
    public R<Boolean> deleteAddress(Long id, Long userId) {
        boolean remove = remove(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getId, id)
                .eq(UserAddress::getUserId, userId));

        if (!remove) {
            return R.fail("地址不存在或无权限删除");
        }
        return R.ok(true);
    }

    /**
     * 查询当前用户的收货地址列表，默认地址优先
     */
    @Override
    public List<UserAddress> getByUserId(Long userId) {
        return list(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getUpdateTime));
    }

    /**
     * 设置默认收货地址（事务保证一致性）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> setDefault(Long id, Long userId) {
        UserAddress address = getById(id);
        if (address == null) {
            return R.fail("地址不存在");
        }

        if (!address.getUserId().equals(userId)) {
            return R.fail("无权限设置该地址");
        }

        // 先将该用户所有地址设为非默认
        update(new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .set(UserAddress::getIsDefault, 0));

        // 将当前地址设为默认
        update(new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getId, id)
                .set(UserAddress::getIsDefault, 1));

        return R.ok(true);
    }
}