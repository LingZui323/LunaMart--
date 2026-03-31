<template>
  <div class="admin-page">
    <van-nav-bar
      title="商家管理"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <div class="container" style="margin-top: 50px; padding-bottom: 80px">
      <!-- 状态筛选 -->
      <van-tabs v-model:active="activeStatus" class="status-tabs" @change="loadMerchantList">
        <van-tab title="全部" name="" />
        <van-tab title="待审核" name="PENDING" />
        <van-tab title="已通过" name="APPROVED" />
        <van-tab title="已拒绝" name="REJECTED" />
        <van-tab title="已冻结" name="FROZEN" />
      </van-tabs>

      <!-- 商家列表 -->
      <van-cell-group inset class="list-card">
        <van-cell
          v-for="merchant in merchantList"
          :key="merchant.id"
          is-link
          @click="openMerchantDetail(merchant)"
        >
          <template #title>
            <div class="merchant-row">
              <div>
                <div class="merchant-name">{{ merchant.shopName }}</div>
                <div class="merchant-info-text">
                  商家ID：{{ merchant.id }}｜用户ID：{{ merchant.userId }}
                </div>
              </div>
              <van-tag 
                :type="statusTagMap[String(merchant.status)]?.type || 'default'" 
                size="small"
              >
                {{ statusTagMap[String(merchant.status)]?.text || merchant.status }}
              </van-tag>
            </div>
          </template>
        </van-cell>

        <div v-if="merchantList.length === 0" class="empty">
          <van-empty description="暂无商家数据" />
        </div>
      </van-cell-group>
    </div>

    <!-- 商家详情 & 审核弹窗 -->
    <van-popup v-model:show="showDetailPopup" position="bottom" :style="{ height: '85%', borderRadius: '16px 16px 0 0' }">
      <div class="detail-popup">
        <div class="popup-header">
          <div class="popup-title">商家申请详情</div>
          <van-icon name="cross" @click="showDetailPopup = false" />
        </div>

        <van-cell-group inset class="info-group">
          <van-cell title="商家ID" :value="currentMerchant.id" />
          <van-cell title="店铺名称" :value="currentMerchant.shopName" />
          <van-cell title="申请人ID" :value="currentMerchant.userId" />
          <van-cell title="营业执照" :value="currentMerchant.businessLicense" />
          <van-cell title="申请说明" :value="currentMerchant.applyRemark" />
          <van-cell title="申请状态">
            <template #extra>
              <van-tag 
              :type="statusTagMap[String(currentMerchant.status)]?.type || 'default'"
            >
              {{ statusTagMap[String(currentMerchant.status)]?.text || currentMerchant.status }}
            </van-tag>
            </template>
          </van-cell>
          <van-cell title="申请时间" :value="formatTime(currentMerchant.createTime)" />
        </van-cell-group>

        <!-- 审核按钮：只有待审核才显示 -->
        <div v-if="currentMerchant.status === 'PENDING'" class="audit-actions">
          <van-button type="danger" plain block @click="handleAuditMerchant('REJECTED')">
            拒绝申请
          </van-button>
          <van-button type="primary" color="#ff9f00" block @click="handleAuditMerchant('APPROVED')">
            同意入驻
          </van-button>
        </div>

        <!-- 冻结按钮：只有已通过才显示 -->
        <div v-if="currentMerchant.status === 'APPROVED'" class="freeze-action">
          <van-button type="danger" block @click="handleFreezeMerchant">
            冻结该商家
          </van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()

// 筛选状态
const activeStatus = ref('')
const merchantList = ref([])
const showDetailPopup = ref(false)
const currentMerchant = ref({})

// 状态映射（完全匹配后端）
const statusTagMap = {
  PENDING: { type: 'warning', text: '待审核' },
  APPROVED: { type: 'success', text: '已通过' },
  REJECTED: { type: 'danger', text: '已拒绝' },
  FROZEN: { type: 'primary', text: '已冻结' },
  // 兜底：处理数字/未知状态
  '0': { type: 'warning', text: '待审核' },
  '1': { type: 'success', text: '已通过' },
  '2': { type: 'danger', text: '已拒绝' },
  '3': { type: 'primary', text: '已冻结' }
}

// ======================================
// 加载商家列表
// ======================================
const loadMerchantList = async () => {
  try {
    const res = await fetch(`/api/merchant/list?status=${activeStatus.value}`, {
      headers: {
        Authorization: 'Bearer ' + userStore.token
      }
    })
    const data = await res.json()
    if (data.code === 200) {
      merchantList.value = data.data || []
    }
  } catch (err) {
    showToast('加载失败')
  }
}

// ======================================
// 打开详情（修复：深拷贝 + 强制转字符串）
// ======================================
const openMerchantDetail = (merchant) => {
  // 1. 深拷贝：防止原对象响应式污染
  const copy = { ...merchant }
  
  // 2. 强制将 status 转为字符串
  // 解决后端返回数字/其他类型导致的报错
  copy.status = String(copy.status)
  
  currentMerchant.value = copy
  showDetailPopup.value = true
}

// ======================================
// 审核商家（通过/拒绝）
// ======================================
const handleAuditMerchant = async (status) => {
  const id = currentMerchant.value.id
  const text = status === 'APPROVED' ? '通过' : '拒绝'

  try {
    await showConfirmDialog(`确定要${text}该商家申请吗？`)

    const res = await fetch(`/api/merchant/audit/${id}?status=${status}`, {
      method: 'PUT',
      headers: {
        Authorization: 'Bearer ' + userStore.token
      }
    })

    const data = await res.json()
    if (data.code === 200) {
      showToast(`${text}成功`)
      showDetailPopup.value = false
      loadMerchantList()
    } else {
      showToast(data.msg || '操作失败')
    }
  } catch (e) {
    // 点击取消会触发 reject，这里不需要报错
  }
}

// ======================================
// 冻结商家
// ======================================
const handleFreezeMerchant = async () => {
  const merchantId = currentMerchant.value.id
  try {
    await showConfirmDialog('确定要冻结该商家吗？')
    
    const res = await fetch(`/api/merchant/freeze/${merchantId}`, {
      method: 'PUT',
      headers: {
        Authorization: 'Bearer ' + userStore.token
      }
    })

    const data = await res.json()
    if (data.code === 200) {
      showToast('冻结成功')
      showDetailPopup.value = false
      loadMerchantList()
    } else {
      showToast(data.msg || '冻结失败')
    }
  } catch (e) {
    // 点击取消会触发 reject，这里不需要报错
  }
}

// 时间格式化
const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

onMounted(() => {
  loadMerchantList()
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
  padding: 10px;
}
.status-tabs {
  background: #fff;
  margin-bottom: 10px;
}
.list-card {
  background: #fff;
  border-radius: 12px;
}
.merchant-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.merchant-name {
  font-weight: bold;
  font-size: 15px;
}
.merchant-info-text {
  font-size: 12px;
  color: #999;
}
.empty {
  padding: 50px 0;
  text-align: center;
}

/* 弹窗 */
.detail-popup {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
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
.info-group {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 20px;
}
.audit-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}
.audit-actions .van-button, .freeze-action .van-button {
  border-radius: 8px;
}
</style>