<template>
  <div class="aftersale-page">
    <van-nav-bar
      title="售后处理"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <div class="container" style="margin-top: 46px; padding-bottom: 20px">
      <div class="filter-bar">
        <van-dropdown-menu active-color="#ff9f00">
          <van-dropdown-item v-model="filterType" :options="typeOptions" @change="handleFilter" />
          <van-dropdown-item v-model="filterStatus" :options="statusOptions" @change="handleFilter" />
        </van-dropdown-menu>
      </div>

      <div v-for="item in filteredAfterSales" :key="item.id" class="aftersale-card">
        <div class="aftersale-header">
          <div class="aftersale-id">售后ID：{{ item.id }}</div>
          <div class="aftersale-status" :class="statusClass(item.status)">
            {{ statusText(item.status) }}
          </div>
        </div>

        <div class="aftersale-info">
          <div class="info-item">
            <span>订单编号：{{ item.orderNo }}</span>
          </div>
          <div class="info-item">
            <span>商品名称：{{ item.goodsTitle }}</span>
          </div>
          <div class="info-item">
            <span>订单金额：¥{{ item.totalAmount }}</span>
          </div>
          <div class="info-item">
            <span>售后类型：{{ typeText(item.type) }}</span>
          </div>
          <div class="info-item reason">
            <span>申请原因：{{ item.reason }}</span>
          </div>
          <div class="info-item">
            <span>申请时间：{{ formatTime(item.createTime) }}</span>
          </div>
          <div class="info-item" v-if="item.updateTime">
            <span>处理时间：{{ formatTime(item.updateTime) }}</span>
          </div>
          <div class="info-item" v-if="item.handleRemark">
            <span>处理备注：{{ item.handleRemark }}</span>
          </div>
        </div>

        <div class="aftersale-actions" v-if="item.status === 'PENDING'">
          <van-button size="small" type="primary" plain @click="handleRefuse(item)">拒绝</van-button>
          <van-button size="small" type="primary" color="#ff9f00" @click="handleApprove(item)">同意</van-button>
        </div>
        <div class="aftersale-actions" v-if="item.status === 'RETURNED' && item.type === 'RETURN'">
          <van-button 
            size="small" 
            type="success" 
            @click="handleMerchantConfirmReceive(item)"
          >
            确认收到退货
          </van-button>
        </div>
      </div>

      <div v-if="filteredAfterSales.length === 0" class="empty-state">
        <van-empty description="暂无售后申请" />
      </div>
    </div>

    <van-popup v-model:show="showHandleDialog" position="bottom" :style="{ height: '50%', padding: '20px', borderRadius: '12px 12px 0 0' }">
      <div class="dialog-header">
        <span @click="showHandleDialog = false">取消</span>
        <span class="title">{{ handleAction === 'APPROVED' ? '同意售后' : '拒绝售后' }}</span>
        <span @click="submitHandle" class="confirm">提交</span>
      </div>
      <div class="handle-form">
        <van-field v-model="handleForm.remark" label="处理备注" type="textarea" rows="4" placeholder="请输入处理备注" />
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter()
const userStore = useUserStore()

// 筛选
const filterType = ref('')
const filterStatus = ref('')

const typeOptions = ref([
  { text: '全部类型', value: '' },
  { text: '仅退款', value: 'REFUND' },
  { text: '退货退款', value: 'RETURN' }
])

const statusOptions = ref([
  { text: '全部状态', value: '' },
  { text: '待处理', value: 'PENDING' },
  { text: '已同意', value: 'APPROVED' },
  { text: '已拒绝', value: 'REJECTED' },
  { text: '平台介入', value: 'PLATFORM' },
  { text: '已完成', value: 'COMPLETED' }
])

// 真实售后列表
const afterSales = ref([])

const filteredAfterSales = computed(() => {
  let filtered = [...afterSales.value]
  if (filterType.value) filtered = filtered.filter(i => i.type === filterType.value)
  if (filterStatus.value) filtered = filtered.filter(i => i.status === filterStatus.value)
  return filtered
})

