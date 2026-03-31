<template>
  <div class="admin-page">
    <!-- 顶部导航 -->
    <van-nav-bar
      title="订单统计"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <div class="container" style="margin-top: 50px; padding-bottom: 80px">
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <van-search
          v-model="searchKey"
          placeholder="搜索订单号/用户ID"
          show-action
          @search="handleSearch"
          class="search-bar"
        />
        <van-dropdown-menu active-color="#ff9f00">
          <van-dropdown-item v-model="filterStatus" :options="statusOptions" @change="handleFilter" />
          <van-dropdown-item v-model="filterUnfinished" :options="unfinishedOptions" @change="handleFilter" />
        </van-dropdown-menu>
      </div>

      <!-- 订单列表 -->
      <van-cell-group inset class="list-card">
        <van-cell
          v-for="order in filteredOrderList"
          :key="order.id"
          is-link
          @click="openOrderDetail(order)"
        >
          <template #title>
            <div class="order-row">
              <div>
                <div class="order-no">{{ order.orderNo }}</div>
                <div class="order-info-text">
                  用户ID:{{ order.userId }} | 商家ID:{{ order.merchantId }} | 金额:¥{{ order.totalAmount }}
                </div>
              </div>
              <van-tag :type="orderStatusMap[order.status].type" size="small">
                {{ orderStatusMap[order.status].text }}
              </van-tag>
            </div>
          </template>
        </van-cell>

        <div v-if="filteredOrderList.length === 0" class="empty">
          <van-empty description="暂无订单数据" />
        </div>
      </van-cell-group>
    </div>

    <!-- ====================== 订单详情弹窗 ====================== -->
    <van-popup v-model:show="showDetailPopup" position="bottom" :style="{ height: '90%', borderRadius: '16px 16px 0 0' }">
      <div class="detail-popup">
        <div class="popup-header">
          <div class="popup-title">订单详情</div>
          <van-icon name="cross" @click="showDetailPopup = false" />
        </div>

      <!-- 订单基本信息 -->
      <van-cell-group inset class="info-group">
        <van-cell title="订单ID" :value="currentOrder.id" />
        <van-cell title="订单号" :value="currentOrder.orderNo" />
        <!-- ✅ 用户名：后端字段是 username -->
        <van-cell title="用户" :value="currentOrder.username || '未知用户'" />
        <!-- ✅ 商家名：后端字段是 merchantName -->
        <van-cell title="商家" :value="currentOrder.merchantName || '未知商家'" />
        <van-cell title="总金额" :value="`¥${currentOrder.totalAmount}`" />
        <van-cell title="当前状态">
          <template #extra>
            <van-tag :type="orderStatusMap[currentOrder.status].type">
              {{ orderStatusMap[currentOrder.status].text }}
            </van-tag>
          </template>
        </van-cell>
        <van-cell title="地址" :value="currentOrder.addressText || currentOrder.fullAddress" />
        <van-cell title="创建时间" :value="formatTime(currentOrder.createTime)" />
        <van-cell title="支付时间" :value="formatTime(currentOrder.payTime) || '未支付'" />
        <van-cell title="发货时间" :value="formatTime(currentOrder.deliverTime) || '未发货'" />
        <van-cell title="收货时间" :value="formatTime(currentOrder.receiveTime) || '未收货'" />
      </van-cell-group>

     <!-- 交易日志 -->
      <div class="log-section">
        <div class="section-title">交易日志</div>
        <van-cell-group inset class="log-group">
          <van-cell
            v-for="log in orderLogs"
            :key="log.id"
          >
      <template #title>
            <div class="log-row">
              <div class="log-action">{{ actionMap[log.action] || log.action }}</div>
              <div class="log-time">{{ formatTime(log.createTime) }}</div>
            </div>
          </template>
          <template #label>
            <div class="log-desc">
              操作人：{{ log.operatorName }}({{ log.operatorType }})
              <br/>
              备注：{{ log.remark || '无' }}
            </div>
          </template>
        </van-cell>
      </van-cell-group>
    </div>
          </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { showToast } from 'vant'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()

// 搜索与筛选
const searchKey = ref('')
const filterStatus = ref('')
const filterUnfinished = ref('')

// 筛选选项
const statusOptions = ref([
  { text: '全部状态', value: '' },
  { text: '待支付', value: 'PENDING_PAY' },
  { text: '已支付', value: 'PAID' },
  { text: '已发货', value: 'DELIVERED' },
  { text: '已完成', value: 'RECEIVED' },
  { text: '已取消', value: 'CANCELLED' }
])

const unfinishedOptions = ref([
  { text: '全部订单', value: '' },
  { text: '超过24小时未完成', value: '24h' },
  { text: '超过48小时未完成', value: '48h' },
  { text: '超过72小时未完成', value: '72h' }
])

// 🔥 订单列表（必须是数组，后端返回 IPage）
const orderList = ref([])

