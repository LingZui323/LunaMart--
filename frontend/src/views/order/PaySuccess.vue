<template>
  <div class="pay-success-page">
    <div class="success-box">
      <van-icon name="success" size="60" color="#07c160" />
      <div class="title">支付成功</div>
      <div class="order-info">订单号：{{ orderNo }}</div>
      <div class="price">实付金额：<span>¥{{ totalAmount }}</span></div>
    </div>

    <div class="btns">
      <van-button plain @click="toOrder">查看订单</van-button>
      <van-button type="primary" color="#ff9f00" @click="toHome">返回首页</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const orderId = route.query.orderId || 0
const orderNo = ref('ORD202503270001')
const totalAmount = ref(2499)

onMounted(() => {
  // 可在这里加载订单支付结果
})

const toOrder = () => {
  router.push(`/order/detail/${orderId}`)
}
const toHome = () => {
  router.push('/')
}
</script>

<style scoped>
.pay-success-page {
  background: #fff9e6;
  min-height: 100vh;
  padding: 60px 20px;
  text-align: center;
}
.success-box {
  background: #fff;
  border-radius: 16px;
  padding: 40px 20px;
  margin-bottom: 30px;
}
.title {
  font-size: 20px;
  font-weight: bold;
  margin: 10px 0;
}
.order-info {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}
.price {
  font-size: 16px;
  color: #ff4444;
  font-weight: bold;
}
.btns {
  display: flex;
  gap: 15px;
  justify-content: center;
}
</style>