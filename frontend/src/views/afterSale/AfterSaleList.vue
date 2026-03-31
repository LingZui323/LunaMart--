<template>
  <div class="after-sale-list-page">
    <van-nav-bar
      title="售后记录"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <div class="container" style="margin-top: 46px; padding: 16px;">
      <van-tabs v-model:active="activeTab" class="tab-bar" @change="loadAfterSaleList">
        <van-tab title="全部" name="" />
        <van-tab title="待处理" name="PENDING" />
        <van-tab title="已同意" name="APPROVED" />
        <van-tab title="已拒绝" name="REJECTED" />
        <van-tab title="平台介入" name="PLATFORM" />
      </van-tabs>

      <div v-if="afterSaleList.length === 0" class="empty-tip">
        <van-empty description="暂无售后记录" />
      </div>

      <!-- 🔥 卡片展示所有信息 -->
      <div class="after-sale-card" v-for="item in afterSaleList" :key="item.id">
        <div class="card-header">
          <div class="order-info">
            <span class="label">订单编号：</span>
            <span class="value">{{ item.orderNo }}</span>
          </div>
          <div class="status">{{ statusMap[item.status] }}</div>
        </div>

        <div class="card-body">
          <div class="row">
            <span class="label">商品信息：</span>
            <span class="value">{{ item.goodsTitle || '未知商品' }}</span>
          </div>
          <div class="row">
            <span class="label">订单金额：</span>
            <span class="value">¥{{ item.totalAmount }}</span>
          </div>
          <div class="row">
            <span class="label">售后类型：</span>
            <span class="value">{{ typeMap[item.type] }}</span>
          </div>
          <div class="row reason-row">
            <span class="label">退款原因：</span>
            <span class="value">{{ item.reason }}</span>
          </div>
        </div>

        <div class="card-handle" v-if="item.handleRemark">
          <div class="handle-title">商家处理备注：</div>
          <div class="handle-content">{{ item.handleRemark }}</div>
        </div>

        <div class="card-footer">
          <div class="time">{{ formatTime(item.createTime) }}</div>
          <van-button
            v-if="item.status === 'REJECTED'"
            size="mini"
            type="primary"
            plain
            @click.stop="handlePlatformApply(item)"
          >
            申请平台介入
          </van-button>
            <!-- 🔥 新增：用户确认退货按钮 -->
          <van-button
            v-if="item.status === 'APPROVED' && item.type === 'RETURN'"
            size="mini"
            type="success"
            plain
            @click.stop="handleUserReturnGoods(item)"
          >
            确认已退货
          </van-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../store/user'
import { showToast, showConfirmDialog } from 'vant'

const userStore = useUserStore()

const activeTab = ref('')
const afterSaleList = ref([])

const statusMap = {
  PENDING: '待商家审核',
  APPROVED: '商家同意',
  REJECTED: '商家拒绝',
  PLATFORM: '平台处理中',
  COMPLETED: '已完成'
}

const typeMap = {
  REFUND: '仅退款',
  RETURN: '退货退款'
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString()
}

// 🔥 极简加载：后端已返回所有信息
const loadAfterSaleList = async () => {
  if (!userStore.isLogin) {
    showToast('请先登录')
    return
  }
  try {
    const res = await fetch('/api/after/sale/user/list', {
      headers: {
        'Authorization': 'Bearer ' + userStore.token
      }
    })
    const data = await res.json()
    if (data.code === 200) {
      const list = data.data?.records || []
      if (activeTab.value) {
        afterSaleList.value = list.filter(item => item.status === activeTab.value)
      } else {
        afterSaleList.value = list
      }
    }
  } catch (err) {
    showToast('加载失败')
  }
}

const handlePlatformApply = async (item) => {
  try {
    await showConfirmDialog('确认申请平台介入？')
    const res = await fetch(`/api/after/sale/apply/platform/${item.id}`, {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ' + userStore.token
      }
    })
    const data = await res.json()
    if (data.code === 200) {
      showToast('平台介入申请成功')
      loadAfterSaleList()
    } else {
      showToast(data.msg || '申请失败')
    }
  } catch {
    showToast('已取消')
  }
}

// 🔥 新增：用户确认退货
const handleUserReturnGoods = async (item) => {
  try {
    await showConfirmDialog('确认已经退货并寄回商品？')
    const res = await fetch(`/api/after/sale/user/return/${item.id}`, {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ' + userStore.token
      }
    })
    const data = await res.json()
    if (data.code === 200) {
      showToast('已确认退货')
      loadAfterSaleList()
    } else {
      showToast(data.msg || '操作失败')
    }
  } catch {
    showToast('已取消')
  }
}
onMounted(() => loadAfterSaleList())
</script>

<style scoped>
.after-sale-list-page {
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
  padding-bottom: 20px;
}

.tab-bar {
  background: #fff;
  margin-bottom: 16px;
  border-radius: 12px;
}

.empty-tip {
  padding: 60px 0;
  text-align: center;
}

.after-sale-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.order-info .label {
  font-size: 14px;
  color: #666;
}
.order-info .value {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}
.status {
  font-size: 14px;
  color: #ff9f00;
  font-weight: 500;
}

.card-body {
  margin-bottom: 12px;
}
.row {
  display: flex;
  margin-bottom: 8px;
}
.row .label {
  width: 90px;
  font-size: 14px;
  color: #666;
  flex-shrink: 0;
}
.row .value {
  font-size: 14px;
  color: #333;
  flex: 1;
}
.reason-row .value {
  color: #f56c6c;
}

.card-handle {
  background: #fef3e0;
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 12px;
}
.handle-title {
  font-size: 13px;
  color: #ff976a;
  margin-bottom: 4px;
}
.handle-content {
  font-size: 14px;
  color: #333;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.time {
  font-size: 12px;
  color: #999;
}
</style>