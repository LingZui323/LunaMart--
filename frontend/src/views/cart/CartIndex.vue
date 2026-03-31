<template>
  <div class="cart-page">
    <van-nav-bar
      title="购物车"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <div class="cart-list" style="margin-top: 50px; padding-bottom: 120px">
      <template v-if="cartList.length === 0">
        <van-empty description="购物车为空" />
      </template>

      <template v-for="merchant in merchantGroups" :key="merchant.merchantId">
        <div class="merchant-group">
          <div class="merchant-title">
            <van-icon name="shop-o" color="#ff9f00" />
            <span>{{ merchant.merchantName }}</span>
          </div>

          <van-cell-group inset>
            <van-cell
              v-for="item in merchant.items"
              :key="item.id"
              class="cart-item"
            >
              <template #icon>
                <van-checkbox
                  v-model="item.checked"
                  @change="handleSelectItem(item)"
                />
              </template>
              <template #title>
                <div class="goods-info">
                  <img 
                    :src="item.goods?.imageList?.[0]?.imageUrl || '/default-goods.png'" 
                    class="goods-img" 
                  />
                  <div class="goods-desc">
                    <div class="goods-name">{{ item.goods?.title || '商品' }}</div>
                    <div class="goods-price">¥{{ (item.goods?.price || 0).toFixed(2) }}</div>
                  </div>
                </div>
              </template>
              <template #right-icon>
                <div class="stepper-wrap">
                  <van-stepper
                    v-model="item.quantity"
                    min="1"
                    :max="item.goods?.stock || 1"
                    @change="handleQuantityChange(item)"
                  />
                  <van-button
                    size="mini"
                    type="danger"
                    plain
                    class="del-btn"
                    @click="handleDelete(item.id)"
                  >
                    删除
                  </van-button>
                </div>
              </template>
            </van-cell>
          </van-cell-group>
        </div>
      </template>
    </div>

    <div class="cart-bottom-bar" style="padding-bottom: 50px">
      <div class="bar-content">
        <van-checkbox v-model="allSelected" @change="handleAllSelected">全选</van-checkbox>
        <div class="price-info">
          <span>合计: ¥{{ totalPrice.toFixed(2) }}</span>
        </div>
        <van-button
          type="primary"
          color="#ff4444"
          class="submit-btn"
          @click="handleSubmitOrder"
        >
          去结算
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()
const cartList = ref([])

// 加载购物车 + 自动加载商品信息
const loadCartList = async () => {
  if (!userStore.isLogin) {
    cartList.value = []
    return
  }
  try {
    const res = await fetch('/api/cart/my', {
      headers: { Authorization: `Bearer ${userStore.token}` }
    })
    const data = await res.json()
    if (data.code === 200) {
      const carts = data.data
      // 为每个购物车项加载商品信息
      for (let cart of carts) {
        const goodsRes = await fetch(`/api/goods/${cart.goodsId}`, {
          headers: { Authorization: `Bearer ${userStore.token}` }
        })
        const goodsData = await goodsRes.json()
        cart.goods = goodsData.data || {}
        cart.checked = cart.selected === 1
      }
      cartList.value = carts
    }
  } catch (err) {
    showToast('加载购物车失败')
  }
}

// 按商家分组
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

// 全选
const allSelected = computed({
  get: () => cartList.value.length > 0 && cartList.value.every(i => i.checked),
  set: (val) => {
    cartList.value.forEach(i => i.checked = val)
    handleAllSelected(val)
  }
})

// 总价计算
const totalPrice = computed(() => {
  return cartList.value
    .filter(i => i.checked)
    .reduce((sum, i) => sum + (i.goods?.price || 0) * i.quantity, 0)
})

// 修改数量
const handleQuantityChange = async (item) => {
  try {
    await fetch(`/api/cart/quantity/${item.id}?quantity=${item.quantity}`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${userStore.token}` }
    })
  } catch (e) {
    showToast('修改失败')
    loadCartList()
  }
}

// 单选
const handleSelectItem = async (item) => {
  const selected = item.checked ? 1 : 0
  await fetch(`/api/cart/selected/${item.id}?selected=${selected}`, {
    method: 'PUT',
    headers: { Authorization: `Bearer ${userStore.token}` }
  })
}

// 全选操作
const handleAllSelected = async (val) => {
  for (let item of cartList.value) {
    item.checked = val
    await fetch(`/api/cart/selected/${item.id}?selected=${val ? 1 : 0}`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${userStore.token}` }
    })
  }
}

// 删除
const handleDelete = async (cartId) => {
  try {
    await showConfirmDialog('确定删除？')
    const res = await fetch(`/api/cart/${cartId}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${userStore.token}` }
    })
    const data = await res.json()
    if (data.code === 200) {
      cartList.value = cartList.value.filter(i => i.id !== cartId)
      showToast('删除成功')
    }
  } catch (e) {
    showToast('已取消')
  }
}

// 结算
const handleSubmitOrder = () => {
  const selected = cartList.value.filter(i => i.checked)
  if (!selected.length) return showToast('请选择商品')
  const ids = selected.map(i => i.id).join(',')
  router.push(`/order/confirm?cartIds=${ids}`)
}

watch(() => userStore.userInfo?.id, () => loadCartList())
onMounted(() => loadCartList())
</script>

<style scoped>
.cart-page {
  background: #fff9e6;
  min-height: 100vh;
}
.moon-nav-bar {
  background: #FFE181;
}
.merchant-group {
  margin-bottom: 15px;
}
.merchant-title {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  background: #fff;
  font-size: 14px;
}
.goods-info {
  display: flex;
  align-items: center;
}
.goods-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  margin-right: 10px;
  object-fit: cover;
}
.goods-price {
  color: #ff4444;
  font-weight: bold;
}
.stepper-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}
.cart-bottom-bar {
  position: fixed;
  bottom: 50px;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid #eee;
  padding: 6px 15px;
}
.bar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.price-info {
  font-weight: bold;
  color: #ff4444;
}
</style>