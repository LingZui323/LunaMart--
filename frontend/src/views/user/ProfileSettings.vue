<template>
  <div class="profile-settings-page">
    <van-nav-bar
      title="账号设置"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <div class="container" style="margin-top: 46px; padding-bottom: 80px">
      <van-cell-group inset class="info-group">
        <van-cell title="用户名" :value="userStore.userInfo?.username" is-link @click="openEditField('username')" />
        <van-cell title="邮箱" :value="userStore.userInfo?.email" is-link @click="openEditField('email')" />
        <van-cell title="当前权限" :value="roleText" />
      </van-cell-group>

      <!-- 普通用户：显示申请入口 或 申请进度 -->
      <template v-if="userStore.userInfo?.role === 'USER'">
        <!-- 已有申请记录：显示进度 -->
        <van-cell-group inset class="apply-group" v-if="merchantInfo">
          <van-cell title="申请状态" :value="statusText" />
          <van-cell title="店铺名称" :value="merchantInfo.shopName" />
          <van-cell title="申请时间" :value="formatTime(merchantInfo.createTime)" />
          <van-cell
            v-if="merchantInfo.status === 'REJECTED'"
            title="修改资料重新申请"
            value="点击修改"
            is-link
            @click="openUpdateDialog"
          />
        </van-cell-group>
        <!-- 无申请记录：显示申请按钮 -->
        <van-cell-group inset class="apply-group" v-else>
          <van-cell
            title="申请成为商户"
            value="点击申请"
            is-link
            @click="showApplyDialog = true"
          />
        </van-cell-group>
      </template>

      <!-- 商户/管理员：显示对应状态 -->
      <van-cell-group inset class="apply-group" v-else-if="userStore.userInfo?.role === 'MERCHANT'">
       <van-cell title="商户状态" :value="merchantInfo?.status === 'FROZEN' ? '已冻结' : merchantInfo?.status === 'PENDING' ? '资料修改待审核' : '已认证商户'" /><van-cell title="店铺名称" :value="merchantInfo?.shopName" />
        <van-cell
          v-if="merchantInfo?.status !== 'FROZEN'"
          title="修改店铺资料"
          value="点击修改"
          is-link
          @click="openUpdateDialog"
        />
      </van-cell-group>

      <van-cell-group inset class="apply-group" v-else-if="userStore.userInfo?.role === 'ADMIN'">
        <van-cell title="管理员状态" value="系统管理员" />
      </van-cell-group>
    </div>

    <!-- 修改信息弹窗 -->
    <van-popup v-model:show="showEditDialog" position="bottom" :style="{ height: '40%', padding: '20px', borderRadius: '12px 12px 0 0' }">
      <div class="dialog-header">
        <span @click="showEditDialog = false">取消</span>
        <span class="title">修改{{ editLabel }}</span>
        <span @click="handleEditConfirm" class="confirm">保存</span>
      </div>
      <van-field v-model="editValue" :placeholder="`请输入新的${editLabel}`" />
    </van-popup>

    <!-- 首次申请商户弹窗 -->
    <van-popup v-model:show="showApplyDialog" position="bottom" :style="{ height: '50%', padding: '20px', borderRadius: '12px 12px 0 0' }">
      <div class="dialog-header">
        <span @click="showApplyDialog = false">取消</span>
        <span class="title">申请成为商户</span>
        <span @click="handleApplyMerchant" class="confirm">提交</span>
      </div>
      <van-form>
        <van-field
          v-model="applyForm.shopName"
          placeholder="请输入店铺名称"
          label="店铺名称"
          :rules="[{ required: true, message: '店铺名称不能为空' }]"
        />
        <van-field
          v-model="applyForm.businessLicense"
          placeholder="请输入营业执照编号"
          label="营业执照"
          :rules="[{ required: true, message: '营业执照不能为空' }]"
        />
        <van-field
          v-model="applyForm.remark"
          placeholder="请输入申请说明（选填）"
          label="申请说明"
          type="textarea"
          rows="3"
        />
      </van-form>
    </van-popup>

    <!-- 修改商户资料弹窗 -->
    <van-popup v-model:show="showUpdateDialog" position="bottom" :style="{ height: '50%', padding: '20px', borderRadius: '12px 12px 0 0' }">
      <div class="dialog-header">
        <span @click="showUpdateDialog = false">取消</span>
        <span class="title">修改店铺资料</span>
        <span @click="handleUpdateMerchant" class="confirm">提交</span>
      </div>
      <van-form>
        <van-field
          v-model="updateForm.shopName"
          placeholder="请输入店铺名称"
          label="店铺名称"
          :rules="[{ required: true, message: '店铺名称不能为空' }]"
        />
        <van-field
          v-model="updateForm.businessLicense"
          placeholder="请输入营业执照编号"
          label="营业执照"
          :rules="[{ required: true, message: '营业执照不能为空' }]"
        />
        <van-field
          v-model="updateForm.applyRemark"
          placeholder="请输入申请说明（选填）"
          label="申请说明"
          type="textarea"
          rows="3"
        />
      </van-form>
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

