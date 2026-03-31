<template>
  <div class="address-page">
    <van-nav-bar
      title="地址管理"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <div class="address-list" style="margin-top: 50px">
      <van-cell-group inset class="address-group">
        <van-cell
          v-for="item in addressList"
          :key="item.id"
          :title="item.receiver"
          :value="item.phone"
          :label="`${item.province} ${item.city} ${item.district} ${item.detailAddress}`"
          class="address-item"
          @click="handleSelectAddress(item)"
        >
          <template #right-icon>
            <div class="action-buttons">
              <!-- 修复1：默认地址开关只做展示，不直接v-model修改 -->
              <van-switch
                v-if="addressList.length > 1"
                :model-value="item.isDefault === 1"
                size="24"
                active-color="#FFE181"
                @change="(val) => handleSetDefault(item.id, val)"
                class="default-switch"
              />
              <span v-else class="default-tag">默认</span>

              <van-button
                size="small"
                type="primary"
                plain
                @click.stop="handleEdit(item)"
                class="edit-btn"
              >
                修改
              </van-button>

              <van-button
                size="small"
                type="danger"
                plain
                @click.stop="handleDelete(item.id)"
                class="del-btn"
              >
                删除
              </van-button>
            </div>
          </template>
        </van-cell>
      </van-cell-group>

      <van-button
        type="primary"
        color="#FFE181"
        block
        class="add-btn"
        @click="showDialog = true"
      >
        添加新地址
      </van-button>
    </div>

    <van-popup
      v-model:show="showDialog"
      position="bottom"
      :style="{ height: '85%', padding: '24px', borderRadius: '20px 20px 0 0' }"
      class="address-popup"
    >
      <div class="dialog-header">
        <span @click="resetForm()" class="cancel-btn">取消</span>
        <span class="title">{{ isEdit ? '编辑地址' : '新增地址' }}</span>
        <span @click="handleSubmit" class="confirm-btn">保存</span>
      </div>

      <van-form @submit="handleSubmit" class="address-form">
        <van-field
          v-model="form.receiver"
          label="收货人"
          placeholder="请输入收货人"
          :rules="[{ required: true, message: '必填' }]"
          class="form-field"
        />
        <van-field
          v-model="form.phone"
          label="手机号"
          placeholder="请输入手机号"
          :rules="[{ required: true, message: '必填' }]"
          class="form-field"
        />

        <van-field
          v-model="form.province"
          label="省份"
          placeholder="请输入省份"
          :rules="[{ required: true, message: '必填' }]"
          class="form-field"
        />
        <van-field
          v-model="form.city"
          label="城市"
          placeholder="请输入城市"
          :rules="[{ required: true, message: '必填' }]"
          class="form-field"
        />
        <van-field
          v-model="form.district"
          label="区县"
          placeholder="请输入区县"
          :rules="[{ required: true, message: '必填' }]"
          class="form-field"
        />
        <van-field
          v-model="form.detailAddress"
          label="详细地址"
          placeholder="门牌号、街道等"
          rows="3"
          type="textarea"
          :rules="[{ required: true, message: '必填' }]"
          class="form-field"
        />

        <div class="default-row">
          <span>设为默认地址</span>
          <van-switch 
            :model-value="form.isDefault === 1" 
            @change="val => form.isDefault = val ? 1 : 0"
            active-color="#FFE181" 
            size="26" 
          />
        </div>
      </van-form>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import { useUserStore } from '../../store/user'
import { useRoute, useRouter } from 'vue-router'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const addressList = ref([])
const showDialog = ref(false)
const isEdit = ref(false)

const form = ref({
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: 0
})

// 加载地址
const loadAddressList = async () => {
  if (!userStore.isLogin) {
    showToast('请先登录', { background: '#fff4e0', color: '#333' })
    return
  }
  try {
    const res = await fetch('/api/address/list', {
      headers: { 'Authorization': 'Bearer ' + userStore.token }
    })
    const data = await res.json()
    if (data.code === 200) {
      addressList.value = data.data
    }
  } catch (err) {
    showToast('加载失败', { background: '#fff4e0', color: '#333' })
  }
}

// 提交
const handleSubmit = async () => {
  try {
    if (!userStore.isLogin) return
    const res = await fetch('/api/address/' + (isEdit.value ? 'update' : 'add'), {
      method: isEdit.value ? 'PUT' : 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + userStore.token
      },
      body: JSON.stringify(form.value)
    })
    const data = await res.json()
    if (data.code === 200) {
      showToast(isEdit.value ? '修改成功 ✅' : '添加成功 ✅', {
        background: '#e6f7e6', color: '#2e7d32'
      })
      showDialog.value = false
      resetForm()
      loadAddressList()
    } else {
      showToast(data.msg || '操作失败', { background: '#fff1f0', color: '#d32f2f' })
    }
  } catch (e) {
    showToast('操作失败', { background: '#fff1f0', color: '#d32f2f' })
  }
}

