<template>
  <div class="merchant-home-page">
    <van-nav-bar
      :title="merchantInfo?.shopName || '店铺详情'"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <div class="container" style="margin-top: 46px; padding-bottom: 20px">
      <div class="merchant-info-bar">
        <div class="shop-name">{{ merchantInfo?.shopName || '店铺加载中...' }}</div>
        <div class="right-box">
          <van-button
            v-if="!isMyMerchant"
            size="small"
            type="primary"
            :color="isCollected ? '#ff9f00' : '#fff'"
            :style="{ borderColor: isCollected ? '#ff9f00' : '#eee' }"
            @click="toggleCollectMerchant"
          >
            {{ isCollected ? '已收藏' : '收藏店铺' }}
          </van-button>

          <div class="shop-status" 
               :class="{ 
                 approved: merchantInfo?.status === 'APPROVED',
                 pending: merchantInfo?.status === 'PENDING',
                 rejected: merchantInfo?.status === 'REJECTED',
                 frozen: merchantInfo?.status === 'FROZEN' 
               }" 
               style="margin-left:10px">
            {{ getStatusText(merchantInfo?.status) }}
          </div>
        </div>
      </div>

      <div class="grid-container" v-if="isMyMerchant">
        <van-grid column-num="2" :border="false" gutter="10">
          <van-grid-item class="grid-item" @click="handleGoTo('/merchant/goods/list')">
            <div class="grid-content">
              <van-icon name="orders-o" size="20" color="#ff9f00" />
              <div class="grid-title">商品管理</div>
              <div class="grid-desc">上架/下架商品</div>
            </div>
          </van-grid-item>

          <van-grid-item class="grid-item" @click="handleGoTo('/merchant/board')">
            <div class="grid-content">
              <van-icon name="chart-trending-o" size="20" color="#1989fa" />
              <div class="grid-title">数据中心</div>
              <div class="grid-desc">查看经营数据</div>
            </div>
          </van-grid-item>

          <van-grid-item class="grid-item" @click="handleGoTo('/merchant/order/list')">
            <div class="grid-content">
              <van-icon name="orders-o" size="20" color="#ff4444" />
              <div class="grid-title">订单管理</div>
              <div class="grid-desc">处理订单</div>
            </div>
          </van-grid-item>

          <van-grid-item class="grid-item" @click="handleGoTo('/merchant/aftersale')">
            <div class="grid-content">
              <van-icon name="service-o" size="20" color="#07c160" />
              <div class="grid-title">售后处理</div>
              <div class="grid-desc">处理售后申请</div>
            </div>
          </van-grid-item>
        </van-grid>
      </div>

      <div class="goods-title">
        <span>全部商品</span>
        <span class="tip" @click="handleGoTo('/merchant/goods')" v-if="isMyMerchant">
          管理商品 →
        </span>
      </div>

      <div class="goods-list">
        <van-cell-group inset>
          <van-cell
            v-for="item in goodsList"
            :key="item.id"
            class="goods-card"
            @click="handleViewGoods(item)"
          >
            <template #default>
              <div class="goods-info">
                <img
                  :src="item.imageList?.[0]?.imageUrl || 'https://picsum.photos/80/80'"
                  class="goods-img"
                />
                <div class="goods-text">
                  <div class="goods-name">{{ item.title }}</div>
                  <div class="goods-price">¥{{ item.price }}</div>
                  <div class="goods-status" :class="{ on: item.status === 'ON_SALE' }">
                    {{ item.status === 'ON_SALE' ? '在售' : '已下架' }}
                  </div>
                </div>
              </div>
            </template>
          </van-cell>
        </van-cell-group>

        <div v-if="goodsList.length === 0" style="text-align:center;padding:30px;color:#999">
          该商家暂无商品
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '../../store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 商家ID
const merchantId = ref(route.query.merchantId)
if (!merchantId.value) {
  showToast('商家ID不存在')
  router.back()
}

// 所有变量提前声明
const myMerchant = ref({})
const isMyMerchant = ref(false)
const merchantInfo = ref({})
const goodsList = ref([])
const isCollected = ref(false)

// 状态文本
const getStatusText = (status) => {
  const map = {
    PENDING: '待审核',
    APPROVED: '正常营业',
    REJECTED: '已拒绝',
    FROZEN: '已冻结'
  }
  return map[status] || '未知状态'
}

