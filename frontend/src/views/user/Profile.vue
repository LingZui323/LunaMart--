<template>
  <div class="profile-page">
    <!-- 顶部导航栏：去掉 placeholder，避免遮挡 -->
    <van-nav-bar
      title="个人中心"
      fixed
      class="moon-nav-bar"
    />

    <!-- 用户信息卡片：加 margin-top 避开导航栏 -->
    <div class="user-card" style="margin-top: 46px;">
      <div class="avatar">
        <van-icon name="user-o" size="40" color="#fff" />
      </div>
      <div class="user-info">
        <div class="username">{{ userStore?.userInfo?.username || '未登录用户' }}</div>
        <div class="user-desc">
          {{ userStore?.userInfo?.email || '暂无邮箱' }} 
          <span style="font-size:12px;color:#ff4444;margin-left:8px;">
            {{ roleText }}
          </span>
        </div>
      </div>
      <van-button
        size="small"
        type="primary"
        color="#ff9f00"
        class="logout-btn-right"
        @click="handleAuthAction"
      >
        {{ userStore?.isLogin ? '退出登录' : '登录' }}
      </van-button>
    </div>

    <!-- 功能菜单（不变） -->
    <van-cell-group inset class="menu-group">
      <van-cell
        title="我的订单"
        value="查看全部订单"
        is-link
        to="/order/list"
        icon="orders-o"
      />
      <van-cell
        title="售后处理"
        value="查看售后记录"
        is-link
        to="/aftersale/list"
        icon="service-o"
      />
      <van-cell
        title="我的收藏"
        value="商品收藏"
        is-link
        to="/favorite"
        icon="star-o"
      />
      <van-cell
        title="地址管理"
        value="收货地址"
        is-link
        to="/address"
        icon="location-o"
      />
      <van-cell
        title="账号设置"
        value="修改信息"
        is-link
        to="/settings"
        icon="setting-o"
      />

      <!-- 商户专属入口 -->
      <van-cell
        v-if="userStore?.userInfo?.role === 'MERCHANT'"
        title="商户管理中心"
        value="商品/订单/数据"
        is-link
        @click="goMerchantCenter"  
        icon="shop-o"
        style="color:#ff9f00;font-weight:bold;"
      />


      <!-- 超级管理员菜单 -->
      <template v-if="userStore?.userInfo?.role === 'SUPER_ADMIN'">
        <van-cell
          title="用户管理"
          value="用户订单处理"
          is-link
          to="/admin/user"
          icon="friends-o"
          style="color:#ff4444;font-weight:bold;"
        />
        <van-cell
          title="商家管理"
          value="商家申请/数据"
          is-link
          to="/admin/merchant"
          icon="shop-o"
          style="color:#ff4444;font-weight:bold;"
        />
        <van-cell
          title="商品审核"
          value="上架/下架审核"
          is-link
          to="/admin/goods/audit"
          icon="checklist-o"
          style="color:#ff4444;font-weight:bold;"
        />
        <van-cell
          title="售后审核"
          value="平台售后处理"
          is-link
          to="/admin/aftersale"
          icon="warning-o"
          style="color:#ff4444;font-weight:bold;"
        />
        <van-cell
          title="订单统计"
          value="订单数据统计"
          is-link
          to="/admin/order/stat"
          icon="chart-trending-o"
          style="color:#ff4444;font-weight:bold;"
        />
        <van-cell
          title="AI审核日志"
          value="内容审核记录"
          is-link
          to="/admin/ai/log"
          icon="shield-o"
          style="color:#ff4444;font-weight:bold;"
        />
      </template>

      <!-- 普通管理员菜单 -->
      <template v-if="userStore?.userInfo?.role === 'ADMIN'">
        <van-cell
          title="商家管理"
          value="商家申请/数据"
          is-link
          to="/admin/merchant"
          icon="shop-o"
          style="color:#ff4444;font-weight:bold;"
        />
        <van-cell
          title="商品审核"
          value="上架/下架审核"
          is-link
          to="/admin/goods/audit"
          icon="checklist-o"
          style="color:#ff4444;font-weight:bold;"
        />
        <van-cell
          title="售后审核"
          value="平台售后处理"
          is-link
          to="/admin/aftersale"
          icon="warning-o"
          style="color:#ff4444;font-weight:bold;"
        />
      </template>
    </van-cell-group>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore() 

// 跳转到商户管理中心
const goMerchantCenter = () => {
  router.push(`/merchant?merchantId=${userStore.userInfo.id}`)
}

// 角色文本展示
const roleText = computed(() => {
  const role = userStore?.userInfo?.role
  if (role === 'USER') return '普通用户'
  if (role === 'MERCHANT') return '商户'
  if (role === 'ADMIN') return '管理员'
  if (role === 'SUPER_ADMIN') return '超级管理员'
  return ''
})

// 退出登录/登录
const handleAuthAction = () => {
  if (!userStore) return
  if (userStore.isLogin) {
    userStore.logout()
    showToast('已退出登录')
    setTimeout(() => router.push('/login'), 800)
  } else {
    router.push('/login')
  }
}

// ✅ 修复：删掉了不存在的 fetchUserInfo 调用
// onMounted 里不再做任何操作，避免报错
</script>

<style scoped>
.profile-page {
  background: #fff9e6;
  min-height: 100vh;
}

.moon-nav-bar {
  background: #FFE181;
}
.moon-nav-bar .van-nav-bar__title {
  color: #333;
  font-weight: 500;
}

.user-card {
  display: flex;
  align-items: center;
  padding: 25px 20px;
  background: #FFE181;
  border-radius: 0 0 20px 20px;
  margin-bottom: 15px;
}
.avatar {
  width: 60px;
  height: 60px;
  background: #ff9f00;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}
.user-info {
  flex: 1;
}
.username {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}
.user-desc {
  font-size: 13px;
  color: #666;
}

.logout-btn-right {
  border-radius: 20px;
  color: #fff;
  font-size: 12px;
  padding: 5px 12px;
}

.menu-group {
  margin: 0 15px 20px;
  background: #fff;
  border-radius: 12px;
}
.menu-group .van-cell {
  padding: 15px 20px;
}
</style>