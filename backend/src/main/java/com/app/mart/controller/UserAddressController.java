package com.app.mart.controller;

import com.app.mart.common.result.R;
import com.app.mart.entity.UserAddress;
import com.app.mart.service.UserAddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * 用户收货地址模块控制层
 * 功能：收货地址新增、修改、删除、查询、设置默认地址
 * 权限：仅当前登录用户可操作自己的地址信息
 *
 * @author LunaMart
 */
@RestController
@RequestMapping("/address")
@Api(tags = "用户收货地址模块")
public class UserAddressController {

    private final UserAddressService userAddressService;

    /**
     * 构造注入：依赖注入用户地址业务服务
     */
    public UserAddressController(UserAddressService userAddressService) {
        this.userAddressService = userAddressService;
    }

    /**
     * 新增用户收货地址
     * 最多可保存多个地址，支持设置默认地址
     *
     * @param address 收货地址信息
     * @param userId  当前登录用户ID
     * @return 新增结果
     */
    @PostMapping("/add")
    @ApiOperation("新增收货地址")
    public R<Boolean> addAddress(
            @RequestBody UserAddress address,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        return userAddressService.addAddress(address, userId);
    }

    /**
     * 修改用户收货地址
     * 仅地址所属用户可修改，接口自动校验权限
     *
     * @param address 收货地址信息
     * @param userId  当前登录用户ID
     * @return 修改结果
     */
    @PutMapping("/update")
    @ApiOperation("修改收货地址")
    public R<Boolean> updateAddress(
            @RequestBody UserAddress address,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        return userAddressService.updateAddress(address, userId);
    }

    /**
     * 删除收货地址
     * 仅地址所属用户可删除
     *
     * @param id     地址ID
     * @param userId 当前登录用户ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除收货地址")
    public R<Boolean> deleteAddress(
            @PathVariable Long id,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        return userAddressService.deleteAddress(id, userId);
    }

    /**
     * 查询当前用户所有收货地址
     * 按默认地址优先、更新时间倒序展示
     *
     * @param userId 当前登录用户ID
     * @return 地址列表
     */
    @GetMapping("/list")
    @ApiOperation("查询用户所有地址")
    public R<List<UserAddress>> getByUserId(
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        return R.ok(userAddressService.getByUserId(userId));
    }

    /**
     * 设置默认收货地址
     * 同一用户只能有一个默认地址，下单时自动选中
     *
     * @param id     地址ID
     * @param userId 当前登录用户ID
     * @return 设置结果
     */
    @PutMapping("/default/{id}")
    @ApiOperation("设置默认地址")
    public R<Boolean> setDefault(
            @PathVariable Long id,
            @ApiIgnore @RequestAttribute("userId") Long userId) {
        return userAddressService.setDefault(id, userId);
    }
}