// 文本映射（完全匹配后端）
const typeText = (type) => {
  const map = { REFUND: '仅退款', RETURN: '退货退款' }
  return map[type] || type
}

const statusText = (status) => {
  const map = {
    PENDING: '待处理',
    APPROVED: '已同意',
    REJECTED: '已拒绝',
    PLATFORM: '平台介入中',
    COMPLETED: '已完成',
    RETURNED: '用户已退货' // 🔥 新增
  }
  return map[status] || status
}

const statusClass = (status) => {
  const map = {
    PENDING: 'status-pending',
    APPROVED: 'status-approved',
    REJECTED: 'status-rejected',
    PLATFORM: 'status-platform',
    COMPLETED: 'status-completed'
  }
  return map[status] || ''
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString()
}

const handleFilter = () => {}

// ======================
// 🔥 真实接口：加载商家售后列表
// ======================
const loadAfterSales = async () => {
  try {
    const res = await fetch('/api/after/sale/merchant/list', {
      headers: {
        'Authorization': 'Bearer ' + userStore.token
      }
    })
    const data = await res.json()
    if (data.code === 200) {
      afterSales.value = data.data?.records || [] // 🔥 取 records 数组
    }
  } catch (err) {
    showToast('加载失败')
  }
}

// ======================
// 🔥 商家审核（同意/拒绝）
// ======================
const showHandleDialog = ref(false)
const handleAction = ref('')
const handleForm = ref({ id: null, remark: '' })

const handleApprove = (item) => {
  handleAction.value = 'APPROVED'
  handleForm.value = { id: item.id, remark: '' }
  showHandleDialog.value = true
}

const handleRefuse = (item) => {
  handleAction.value = 'REJECTED'
  handleForm.value = { id: item.id, remark: '' }
  showHandleDialog.value = true
}

const submitHandle = async () => {
  if (!handleForm.value.remark) {
    showToast('请输入处理备注')
    return
  }

  try {
    const res = await fetch(`/api/after/sale/merchant/audit/${handleForm.value.id}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': 'Bearer ' + userStore.token
      },
      body: `status=${handleAction.value}&remark=${handleForm.value.remark}`
    })

    const data = await res.json()
    if (data.code === 200) {
      showToast('处理成功')
      showHandleDialog.value = false
      loadAfterSales()
    } else {
      showToast(data.msg || '处理失败')
    }
  } catch (e) {
    showToast('处理失败')
  }
}

// 🔥 新增：商家确认收到退货
const handleMerchantConfirmReceive = async (item) => {
  try {
    await showConfirmDialog('确认已收到用户退回的商品？确认后将完成退款！')
    const res = await fetch(`/api/after/sale/merchant/confirm/${item.id}`, {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ' + userStore.token
      }
    })
    const data = await res.json()
    if (data.code === 200) {
      showToast('确认收货成功，已退款')
      loadAfterSales()
    } else {
      showToast(data.msg || '操作失败')
    }
  } catch {
    showToast('已取消')
  }
}

onMounted(() => {
  loadAfterSales()
})
</script>

<style scoped>
.aftersale-page {
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
  padding: 10px;
  margin-bottom: 15px;
}

.aftersale-card {
  background: #fff;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 10px;
}
.aftersale-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.aftersale-id {
  font-size: 14px;
  color: #333;
}
.aftersale-status {
  font-size: 13px;
  padding: 2px 8px;
  border-radius: 4px;
}
.status-pending { color: #ff976a; background: #fff7e6; }
.status-approved { color: #1989fa; background: #e8f3ff; }
.status-rejected { color: #ff4444; background: #ffe6e6; }
.status-platform { color: #722ed1; background: #f3e8ff; }
.status-completed { color: #07c160; background: #e6f7e6; }

.aftersale-info {
  font-size: 13px;
  color: #666;
  margin-bottom: 15px;
}
.info-item {
  margin-bottom: 6px;
}
.info-item.reason {
  margin-top: 4px;
}

.aftersale-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 16px;
}
.dialog-header .title {
  font-weight: bold;
}
.dialog-header .confirm {
  color: #ff9f00;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}
</style>