// 订单状态映射
const orderStatusMap = {
  PENDING_PAY: { type: 'warning', text: '待支付' },
  PAID: { type: 'primary', text: '已支付' },
  DELIVERED: { type: 'orange', text: '已发货' },
  RECEIVED: { type: 'success', text: '已完成' },
  CANCELLED: { type: 'danger', text: '已取消' },
  REFUNDED: { type: 'danger', text: '已退款' }
}

// 操作日志文本映射
const actionMap = {
  CREATE: '创建订单',
  PAY: '支付订单',
  DELIVER: '商家发货',
  RECEIVE: '确认收货',
  CANCEL: '取消订单',
  REFUND: '申请退款'
}

// 弹窗
const showDetailPopup = ref(false)
const currentOrder = ref({})
const orderLogs = ref([])

// ======================
// ✅ 核心修复：筛选逻辑完全适配后端
// ======================
const filteredOrderList = computed(() => {
  const result = [...(Array.isArray(orderList.value) ? orderList.value : [])]

  // 搜索过滤（订单号 / 用户ID）
  if (searchKey.value) {
    return result.filter(o =>
      (o.orderNo && o.orderNo.includes(searchKey.value)) ||
      String(o.userId).includes(searchKey.value)
    )
  }

  // 状态过滤
  if (filterStatus.value) {
    return result.filter(o => o.status === filterStatus.value)
  }

  // 长时间未完成过滤（后端逻辑一致）
  if (filterUnfinished.value) {
    const hourMap = { '24h': 24, '48h': 48, '72h': 72 }
    const hours = hourMap[filterUnfinished.value]
    const now = new Date()
    return result.filter(o => {
      if (o.status === 'COMPLETED' || o.status === 'CANCELLED' || o.status === 'REFUNDED') return false
      const createTime = new Date(o.createTime)
      const diff = (now - createTime) / (1000 * 60 * 60)
      return diff >= hours
    })
  }

  return result
})

// ======================================
// 🔥 修复：管理员订单列表（正确对接 IPage 分页结构）
// ======================================
const loadOrderList = async () => {
  try {
    const res = await fetch('/api/order/admin/list', {
      headers: {
        Authorization: 'Bearer ' + userStore.token
      }
    })
    const data = await res.json()
    if (data.code === 200) {
      // ✅ 关键：后端返回 IPage，必须取 records
      orderList.value = Array.isArray(data.data?.records) ? data.data.records : []
    } else {
      showToast('获取订单失败：' + data.msg)
    }
  } catch (e) {
    showToast('加载订单列表失败')
    console.error(e)
  }
}

// ======================================
// 搜索 / 筛选（完善逻辑）
// ======================================
const handleSearch = () => {
  // 触发重新请求（如果你要做搜索参数，也可以带上）
  loadOrderList()
}

const handleFilter = () => {
  // 筛选变化后重新请求
  loadOrderList()
}

// ======================================
// 打开订单详情
// ======================================
// ======================
// 打开订单详情（补充获取用户名/商家名）
// ======================
const openOrderDetail = async (order) => {
  currentOrder.value = order
  showDetailPopup.value = true
  // ✅ 直接用后端封装好的字段，不再发请求
  currentOrder.value.username = order.username
  currentOrder.value.merchantName = order.merchantName
  currentOrder.value.addressText = order.addressText
  await loadOrderLogs(order.id)
}

// ======================================
// 🔥 加载订单日志（完全匹配你最新后端接口）
// ======================================
const loadOrderLogs = async (orderId) => {
  try {
    const res = await fetch(`/api/order/log/${orderId}`, {
      headers: {
        Authorization: 'Bearer ' + userStore.token
      }
    })
    const data = await res.json()
    if (data.code === 200) {
      // ✅ 后端返回 List<OrderLog>，直接赋值
      orderLogs.value = data.data || []
    }
  } catch (e) {
    console.error('加载日志失败', e)
  }
}

// 时间格式化
const formatTime = (t) => t ? new Date(t).toLocaleString() : null

onMounted(() => {
  loadOrderList()
})
</script>

<style scoped>
.admin-page {
  background: #fff9e6;
  min-height: 100vh;
}

.moon-nav-bar {
  background: #ffe181;
}

.container {
  padding: 10px 15px;
}

.filter-bar {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 12px;
}

.search-bar {
  background: #fff;
  border-radius: 12px;
}

.list-card {
  background: #fff;
  border-radius: 12px;
}

.order-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.order-no {
  font-weight: bold;
  font-size: 15px;
  margin-bottom: 4px;
}

.order-info-text {
  font-size: 12px;
  color: #999;
}

.empty {
  padding: 40px 0;
}

/* 弹窗样式 */
.detail-popup {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
  box-sizing: border-box;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.popup-title {
  font-size: 18px;
  font-weight: bold;
}

.info-group,
.log-group {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  padding-left: 2px;
}

.log-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.log-action {
  font-weight: bold;
  font-size: 14px;
}

.log-time {
  font-size: 12px;
  color: #999;
}

.log-desc {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}
</style>