// ==============================================
// 🔥 加载【任意商家的基础信息】（你后端已有接口）
// ==============================================
const loadMerchantInfo = async () => {
  try {
    const res = await fetch(`/api/merchant/${merchantId.value}`, {
      headers: { Authorization: 'Bearer ' + userStore.token }
    })
    const data = await res.json()
    if (data.code === 200) {
      merchantInfo.value = data.data
    }
  } catch (e) {}
}

// ==============================================
// 加载我的商家信息（判断是否本人）
// ==============================================
const loadMyMerchant = async () => {
  try {
    const res = await fetch('/api/merchant/my', {
      headers: { Authorization: 'Bearer ' + userStore.token }
    })
    const data = await res.json()
    if (data.code === 200) {
      myMerchant.value = data.data || {}
    }
  } catch (e) {}
}

// ==============================================
// 加载商家商品（你已有接口）
// ==============================================
const loadMerchantGoods = async () => {
  try {
    const res = await fetch(`/api/goods/merchant/${merchantId.value}`, {
      headers: { Authorization: 'Bearer ' + userStore.token }
    })
    const data = await res.json()
    if (data.code === 200) {
      goodsList.value = data.data || []
    }
  } catch (e) {}
}

// ==============================================
// 收藏检查
// ==============================================
const checkIsCollected = async () => {
  try {
    const res = await fetch(`/api/favorite/check?targetType=MERCHANT&targetId=${merchantId.value}`, {
      headers: { Authorization: 'Bearer ' + userStore.token }
    })
    const data = await res.json()
    isCollected.value = data.data
  } catch (e) {}
}

// ==============================================
// 收藏/取消收藏
// ==============================================
const toggleCollectMerchant = async () => {
  try {
    if (isCollected.value) {
      await fetch(`/api/favorite/remove?targetType=MERCHANT&targetId=${merchantId.value}`, {
        method: 'DELETE',
        headers: { Authorization: 'Bearer ' + userStore.token }
      })
      isCollected.value = false
      showToast('已取消收藏')
    } else {
      await fetch(`/api/favorite/add?targetType=MERCHANT&targetId=${merchantId.value}`, {
        method: 'POST',
        headers: { Authorization: 'Bearer ' + userStore.token }
      })
      isCollected.value = true
      showToast('收藏成功')
    }
  } catch (e) {
    showToast('操作失败')
  }
}

// ==============================================
// 初始化
// ==============================================
onMounted(async () => {
  await loadMerchantInfo()
  await loadMyMerchant()
  isMyMerchant.value = String(merchantId.value) === String(myMerchant.value?.id)
  await loadMerchantGoods()
  if (!isMyMerchant.value) {
    await checkIsCollected()
  }
})

const handleGoTo = (path) => router.push(path)
const handleViewGoods = (item) => router.push(`/goods/detail/${item.id}`)
</script>

<style scoped>
.merchant-home-page { background: #fff9e6; min-height: 100vh; }
.moon-nav-bar { background: #FFE181; }
.moon-nav-bar .van-nav-bar__title { color: #333; font-weight: 500; }
.container { padding: 10px; }
.merchant-info-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 15px; background: #fff; border-radius: 12px; margin-bottom: 15px;
}
.shop-name { font-size: 18px; font-weight: bold; color: #333; }
.shop-status { font-size: 14px; }
.shop-status.approved { color: #07c160; }
.shop-status.pending { color: #ff9f00; }
.shop-status.rejected, .shop-status.frozen { color: #ff4444; }
.right-box { display: flex; align-items: center; }
.grid-container { margin-bottom: 20px; }
.grid-item { background: #fff; border-radius: 10px; padding: 12px 0; }
.grid-content { display: flex; flex-direction: column; align-items: center; }
.grid-title { font-size: 14px; font-weight: bold; color: #333; margin: 6px 0 3px; }
.grid-desc { font-size: 11px; color: #999; }
.goods-title {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 10px; font-size: 16px; font-weight: bold; color: #333;
}
.tip { font-size: 14px; color: #ff9f00; font-weight: normal; }
.goods-card { padding: 12px; background: #fff; border-radius: 12px; margin-bottom: 10px; }
.goods-info { display: flex; align-items: center; }
.goods-img { width: 70px; height: 70px; border-radius: 8px; margin-right: 10px; object-fit: cover; }
.goods-text { flex: 1; }
.goods-name { font-size: 15px; color: #333; margin-bottom: 4px; }
.goods-price { font-size: 16px; color: #ff4444; font-weight: bold; margin-bottom: 4px; }
.goods-status { font-size: 12px; padding: 2px 6px; border-radius: 4px; }
.goods-status.on { background: #e6f7e6; color: #07c160; }
.goods-status.off { background: #ffe6e6; color: #ff4444; }
</style>