// 权限文本
const roleText = computed(() => {
  const roleMap = {
    USER: '普通用户',
    MERCHANT: '商户',
    ADMIN: '管理员'
  }
  return roleMap[userStore.userInfo?.role] || '未知'
})

// 商家信息（来自 /merchant/my）
const merchantInfo = ref(null)

// 状态文本映射
const statusText = computed(() => {
  const statusMap = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    FROZEN: '已冻结'
  }
  return statusMap[merchantInfo.value?.status] || '未知状态'
})

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString()
}

// 加载我的商家信息
const loadMyMerchant = async () => {
  if (!userStore.isLogin) return
  try {
    const res = await fetch('/api/merchant/my', {
      headers: {
        'Authorization': 'Bearer ' + userStore.token
      }
    })
    const data = await res.json()
    if (data.code === 200) {
      merchantInfo.value = data.data
    }
  } catch (err) {
    merchantInfo.value = null
  }
}

// ==================== 原有编辑功能 ====================
const showEditDialog = ref(false)
const editFieldRef = ref('')
const editLabel = ref('')
const editValue = ref('')

const openEditField = (field) => {
  editFieldRef.value = field
  editLabel.value = field === 'username' ? '用户名' : '邮箱'
  editValue.value = userStore.userInfo[field]
  showEditDialog.value = true
}

const handleEditConfirm = async () => {
  if (!editValue.value) {
    showToast(`请输入新的${editLabel.value}`)
    return
  }
  try {
    userStore.userInfo[editFieldRef.value] = editValue.value
    showToast('修改成功')
    showEditDialog.value = false
  } catch (err) {
    showToast('修改失败')
  }
}

// ==================== 首次申请商户 ====================
const showApplyDialog = ref(false)
const applyForm = ref({
  shopName: '',
  businessLicense: '',
  applyRemark: ''
})

const handleApplyMerchant = async () => {
  if (!applyForm.value.shopName) {
    showToast('请输入店铺名称')
    return
  }
  if (!applyForm.value.businessLicense) {
    showToast('请输入营业执照编号')
    return
  }
  try {
    await showConfirmDialog('确认提交商户申请？')
    const res = await fetch('/api/merchant/apply', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + userStore.token
      },
      body: JSON.stringify(applyForm.value)
    })
    const data = await res.json()
    if (data.code === 200) {
      showToast('申请已提交，请等待管理员审核')
      showApplyDialog.value = false
      applyForm.value = { shopName: '', businessLicense: '', remark: '' }
      loadMyMerchant() // 刷新商家信息
    } else {
      showToast(data.msg || '申请提交失败')
    }
  } catch (err) {
    showToast('申请提交失败，请稍后重试')
  }
}

// ==================== 修改商户资料 ====================
const showUpdateDialog = ref(false)
const updateForm = ref({
  shopName: '',
  businessLicense: '',
  applyRemark: ''
})

// 打开修改弹窗时，回填当前数据
const openUpdateDialog = () => {
  updateForm.value = {
    shopName: merchantInfo.value?.shopName || '',
    businessLicense: merchantInfo.value?.businessLicense || '',
    applyRemark: merchantInfo.value?.applyRemark || ''
  }
  showUpdateDialog.value = true
}

const handleUpdateMerchant = async () => {
  if (!updateForm.value.shopName) {
    showToast('请输入店铺名称')
    return
  }
  if (!updateForm.value.businessLicense) {
    showToast('请输入营业执照编号')
    return
  }
  try {
    await showConfirmDialog('确认修改店铺资料？修改后将重新进入审核流程')
    const res = await fetch('/api/merchant/update', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + userStore.token
      },
      body: JSON.stringify(updateForm.value)
    })
    const data = await res.json()
    if (data.code === 200) {
      showToast('修改成功，等待管理员重新审核')
      showUpdateDialog.value = false

      // 👇 👇 关键：强制进入待审核状态，前端立刻显示
      if (merchantInfo.value) {
        merchantInfo.value.status = "PENDING"
        // 保留旧店铺名，不显示新修改的名字（模拟审核中）
        merchantInfo.value.shopName = merchantInfo.value.shopName
      }

      // 重新拉取后端最新数据（确保和数据库一致）
      setTimeout(() => {
        loadMyMerchant()
      }, 500)

    } else {
      showToast(data.msg || '修改失败')
    }
  } catch (err) {
    showToast('修改失败，请稍后重试')
  }
}

// 页面加载时获取商家信息
onMounted(() => {
  loadMyMerchant()
})
</script>

<style scoped>
.profile-settings-page {
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

.info-group, .apply-group {
  margin-bottom: 15px;
  border-radius: 12px;
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
</style>