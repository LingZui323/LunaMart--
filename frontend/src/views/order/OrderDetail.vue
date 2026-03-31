<template>
  <div class="order-detail-page">
    <!-- 顶部导航 -->
    <van-nav-bar
      title="订单详情"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <div class="container" style="margin-top: 50px; padding-bottom: 80px">
      <!-- 1. 收货地址（用订单快照，避免404） -->
      <div class="address-card">
        <van-cell
          :title="orderDetail.receiver || '未设置收货地址'"
          :value="orderDetail.phone || ''"
          :label="orderDetail.fullAddress || ''"
        />
      </div>

      <!-- 2. 订单基本信息（隐藏ID，显示商家名+用户名+商品名） -->
      <van-cell-group inset class="info-card">
        <!-- 商品名称 -->
        <van-cell title="商品名称" :value="goodsName" />
        <!-- 用户名 -->
        <van-cell title="用户名" :value="userName" />
        <!-- 商家店铺名 -->
        <van-cell title="商家店铺" :value="merchantName" />
        <!-- 总金额 -->
        <van-cell title="总金额" :value="`¥${orderDetail.totalAmount}`" />
        <!-- 状态 -->
        <van-cell title="当前状态">
          <template #extra>
            <van-tag :type="orderStatusMap[orderDetail.status]?.type || 'default'">
              {{ orderStatusMap[orderDetail.status]?.text || '未知状态' }}
            </van-tag>
          </template>
        </van-cell>
      </van-cell-group>

      <!-- 新增：商品列表（展示所有订单项） -->
        <van-cell-group inset class="goods-card">
          <van-cell title="商品列表" />
          <div v-for="item in orderDetail.itemList" :key="item.id" class="order-item">
            <div class="item-name">{{ item.goodsTitle }}</div>
            <div class="item-info">
              <span>¥{{ item.price }} × {{ item.quantity }}</span>
            </div>
          </div>
        </van-cell-group>

      <!-- 3. 时间节点（增加支付超时判断） -->
      <van-cell-group inset class="time-card">
        <van-cell title="创建时间" :value="formatTime(orderDetail.createTime)" />
        <van-cell
          title="支付时间"
          :value="isPayTimeout ? '超时未支付' : (formatTime(orderDetail.payTime) || '未支付')"
          :class="isPayTimeout ? 'text-danger' : ''"
        />
        <van-cell title="发货时间" :value="formatTime(orderDetail.deliverTime) || '未发货'" />
        <van-cell title="收货时间" :value="formatTime(orderDetail.receiveTime) || '未收货'" />
      </van-cell-group>

      <!-- 4. 操作按钮 -->
      <div class="action-buttons">
        <van-button
          v-if="orderDetail.status === 'PENDING_PAY' && !isPayTimeout"
          type="primary"
          color="#ff9f00"
          block
          @click="handlePay"
        >
          立即支付
        </van-button>

        <van-button
          v-if="orderDetail.status === 'DELIVERED'"
          type="success"
          block
          @click="handleConfirmReceive"
        >
          确认收货
        </van-button>

        <van-button
          v-if="['PENDING_PAY'].includes(orderDetail.status)"
          type="danger"
          plain
          block
          @click="handleCancel"
        >
          取消订单
        </van-button>

                <!-- ✅ 申请售后：仅【已完成】状态显示 -->
        <van-button
          v-if="orderDetail.status === 'RECEIVED'"
          type="primary"
          plain
          block
          @click="goToRefund"
        >
          申请售后
        </van-button>

         <!-- 🔥 新增：与商家协商按钮（风格统一） -->
        <van-button
          type="primary"
          plain
          block
          color="#ff9f00"
          @click="handleChatWithMerchant"
        >
          与商家协商
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { showToast, showConfirmDialog } from 'vant'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const orderId = route.params.id

// 订单详情
const orderDetail = ref({ status: '' })
// 商品名称（取第一个订单项）
const goodsName = ref('')
// 用户名（后端如果有userName字段，直接用；没有则默认"用户"）
const userName = ref('用户')
// 商家店铺名（后端如果有merchantName字段，直接用；没有则默认"商家"）
const merchantName = ref('商家')

