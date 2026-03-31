package com.app.mart.service.impl;

import com.app.mart.common.result.R;
import com.app.mart.entity.Goods;
import com.app.mart.entity.Merchant;
import com.app.mart.entity.User;
import com.app.mart.mapper.MerchantMapper;
import com.app.mart.service.GoodsService;
import com.app.mart.service.MerchantService;
import com.app.mart.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 商家服务实现类
 * 实现商家申请、资料修改、审核、冻结、查询等功能
 *
 * @author LunaMart
 */
@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements MerchantService {

    @Resource
    private UserService userService;

    @Resource
    private GoodsService goodsService;

    /**
     * 用户申请成为商家
     */
    @Override
    public R<Boolean> applyMerchant(Merchant merchant, Long userId) {
        Merchant exist = getMyMerchant(userId);
        if (exist != null) {
            return R.fail("您已申请过商家，请勿重复申请");
        }
        merchant.setUserId(userId);
        merchant.setStatus("PENDING");
        save(merchant);
        return R.ok(true);
    }

    /**
     * 商家修改自身资料（需重新审核）
     */
    @Override
    public R<Boolean> updateMerchant(Merchant merchant, Long userId) {
        Merchant exist = getMyMerchant(userId);
        if (exist == null) {
            return R.fail("您还不是商家，请先提交申请");
        }

        if ("FROZEN".equals(exist.getStatus())) {
            return R.fail("商家已被冻结，无法修改资料");
        }

        if ("REJECTED".equals(exist.getStatus())) {
            return R.fail("申请已被拒绝，无法修改资料");
        }

        merchant.setId(exist.getId());
        merchant.setUserId(userId);
        merchant.setStatus("PENDING");
        updateById(merchant);
        return R.ok(true);
    }

    /**
     * 根据用户ID获取商家信息
     */
    @Override
    public Merchant getMyMerchant(Long userId) {
        return getOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUserId, userId));
    }

    /**
     * 管理员查询商家列表（支持按状态筛选）
     */
    @Override
    public List<Merchant> listMerchants(String status) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(Merchant::getStatus, status);
        }
        wrapper.orderByDesc(Merchant::getCreateTime);
        return list(wrapper);
    }

    /**
     * 管理员审核商家申请
     */
    @Override
    public R<Boolean> auditMerchant(Long merchantId, String status) {
        Merchant merchant = getById(merchantId);
        if (merchant == null) {
            return R.fail("商家不存在");
        }

        if (!"PENDING".equals(merchant.getStatus())) {
            return R.fail("只有待审核的商家才能进行审核操作");
        }

        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            return R.fail("审核状态不合法，仅允许：APPROVED / REJECTED");
        }

        // 更新商家审核状态
        update(new LambdaUpdateWrapper<Merchant>()
                .eq(Merchant::getId, merchantId)
                .set(Merchant::getStatus, status));

        // 审核通过 → 将用户角色升级为 MERCHANT
        if ("APPROVED".equals(status)) {
            userService.update(new LambdaUpdateWrapper<User>()
                    .eq(User::getId, merchant.getUserId())
                    .set(User::getRole, "MERCHANT"));
        }

        return R.ok(true);
    }

    /**
     * 冻结商家，并自动下架其所有商品
     */
    @Override
    public R<Boolean> freezeMerchant(Long merchantId) {
        Merchant merchant = getById(merchantId);
        if (merchant == null) {
            return R.fail("商家不存在");
        }

        if (!"APPROVED".equals(merchant.getStatus())) {
            return R.fail("仅允许冻结【已通过审核】的商家");
        }

        // 冻结商家
        update(new LambdaUpdateWrapper<Merchant>()
                .eq(Merchant::getId, merchantId)
                .set(Merchant::getStatus, "FROZEN"));

        // 下架该商家所有商品
        goodsService.update(new LambdaUpdateWrapper<Goods>()
                .eq(Goods::getMerchantId, merchant.getUserId())
                .set(Goods::getStatus, "OUT_OF_STOCK"));

        return R.ok(true);
    }
}