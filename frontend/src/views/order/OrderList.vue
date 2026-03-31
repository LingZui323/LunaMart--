<template>
  <div class="order-list-page">
    <!-- 顶部导航 -->
    <van-nav-bar
      title="我的订单"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <!-- 状态筛选栏 -->
    <van-tabs v-model:active="activeTab" sticky class="status-tabs">
      <van-tab title="全部" name="all" />
      <van-tab title="待支付" name="PENDING_PAY" />
      <van-tab title="已支付" name="PAID" />
      <van-tab title="已发货" name="DELIVERED" />
      <van-tab title="已完成" name="RECEIVED" />
    </van-tabs>

    <!-- 搜索 + 时间筛选 -->
    <div class="search-bar-wrapper">
      <van-search
        v-model="goodsName"
        placeholder="搜索商品名称"
        @search="loadOrderList"
        shape="round"
        background="transparent"
      />

      <!-- 时间筛选 -->
      <div class="time-filter-row">
        <van-field
          v-model="startTime"
          type="date"
          placeholder="开始时间"
          style="flex:1; font-size:12px;"
        />
        <span style="margin:0 6px;">-</span>
        <van-field
          v-model="endTime"
          type="date"
          placeholder="结束时间"
          style="flex:1; font-size:12px;"
        />
        <van-button
          size="mini"
          type="primary"
          color="#ff9f00"
          @click="loadOrderList"
        >
          筛选
        </van-button>
      </div>
    </div>

    <!-- 内容容器 -->
    <div class="container">
      <van-empty v-if="orderList.length === 0" description="暂无订单" />

      <van-cell-group v-else inset class="order-list">
        <van-cell
          v-for="order in orderList"
          :key="order.id"
          class="order-item"
          is-link
          @click="goToOrderDetail(order.id)"
        >
          <template #title>
            <div class="order-header">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <van-tag :type="orderStatusMap[order.status]?.type || 'default'" size="small">
                {{ orderStatusMap[order.status]?.text || order.status }}
              </van-tag>
            </div>
            <div class="order-info">
              <div class="merchant-info">商品：{{ order.goodsNames || '暂无商品' }}</div>
              <div class="amount-info">商家：{{ order.merchantName || '商家' }}</div>
            </div>
            <div class="order-time">
              创建时间：{{ formatTime(order.createTime) }}
            </div>
          </template>
        </van-cell>
      </van-cell-group>

      <!-- 分页组件（只加颜色，不动逻辑） -->
      <van-pagination
        v-if="total > 0"
        v-model:current-page="page"
        v-model:page-size="size"
        :total-items="total"
        :show-ellipsis="false"
        @change="loadOrderList"
        class="pagination"
        active-color="#ff9f00"
      />

      <van-loading v-if="loading" class="loading">加载中...</van-loading>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

// 筛选条件
const activeTab = ref('all')
const goodsName = ref('')
const startTime = ref('')
const endTime = ref('')

// 分页
const page = ref(1)
const size = ref(10)
const total = ref(0)
const orderList = ref([])
const loading = ref(false)

// 状态映射
const orderStatusMap = {
  PENDING_PAY: { type: 'warning', text: '待支付' },
  PAID: { type: 'primary', text: '已支付' },
  DELIVERED: { type: 'orange', text: '已发货' },
  RECEIVED: { type: 'success', text: '已完成' },
  CANCELLED: { type: 'danger', text: '已取消' },
  REFUNDED: { type: 'default', text: '已退款' }
}

// 加载订单列表
const loadOrderList = async () => {
  if (!userStore.isLogin) {
    orderList.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const status = activeTab.value === 'all' ? '' : activeTab.value

    const res = await fetch(`/api/order/my?page=${page.value}&size=${size.value}&status=${status}&startTime=${startTime.value || ''}&endTime=${endTime.value || ''}&goodsName=${encodeURIComponent(goodsName.value)}`, {
      headers: {
        Authorization: `Bearer ${userStore.token}`
      }
    })

    const data = await res.json()
    if (data.code === 200) {
      orderList.value = data.data.records || []
      total.value = data.data.total || 0
    } else {
      showToast(data.msg || '加载失败')
    }
  } catch (e) {
    showToast('加载订单失败')
  } finally {
    loading.value = false
  }
}

// 切换状态 → 重置页码
watch(activeTab, () => {
  page.value = 1
  loadOrderList()
})

// 搜索/筛选 → 重置页码
watch([goodsName, startTime, endTime], () => {
  page.value = 1
})

// 跳转详情
const goToOrderDetail = (id) => router.push(`/order/detail/${id}`)

// 时间格式化
const formatTime = (t) => t ? new Date(t).toLocaleString() : ''

onMounted(() => loadOrderList())
</script>

<style scoped>
.order-list-page {
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

.status-tabs {
  --van-tab-active-text-color: #ff9f00;
  --van-tab-bottom-line-color: #ff9f00;
  position: sticky;
  top: 46px;
  background: #fff;
  z-index: 10;
}

.search-bar-wrapper {
  padding: 10px 15px;
  background: #fff9e6;
}

.time-filter-row {
  display: flex;
  align-items: center;
  margin-top: 8px;
  gap: 4px;
}

/* 只改这里：缩小顶部空白，让订单往上移 */
.container {
  margin-top: 50px !important;
  padding: 10px 15px;
}

.order-list {
  border-radius: 12px;
}

.order-item {
  padding: 12px 15px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.order-no {
  font-weight: bold;
}

.order-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
}

.order-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.pagination {
  margin: 10px 0;
  text-align: center;
}

.loading {
  text-align: center;
  padding: 10px;
  color: #999;
}
</style>