// 状态映射
const orderStatusMap = {
  PENDING_PAY: { type: 'warning', text: '待支付' },
  PAID: { type: 'primary', text: '已支付' },
  DELIVERED: { type: 'orange', text: '已发货' },
  RECEIVED: { type: 'success', text: '已完成' },
  CANCELLED: { type: 'danger', text: '已取消' },
  REFUNDED: { type: 'default', text: '已退款' }
}

// ======================
// 计算属性：判断是否支付超时（30分钟）
// ======================
const isPayTimeout = computed(() => {
  if (!orderDetail.value.payTime || orderDetail.value.status !== 'PENDING_PAY') {
    return false
  }
  // 30分钟超时时间（与后端一致）
  const timeout = 30 * 60 * 1000
  return Date.now() - new Date(orderDetail.value.payTime).getTime() > timeout
})

// ======================
// 加载订单详情
// ======================
const loadOrderDetail = async () => {
  if (!userStore.isLogin) return
  try {
    const res = await fetch(`/api/order/detail/${orderId}`, {
      headers: { Authorization: 'Bearer ' + userStore.token }
    })
    const data = await res.json()
    if (data.code === 200) {
      orderDetail.value = data.data
      
      // 解析商品名称（取第一个）
      if (data.data.itemList && data.data.itemList.length > 0) {
      goodsName.value = data.data.itemList.map(item => item.goodsTitle).join('、')
    }
          
      // 解析用户名/商家名（如果后端返回了关联数据）
      if (data.data.user) {
        userName.value = data.data.user.username
      }
      if (data.data.merchant) {
        merchantName.value = data.data.merchant.name
      }
    }
  } catch (err) {
    showToast('加载订单失败')
  }
}

// ======================
// 支付
// ======================
const handlePay = async () => {
  try {
    const res = await fetch(`/api/order/pay/${orderId}`, {
      method: 'PUT',
      headers: { Authorization: 'Bearer ' + userStore.token }
    })
    const data = await res.json()
    if (data.code === 200) {
      showToast('支付成功')
      orderDetail.value.status = 'PAID'
    }
  } catch (e) {}
}

// ======================
// 确认收货
// ======================
const handleConfirmReceive = async () => {
  try {
    await showConfirmDialog('确认收货？')
    const res = await fetch(`/api/order/receive/${orderId}`, {
      method: 'PUT',
      headers: { Authorization: 'Bearer ' + userStore.token }
    })
    const data = await res.json()
    if (data.code === 200) {
      orderDetail.value.status = 'RECEIVED'
      showToast('收货成功')
    }
  } catch (e) {}
}

// ======================
// 取消订单
// ======================
const handleCancel = async () => {
  try {
    await showConfirmDialog('确定取消订单？')
    const res = await fetch(`/api/order/cancel/${orderId}`, {
      method: 'PUT',
      headers: { Authorization: 'Bearer ' + userStore.token }
    })
    const data = await res.json()
    if (data.code === 200) {
      orderDetail.value.status = 'CANCELLED'
      showToast('取消成功')
    }
  } catch (e) {}
}

// ======================
// ✅ 申请售后（跳转售后页面）
// ======================
const goToRefund = () => {
  router.push({
    path: '/aftersale/apply',
    query: { orderId: orderId }
  })
}


// ======================
// 🔥 与商家协商（带订单ID跳转）
// ======================
const handleChatWithMerchant = () => {
  const merchantId = orderDetail.value.merchantId
  if (!merchantId) {
    showToast('商家信息异常')
    return
  }

  // ✅ 关键：带上 orderId
  router.push({
    path: `/chat/${merchantId}`,
    query: {
      targetName: merchantName.value || '商家',
      orderId: orderId // 这里！
    }
  })
}

// 时间格式化
const formatTime = (t) => t ? new Date(t).toLocaleString() : ''

onMounted(() => loadOrderDetail())
</script>

<style scoped>
.order-detail-page {
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
  padding: 10px 15px;
}

.address-card {
  margin-bottom: 10px;
  background: #fff;
  border-radius: 12px;
}

.info-card,
.time-card {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 15px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 20px;
}

.text-danger {
  color: #f56c6c;
}

.goods-card {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 15px;
}
.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  border-bottom: 1px solid #f5f5f5;
}
.order-item:last-child {
  border-bottom: none;
}
.item-name {
  font-size: 14px;
  color: #333;
}
.item-info {
  font-size: 13px;
  color: #666;
}

</style>