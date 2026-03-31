<template>
  <div class="apply-refund-page">
    <van-nav-bar
      title="申请售后"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <div class="container" style="margin-top: 46px; padding: 16px;">
      <!-- 订单信息展示 -->
      <van-cell-group inset class="order-info-group">
        <van-cell title="订单号" :value="orderInfo.orderNo" />
        <van-cell title="商品名称" :value="orderInfo.goodsName" />
        <van-cell title="支付金额" :value="`¥ ${orderInfo.payAmount}`" />
      </van-cell-group>

      <!-- 售后申请表单 -->
      <van-form @submit="handleApplyAfterSale" class="after-sale-form">
        <van-cell-group inset class="form-group">
          <van-cell
            readonly
            clickable
            title="售后类型"
            :value="typeText || '请选择售后类型'"
            @click="showTypePicker = true"
          />
          <van-field
            v-model="afterSaleForm.reason"
            label="退款原因"
            type="textarea"
            rows="3"
            placeholder="请描述退款原因（必填）"
            :rules="[{ required: true, message: '退款原因不能为空' }]"
          />
        </van-cell-group>

        <div style="margin: 20px 0;">
          <van-button
            type="primary"
            color="#FFE181"
            block
            native-type="submit"
            :loading="submitting"
            class="submit-btn"
          >
            提交售后申请
          </van-button>
        </div>
      </van-form>
    </div>

    <!-- 售后类型选择器 -->
    <van-action-sheet
      v-model:show="showTypePicker"
      :actions="typeOptions"
      @select="onTypeSelect"  
      @cancel="showTypePicker = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { showToast, showConfirmDialog } from 'vant'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const orderId = route.query.orderId

// 订单信息
const orderInfo = ref({
  orderId: orderId,
  orderNo: '',
  goodsName: '',
  payAmount: 0
})

// 页面显示文本
const typeText = ref('')

// 表单数据
const afterSaleForm = reactive({
  orderId: orderId,
  type: '',
  reason: ''
})

const submitting = ref(false)
const showTypePicker = ref(false)

// 选项定义
const typeOptions = [
  { name: '仅退款', value: 'REFUND' },
  { name: '退货退款', value: 'RETURN' }
]

// ✅ 修复：解构出 item，再取值
const onTypeSelect = (item, index) => {
  console.log('选中了:', item, index) // 看控制台是否输出
  afterSaleForm.type = item.value
  typeText.value = item.name
  showTypePicker.value = false // 手动关闭弹窗
}

// 加载订单详情
const loadOrderDetail = async () => {
  if (!orderId) return;
  try {
    const res = await fetch(`/api/order/detail/${orderId}`, {
      headers: { Authorization: 'Bearer ' + userStore.token }
    });
    const data = await res.json();
    if (data.code === 200) {
      const order = data.data;
      orderInfo.value.orderNo = order.orderNo || '';
      orderInfo.value.payAmount = order.totalAmount || 0;
      if (order.itemList?.length > 0) {
        orderInfo.value.goodsName = order.itemList.map(i => i.goodsTitle).join('、');
      }
    }
  } catch (err) {
    showToast('加载订单信息失败');
  }
};

// 提交售后申请
const handleApplyAfterSale = async () => {
  if (!afterSaleForm.type) {
    showToast('请选择售后类型');
    return;
  }
  if (!afterSaleForm.reason) {
    showToast('请填写退款原因');
    return;
  }
  try {
    await showConfirmDialog('确认提交售后申请？');
    submitting.value = true;
    const payload = {
      orderId: Number(orderId),
      type: afterSaleForm.type,
      reason: afterSaleForm.reason
    };
    const res = await fetch('/api/after/sale/apply', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + userStore.token
      },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.code === 200) {
      showToast('售后申请提交成功');
      router.push('/aftersale/list');
    } else {
      showToast(data.msg || '申请失败');
    }
  } catch (err) {
    showToast('申请失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  loadOrderDetail();
});
</script>

<style scoped>
.apply-refund-page {
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
.order-info-group,
.form-group {
  margin-bottom: 16px;
  border-radius: 12px;
}
.after-sale-form {
  margin-top: 16px;
}
.submit-btn {
  border-radius: 12px;
  color: #333;
  font-weight: 500;
}
</style>