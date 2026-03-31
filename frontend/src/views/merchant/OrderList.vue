<template>
  <div class="merchant-order-page">
    <van-nav-bar
      title="订单管理"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <div class="container" style="margin-top: 46px; padding-bottom: 20px">
      <div class="filter-bar">
        <van-dropdown-menu active-color="#ff9f00">
          <van-dropdown-item v-model="filterStatus" :options="statusOptions" @change="handleFilter" />
          <van-dropdown-item v-model="filterDate" :options="dateOptions" @change="handleFilter" />
        </van-dropdown-menu>
      </div>

      <div v-for="group in groupedOrders" :key="group.date" class="order-group">
        <div class="group-date">{{ group.date }}</div>
        
        <div v-for="order in group.orders" :key="order.id" class="order-card">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-status" :class="statusClass(order.status)">
              {{ statusText(order.status) }}
            </span>
          </div>

          <div class="order-info">
            <div class="info-item">
              <span>订单金额：</span>
              <span class="amount">¥{{ order.totalAmount }}</span>
            </div>
            <div class="info-item">
              <span>创建时间：</span>
              <span>{{ formatTime(order.createTime) }}</span>
            </div>
            <div class="info-item" v-if="order.payTime">
              <span>支付时间：</span>
              <span>{{ formatTime(order.payTime) }}</span>
            </div>
            <div class="info-item" v-if="order.deliverTime">
              <span>发货时间：</span>
              <span>{{ formatTime(order.deliverTime) }}</span>
            </div>
            <div class="info-item" v-if="order.receiveTime">
              <span>收货时间：</span>
              <span>{{ formatTime(order.receiveTime) }}</span>
            </div>
          </div>

          <div class="order-actions">
            <van-button size="small" type="primary" plain @click="goChat(order.userId, order.id)">
              联系用户
            </van-button>
            <van-button size="small" type="primary" color="#ff9f00" @click="handleOperate(order)">
              {{ actionText(order.status) }}
            </van-button>
          </div>
        </div>
      </div>

      <div v-if="groupedOrders.length === 0 && !loading" class="empty-state">
        <van-empty description="暂无订单" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()

const filterStatus = ref('')
const filterDate = ref('all')
const orders = ref([])
const loading = ref(false)

const statusOptions = ref([
  { text: '全部状态', value: '' },
  { text: '待支付', value: 'PENDING_PAY' },
  { text: '已支付', value: 'PAID' },
  { text: '已发货', value: 'DELIVERED' },
  { text: '已收货', value: 'RECEIVED' },
  { text: '已取消', value: 'CANCELLED' },
  { text: '已退款', value: 'REFUNDED' },
])

const dateOptions = ref([
  { text: '全部日期', value: 'all' },
  { text: '今天', value: 'today' },
  { text: '昨天', value: 'yesterday' },
  { text: '本周', value: 'week' },
])

const loadOrders = async () => {
  try {
    loading.value = true
    const res = await fetch(`/api/order/merchant/list?timeType=${filterDate.value}&page=1&size=10`, {
      headers: {
        Authorization: `Bearer ${userStore.token}`
      }
    })
    const data = await res.json()
    if (data.code === 200) {
      orders.value = data.data.records || []
    }
  } catch (err) {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

const groupedOrders = computed(() => {
  let filtered = Array.isArray(orders.value) ? [...orders.value] : []
  if (filterStatus.value) {
    filtered = filtered.filter(o => o.status === filterStatus.value)
  }
  const groups = {}
  filtered.forEach(o => {
    const date = o.createTime ? o.createTime.split('T')[0] : '未知日期'
    if (!groups[date]) groups[date] = []
    groups[date].push(o)
  })
  return Object.entries(groups).map(([date, list]) => ({
    date,
    orders: list.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  })).sort((a, b) => new Date(b.date) - new Date(a.date))
})

const statusText = (status) => {
  const map = {
    PAID: '已支付',
    DELIVERED: '已发货',
    RECEIVED: '已收货',
    CANCELLED: '已取消',
    REFUNDED: '已退款',
    PENDING_PAY: '待支付'
  }
  return map[status] || status
}

const statusClass = (status) => {
  const map = {
    PAID: 'status-paid',
    DELIVERED: 'status-delivered',
    RECEIVED: 'status-received',
    CANCELLED: 'status-cancelled',
    REFUNDED: 'status-refunded',
    PENDING_PAY: 'status-pending'
  }
  return map[status] || ''
}

const actionText = (status) => {
  const map = {
    PAID: '发货',
    DELIVERED: '查看物流',
    RECEIVED: '已完成',
    CANCELLED: '已取消',
    REFUNDED: '售后完成',
    PENDING_PAY: '待支付'
  }
  return map[status] || '操作'
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

const handleFilter = () => {
  loadOrders()
}

watch([filterStatus, filterDate], () => {
  loadOrders()
})

const goChat = (userId, orderId) => {
  router.push({
    path: `/chat/${userId}`,
    query: {
      targetName: '用户',
      orderId: orderId
    }
  })
}

const handleOperate = async (order) => {
  if (order.status === 'PAID') {
    const toast = showLoadingToast('发货中...')
    try {
      const res = await fetch(`/api/order/deliver/${order.id}`, {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${userStore.token}`
        }
      })
      const data = await res.json()
      if (data.code === 200) {
        showToast('发货成功')
        loadOrders()
      } else {
        showToast(data.msg || '发货失败')
      }
    } catch (e) {
      showToast('发货失败')
    } finally {
      closeToast()
    }
  } else {
    showToast('操作成功')
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.merchant-order-page {
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

.container {
  padding: 10px;
}

.filter-bar {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 15px;
  padding: 10px;
}

.order-group {
  margin-bottom: 20px;
}
.group-date {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  padding-left: 5px;
}

.order-card {
  background: #fff;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 10px;
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.order-no {
  font-size: 14px;
  color: #333;
}
.order-status {
  font-size: 13px;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-paid { color: #1989fa; background: #e8f3ff; }
.status-delivered { color: #ff9f00; background: #fff3e0; }
.status-received { color: #07c160; background: #e6f7e6; }
.status-pending { color: #ff976a; background: #fff7e6; }
.status-cancelled { color: #969799; background: #f2f3f5; }
.status-refunded { color: #ff4444; background: #ffeaea; }

.order-info {
  font-size: 13px;
  color: #666;
  margin-bottom: 15px;
}
.info-item {
  margin-bottom: 6px;
}
.amount {
  color: #ff4444;
  font-weight: bold;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.empty-state {
  padding: 60px 0;
}
</style>