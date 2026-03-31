<template>
  <div class="order-confirm-page">
    <van-nav-bar
      title="确认订单"
      fixed
      left-arrow
      @click-left="handleBack"
      class="moon-nav-bar"
    />

    <div class="container" style="margin-top: 46px; padding-bottom: 100px">
      <div class="address-card" @click="goToAddress">
        <van-cell
          :title="defaultAddress?.receiver || '请选择收货地址'"
          :value="defaultAddress?.phone || ''"
          :label="
            defaultAddress
              ? `${defaultAddress.province}${defaultAddress.city}${defaultAddress.district} ${defaultAddress.detailAddress}`
              : '点击添加地址'
          "
          is-link
        />
      </div>

      <div
        class="goods-group"
        v-for="group in merchantGroups"
        :key="group.merchantId"
      >
        <div class="merchant-bar">
          <van-icon name="shop-o" color="#ff9f00" />
          <span>{{ group.merchantName }}</span>
        </div>

        <van-cell-group inset>
          <van-cell
            v-for="item in group.items"
            :key="item.id"
            class="goods-item"
          >
            <template #default>
              <div class="goods-info">
                <img :src="item.goods?.imageList?.[0]?.imageUrl || '/default-goods.png'" 
                     class="goods-img" 
                />
                <div class="goods-text">
                  <div class="goods-name">{{ item.goods.title }}</div>
                  <div class="goods-spec">
                    规格：{{ item.goods.specs || '默认规格' }}
                  </div>
                </div>
              </div>
            </template>
            <template #right-icon>
              <div class="right-info">
                <div class="price">¥{{ item.goods.price }}</div>
                <div class="num">x{{ item.quantity }}</div>
              </div>
            </template>
          </van-cell>
        </van-cell-group>
      </div>

      <van-cell-group inset class="price-group">
        <van-cell title="商品总价" value-class="price" :value="`¥${totalPrice}`" />
        <van-cell title="运费" value-class="price" value="¥0.00" />
        <van-cell title="实付金额" value-class="total-price" :value="`¥${totalPrice}`" />
      </van-cell-group>

      <van-field
        v-model="remark"
        placeholder="备注（选填）"
        maxlength="100"
        class="remark"
      />
    </div>

    <van-submit-bar
      :price="totalPrice * 100"
      button-text="提交订单"
      @submit="submitOrder"
      class="submit-bar"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { useUserStore } from '../../store/user'
import { nextTick } from 'vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const cartIds = route.query.cartIds?.split(',') || []
const goodsId = route.query.goodsId
const count = route.query.count ? Number(route.query.count) : 1

const cartList = ref([])
const defaultAddress = ref(null)
const remark = ref('')

const loadDefaultAddress = async () => {
  if (!userStore.isLogin) return
  try {
    const res = await fetch(`/api/address/list`, {
      headers: { Authorization: `Bearer ${userStore.token}` }
    })
    const data = await res.json()
    if (data.code === 200 && data.data?.length > 0) {
      const def = data.data.find(item => item.isDefault === 1)
      defaultAddress.value = def || data.data[0]
    }
  } catch (e) {}
}

const loadAddressById = async (addressId) => {
  if (!addressId || !userStore.isLogin) return
  try {
    const res = await fetch(`/api/address/list`, {
      headers: { Authorization: `Bearer ${userStore.token}` }
    })
    const data = await res.json()
    if (data.code === 200 && data.data) {
      const addr = data.data.find(item => item.id == addressId)
      if (addr) {
        defaultAddress.value = null
        await nextTick()
        defaultAddress.value = addr
      }
    }
  } catch (e) {}
}

const loadGoodsForBuyNow = async () => {
  const res = await fetch(`/api/goods/${goodsId}`, {
    headers: { Authorization: `Bearer ${userStore.token}` }
  })
  const data = await res.json()
  const goods = data.data
  cartList.value = [{
    id: 'temp',
    goodsId: goods.id,
    quantity: count,
    goods: goods
  }]
}