// 编辑
const handleEdit = (item) => {
  isEdit.value = true
  form.value = { ...item }
  showDialog.value = true
}

// ======================
// 🔥 核心修复：选择地址时保留所有商品参数
// ======================
const handleSelectAddress = (item) => {
  const { from, orderId, goodsId, count, cartIds } = route.query

  // 1. 来自订单详情页 → 返回订单详情
  if (from === 'orderDetail' && orderId) {
    router.replace({
      path: `/order/detail/${orderId}`,
      query: { addressId: item.id }
    })
    return
  }

  // 2. 来自订单确认页 → 带回所有参数（关键修复）
  if (route.query.select === 'true') {
    router.replace({
      path: '/order/confirm',
      query: {
        addressId: item.id,
        goodsId: goodsId || '',
        count: count || '',
        cartIds: cartIds || ''
      }
    })
    return
  }

  // 3. 普通地址管理 → 返回上一页
  router.back()
}

// 设置默认
const handleSetDefault = async (id, val) => {
  if (!val) {
    showToast('至少保留一个默认地址', { background: '#fff4e0', color: '#333' })
    return
  }
  try {
    await showConfirmDialog({
      title: '设为默认地址',
      message: '确定将该地址设为默认吗？',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      confirmButtonColor: '#FF9500',
      cancelButtonColor: '#999'
    })
    const res = await fetch(`/api/address/default/${id}`, {
      method: 'PUT',
      headers: { 'Authorization': 'Bearer ' + userStore.token }
    })
    const data = await res.json()
    if (data.code === 200) {
      showToast('设置成功 ✅', { background: '#e6f7e6', color: '#2e7d32' })
      loadAddressList()
    }
  } catch {
    showToast('已取消', { background: '#f5f5f5', color: '#666' })
  }
}

// 删除
const handleDelete = async (id) => {
  try {
    await showConfirmDialog({
      title: '删除地址',
      message: '确定要删除该地址吗？删除后无法恢复',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      confirmButtonColor: '#ff3b30',
      cancelButtonColor: '#999'
    })
    const res = await fetch(`/api/address/delete/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': 'Bearer ' + userStore.token }
    })
    const data = await res.json()
    if (data.code === 200) {
      addressList.value = addressList.value.filter(i => i.id !== id)
      showToast('删除成功 ✅', { background: '#e6f7e6', color: '#2e7d32' })
    }
  } catch {
    showToast('已取消', { background: '#f5f5f5', color: '#666' })
  }
}

const resetForm = () => {
  isEdit.value = false
  form.value = {
    receiver: '', phone: '', province: '', city: '', district: '',
    detailAddress: '', isDefault: 0
  }
  showDialog.value = false
}

onMounted(() => loadAddressList())
</script>

<style scoped>
.address-page {
  background: #fff9e6;
  min-height: 100vh;
}
.moon-nav-bar {
  background: #FFE181;
}
.address-list {
  padding: 10px;
}
.address-group {
  border-radius: 16px;
  overflow: hidden;
}
.address-item {
  padding: 14px 12px;
}
/* 修复2：调整电话号码位置，和收货人左对齐 */
.address-item :deep(.van-cell__value) {
  text-align: left;
  margin-top: 4px;
}
.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}
.default-switch {
  margin-right: 4px;
}
.default-tag {
  font-size: 12px;
  color: #ff9500;
  margin-right: 6px;
}
.edit-btn {
  font-size: 12px;
  border-radius: 8px;
  padding: 4px 8px;
  border-color: #FFC107;
  color: #ff9500;
}
.del-btn {
  font-size: 12px;
  border-radius: 8px;
  padding: 4px 8px;
}
.add-btn {
  margin: 20px;
  border-radius: 14px;
  color: #333;
  font-weight: 500;
}

.address-popup {
  background: #fff;
}
.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.cancel-btn {
  color: #999;
  font-size: 15px;
}
.title {
  font-size: 17px;
  font-weight: 500;
}
.confirm-btn {
  color: #ff9500;
  font-size: 15px;
  font-weight: 500;
}

.address-form {
  gap: 12px;
  display: flex;
  flex-direction: column;
}
.form-field {
  --van-field-label-width: 80px;
}
.default-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  font-size: 15px;
}
</style>