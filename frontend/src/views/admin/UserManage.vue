<template>
  <div class="admin-page">
    <van-nav-bar
      title="用户管理"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <div class="container" style="margin-top: 50px; padding-bottom: 80px">
      <van-search
        v-model="searchKey"
        placeholder="搜索用户名/ID"
        show-action
        @search="handleSearch"
        class="search-bar"
      />

      <van-cell-group inset class="list-card">
        <van-cell
          v-for="user in userList"
          :key="user.id"
          is-link
          @click="openUserDetail(user)"
        >
          <template #title>
            <div class="user-row">
              <div>
                <div class="username">{{ user.username }}</div>
                <div class="user-info-text">ID:{{ user.id }} | {{ user.email || '未绑定邮箱' }}</div>
              </div>
              <van-tag :type="user.status === 1 ? 'success' : 'danger'" size="small">
                {{ user.status === 1 ? '正常' : '已冻结' }}
              </van-tag>
            </div>
          </template>
        </van-cell>

        <div v-if="userList.length === 0" class="empty">
          <van-empty description="暂无用户" />
        </div>
      </van-cell-group>
    </div>

    <van-popup v-model:show="showDetailPopup" position="bottom" :style="{ height: '90%', borderRadius: '16px 16px 0 0' }">
      <div class="detail-popup">
        <div class="popup-header">
          <div class="popup-title">用户详情</div>
          <van-icon name="cross" @click="showDetailPopup = false" />
        </div>

        <van-cell-group inset class="info-group">
          <van-cell title="用户ID" :value="currentUser.id" />
          <van-cell title="用户名" :value="currentUser.username" />
          <van-cell title="邮箱" :value="currentUser.email || '暂无'" />
          <van-cell title="角色">
            <template #extra>
              <van-tag :type="currentUser.role === 'ADMIN' || currentUser.role === 'SUPER_ADMIN' ? 'danger' : currentUser.role === 'MERCHANT' ? 'primary' : 'default'">
                {{ currentUser.role === 'SUPER_ADMIN' ? '超级管理员' : currentUser.role === 'ADMIN' ? '管理员' : currentUser.role === 'MERCHANT' ? '商家' : '普通用户' }}
              </van-tag>
            </template>
          </van-cell>
          <van-cell title="状态">
            <template #extra>
              <van-button
                size="mini"
                :type="currentUser.status === 1 ? 'danger' : 'success'"
                @click="toggleUserStatus"
              >
                {{ currentUser.status === 1 ? '冻结用户' : '解除冻结' }}
              </van-button>
            </template>
          </van-cell>
          <van-cell title="创建时间" :value="formatTime(currentUser.createTime)" />
        </van-cell-group>

        <div class="order-title">用户订单</div>
        <van-cell-group inset class="order-group">
          <van-empty description="暂未对接订单接口" />
        </van-cell-group>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const searchKey = ref('')
const userList = ref([])
const showDetailPopup = ref(false)
const currentUser = ref({})

// 订单状态
const orderStatusText = {
  PENDING_PAY: '待支付',
  PAID: '已支付',
  DELIVERED: '已发货',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  REFUNDING: '退款中',
  REFUNDED: '已退款'
}

// ======================================
// 加载所有用户（真实后端接口）
// ======================================
const loadUserList = async () => {
  try {
    const res = await fetch('/api/user/manage/list', {
      headers: {
        Authorization: 'Bearer ' + userStore.token
      }
    })
    const data = await res.json()
    if (data.code === 200) {
      userList.value = data.data || []
    } else {
      showToast(data.msg || '加载失败')
    }
  } catch (e) {
    showToast('加载失败')
  }
}

// ======================================
// 搜索
// ======================================
const handleSearch = () => {
  if (!searchKey.value) {
    loadUserList()
    return
  }
  userList.value = userList.value.filter(u =>
    u.username.includes(searchKey.value) ||
    u.id == searchKey.value
  )
}

// ======================================
// 打开详情
// ======================================
const openUserDetail = (user) => {
  currentUser.value = { ...user }
  showDetailPopup.value = true
}

// ======================================
// 冻结 / 解冻用户（真实接口）
// ======================================
const toggleUserStatus = async () => {
  const userId = currentUser.value.id
  const isNowNormal = currentUser.value.status === 1

  if (isNowNormal) {
    await showConfirmDialog('确定要冻结该用户吗？')
  } else {
    await showConfirmDialog('确定要解除冻结吗？')
  }

  try {
    const res = await fetch(`/api/user/manage/freeze/${userId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + userStore.token
      }
    })
    const data = await res.json()
    if (data.code !== 200) {
      showToast(data.msg || '操作失败')
      return
    }

    currentUser.value.status = 0
    const target = userList.value.find(u => u.id === userId)
    if (target) target.status = 0
    showToast('操作成功')
  } catch (e) {
    showToast('操作失败')
  }
}

// 时间格式化
const formatTime = (t) => t ? new Date(t).toLocaleString() : '无'

onMounted(() => {
  loadUserList()
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
.search-bar {
  margin-bottom: 12px;
}
.list-card {
  background: #fff;
  border-radius: 12px;
}
.user-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.username {
  font-weight: bold;
  font-size: 15px;
  margin-bottom: 4px;
}
.user-info-text {
  font-size: 12px;
  color: #999;
}
.empty {
  padding: 40px 0;
}
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
.info-group, .order-group {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 16px;
}
.order-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
}
</style>