const loadSelectedCart = async () => {
  if (goodsId) {
    await loadGoodsForBuyNow()
    return
  }

  const res = await fetch(`/api/cart/my`, {
    headers: { Authorization: `Bearer ${userStore.token}` }
  })
  const data = await res.json()
  let carts = data.data || []
  carts = carts.filter(i => i.selected === 1)

  for (let c of carts) {
    const gRes = await fetch(`/api/goods/${c.goodsId}`, {
      headers: { Authorization: `Bearer ${userStore.token}` }
    })
    const resData = await gRes.json()
    c.goods = resData.data
  }
  cartList.value = carts
}

const merchantGroups = computed(() => {
  const groups = {}
  cartList.value.forEach(item => {
    const mid = item.goods?.merchantId || 0
    if (!groups[mid]) {
      groups[mid] = {
        merchantId: mid,
        merchantName: item.goods?.merchant?.shopName || '未知商家',
        items: []
      }
    }
    groups[mid].items.push(item)
  })
  return Object.values(groups)
})

const totalPrice = computed(() => {
  return cartList.value.reduce(
    (sum, item) => sum + (item.goods?.price || 0) * item.quantity,
    0
  )
})

// ==============================================
// 去地址页（正常 push，不嵌套）
// ==============================================
const goToAddress = () => {
  router.push({
    path: '/address',
    query: {
      select: 'true',
      goodsId: goodsId || '',
      count: count || '',
      cartIds: cartIds.join(',') || ''
    }
  })
}

// ==============================================
// 监听地址变化
// ==============================================
watch(
  () => route.query.addressId,
  async (newId) => {
    if (newId) {
      await loadAddressById(newId)
    }
  },
  { immediate: false }
)

// ==============================================
// ✅ 返回逻辑：100% 安全，不报错
// ==============================================
const handleBack = () => {
  router.back()
}

// ==============================================
// ✅ 提交订单：完全适配你的后端（修复 400 核心）
// ==============================================
const submitOrder = async () => {
  if (!defaultAddress.value) {
    showToast('请选择收货地址')
    return
  }

  try {
    await showConfirmDialog('确认提交订单？')
    let res, result
    const addressId = defaultAddress.value.id

    if (goodsId) {
      // 立即购买 → form 表单格式（适配 @RequestParam）
      res = await fetch(`/api/order/create/buyNow?goodsId=${goodsId}&buyNum=${count}&addressId=${addressId}`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${userStore.token}`
        }
      })
    } else {
      // 购物车结算
      res = await fetch(`/api/order/create?addressId=${addressId}`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${userStore.token}`
        }
      })
    }

    result = await res.json()
    if (result.code !== 200) {
      showToast(result.msg || '创建失败')
      return
    }
    router.push(`/order/detail/${result.data.id}`)
  } catch (e) {
    showToast('提交失败')
  }
}

onMounted(async () => {
  await loadSelectedCart()
  if (route.query.addressId) {
    await loadAddressById(route.query.addressId)
  } else {
    await loadDefaultAddress()
  }
})
</script>

<style scoped>
.order-confirm-page {
  background: #fff9e6;
  min-height: 100vh;
  padding-bottom: 100px;
}
.moon-nav-bar {
  background: #ffe181;
}
.container {
  padding: 10px;
}

.address-card {
  margin-bottom: 10px;
}

.merchant-bar {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  background: #fff;
  font-size: 14px;
  margin-bottom: 2px;
}
.merchant-bar span {
  margin-left: 6px;
}

.goods-item {
  padding: 12px 15px;
}
.goods-info {
  display: flex;
  align-items: center;
}
.goods-img {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  margin-right: 10px;
  object-fit: cover;
}
.goods-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}
.goods-spec {
  font-size: 12px;
  color: #999;
}
.right-info {
  text-align: right;
}
.price {
  color: #ff4444;
  font-size: 15px;
  font-weight: bold;
}
.num {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.price-group {
  margin-top: 10px;
}
.total-price {
  color: #ff4444 !important;
  font-weight: bold;
}

.remark {
  margin-top: 10px;
  background: #fff;
}

.submit-bar {
  --van-submit-bar-price-color: #ff4444;
  position: fixed;
  bottom: 50px;
  left: 0;
  right: 0;
  z-index: 99;
}
</style>