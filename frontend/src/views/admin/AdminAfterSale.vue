<template>
  <div class="admin-aftersale-page">
    <van-nav-bar
      title="管理员售后审核"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="admin-nav-bar"
    />

    <div class="container" style="margin-top: 46px; padding-bottom: 30px;">
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <van-dropdown-menu active-color="#ff4444">
          <van-dropdown-item v-model="filterStatus" :options="statusOptions" @change="loadList" />
        </van-dropdown-menu>
      </div>

      <!-- 售后列表 -->
      <div
        v-for="item in afterSaleList"
        :key="item?.id || Math.random()"
        class="card"
      >
        <div class="card-header">
          <div>售后ID：{{ item?.id || '未知' }}</div>
          <div class="status" :class="statusClass(item?.status)">
            {{ statusMap[item?.status] || '未知状态' }}
          </div>
        </div>

        <div class="info-row">
          <span>订单编号：{{ item?.orderNo || '未知' }}</span>
        </div>
        <div class="info-row">
          <span>商品：{{ item?.goodsTitle || '未知' }}</span>
        </div>
        <div class="info-row">
          <span>金额：¥{{ item?.totalAmount || '0' }}</span>
        </div>
        <div class="info-row">
          <span>类型：{{ typeMap[item?.type] || '未知' }}</span>
        </div>
        <div class="info-row reason">
          <span>退款原因：{{ item?.reason || '无' }}</span>
        </div>

        <div class="info-row" v-if="item?.handleRemark">
          <span>处理备注：{{ item.handleRemark }}</span>
        </div>

        <!-- 🔥 管理员强制处理：仅 PLATFORM 可操作 -->
        <div
          class="action-row"
          v-if="item?.status === 'PLATFORM'"
        >
          <van-button
            type="primary"
            color="#ff4444"
            size="small"
            @click="openAudit(item)"
          >
            平台强制完成
          </van-button>
        </div>
      </div>

      <div v-if="afterSaleList.length === 0" class="empty">
        <van-empty description="暂无售后记录" />
      </div>
    </div>

    <!-- 审核弹窗 -->
    <van-popup v-model:show="showDialog" position="bottom" :style="{ height: '45%', padding: '20px', borderRadius: '12px 12px 0 0' }">
      <div class="dialog-header">
        <span @click="showDialog = false">取消</span>
        <span class="title">平台强制审核</span>
        <span @click="submitAudit" class="confirm">提交</span>
      </div>
      <van-field
        v-model="auditForm.remark"
        label="审核备注"
        type="textarea"
        rows="4"
        placeholder="请输入平台处理意见"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../store/user'
import { showToast } from 'vant'

const userStore = useUserStore()

// 筛选
const filterStatus = ref('')
const statusOptions = ref([
  { text: '全部', value: '' },
  { text: '待商家审核', value: 'PENDING' },
  { text: '已同意', value: 'APPROVED' },
  { text: '已拒绝', value: 'REJECTED' },
  { text: '平台介入中', value: 'PLATFORM' },
  { text: '已完成', value: 'COMPLETED' }
])

// 列表
const afterSaleList = ref([])

// 映射
const statusMap = {
  PENDING: '待商家审核',
  APPROVED: '商家同意',
  REJECTED: '商家拒绝',
  PLATFORM: '平台介入中',
  COMPLETED: '已完成'
}

const typeMap = {
  REFUND: '仅退款',
  RETURN: '退货退款'
}

const statusClass = (status) => {
  if (!status) return ''
  const cls = {
    PENDING: 'status-pending',
    APPROVED: 'status-approved',
    REJECTED: 'status-rejected',
    PLATFORM: 'status-platform',
    COMPLETED: 'status-completed'
  }
  return cls[status] || ''
}

// ======================
// 🔥 管理员：获取全部售后（过滤空数据）
// ======================
const loadList = async () => {
  try {
    const res = await fetch('/api/after/sale/admin/list', {
      headers: {
        'Authorization': 'Bearer ' + userStore.token
      }
    })
    const data = await res.json()
    if (data.code === 200) {
      let list = data.data || []
      // ✅ 过滤掉 null/undefined 项
      list = list.filter(item => item != null)
      if (filterStatus.value) {
        list = list.filter(item => item.status === filterStatus.value)
      }
      afterSaleList.value = list
    }
  } catch (e) {
    showToast('加载失败')
  }
}

// ======================
// 🔥 管理员：强制完成审核（PLATFORM → COMPLETED）
// ======================
const showDialog = ref(false)
const auditForm = ref({
  afterSaleId: null,
  remark: ''
})

const openAudit = (item) => {
  if (!item) return
  auditForm.value.afterSaleId = item.id
  auditForm.value.remark = ''
  showDialog.value = true
}

const submitAudit = async () => {
  if (!auditForm.value.remark) {
    showToast('请填写平台审核备注')
    return
  }

  try {
    const res = await fetch(`/api/after/sale/admin/audit/${auditForm.value.afterSaleId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': 'Bearer ' + userStore.token
      },
      body: `status=COMPLETED&remark=${auditForm.value.remark}`
    })

    const data = await res.json()
    if (data.code === 200) {
      showToast('平台处理完成')
      showDialog.value = false
      loadList()
    } else {
      showToast(data.msg || '处理失败，仅平台介入订单可操作')
    }
  } catch (e) {
    showToast('处理失败')
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.admin-aftersale-page {
  background: #fff9e6;
  min-height: 100vh;
}

.admin-nav-bar {
  background: #ff4444;
}
:deep(.van-nav-bar__title) {
  color: #fff;
}
:deep(.van-nav-bar__left) {
  color: #fff;
}

.container {
  padding: 10px;
}

.filter-bar {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 12px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
}

.status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.status-pending { color: #ff976a; background: #fff7e6; }
.status-approved { color: #1989fa; background: #e8f3ff; }
.status-rejected { color: #ff4444; background: #ffe6e6; }
.status-platform { color: #722ed1; background: #f3e8ff; }
.status-completed { color: #07c160; background: #e6f7e6; }

.info-row {
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}
.reason {
  color: #f56c6c;
}

.action-row {
  text-align: right;
  margin-top: 10px;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  font-size: 16px;
}
.dialog-header .title {
  font-weight: bold;
}
.confirm {
  color: #ff4444;
}

.empty {
  padding: 60px 0;
  text-align: center;
}
</style>