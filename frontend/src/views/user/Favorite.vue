<template>
  <div class="favorite-page">
    <van-nav-bar
      title="我的收藏"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <van-tabs v-model:active="activeTab" class="tab-bar">
      <van-tab title="商品" name="GOODS" />
      <van-tab title="商家" name="MERCHANT" />
    </van-tabs>

    <div class="list-wrapper">
      <div v-if="filteredList.length === 0" class="empty-tip">
        <van-empty description="暂无收藏" />
      </div>

      <!-- 商品：用 item.goods -->
      <template v-if="activeTab === 'GOODS'">
        <van-card
          v-for="item in filteredList"
          :key="item.id"
          :title="item.goods?.title || '商品名称'"
          :desc="item.goods?.status === 'ON_SALE' ? '商品在售' : '已下架'"
          :price="item.goods?.price || 0"
          :thumb="item.goods?.imageList?.[0]?.imageUrl || '/default-goods.png'"
          :class="{ 'card-disabled': item.goods?.status !== 'ON_SALE' }"
          @click="handleItemClick(item)"
        >
          <template #tags>
            <van-tag v-if="item.goods?.status !== 'ON_SALE'" type="danger" plain size="small">
              已下架
            </van-tag>
          </template>
          <template #right>
            <van-button size="mini" type="danger" plain @click.stop="handleDeleteFavorite(item)">
              取消收藏
            </van-button>
          </template>
        </van-card>
      </template>

      <!-- 商家：用 item.merchant -->
      <template v-if="activeTab === 'MERCHANT'">
        <van-card
          v-for="item in filteredList"
          :key="item.id"
          :title="item.merchant?.shopName || item.merchant?.name || '商家名称'"
          :desc="item.merchant?.status === 'NORMAL' ? '商家正常' : '已冻结'"
          :thumb="item.merchant?.logo || '/default-merchant.png'"
          :class="{ 'card-disabled': item.merchant?.status !== 'NORMAL' }"
          @click="handleItemClick(item)"
        >
          <template #tags>
            <van-tag v-if="item.merchant?.status !== 'NORMAL'" type="danger" plain size="small">
              已冻结
            </van-tag>
          </template>
          <template #right>
            <van-button size="mini" type="danger" plain @click.stop="handleDeleteFavorite(item)">
              取消收藏
            </van-button>
          </template>
        </van-card>
      </template>
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

const activeTab = ref('GOODS')
const favoriteList = ref([])

const loadFavorites = async () => {
  if (!userStore.isLogin) {
    showToast('请先登录')
    return
  }

  try {
    const res = await fetch(`/api/favorite/list?targetType=${activeTab.value}`, {
      headers: { Authorization: `Bearer ${userStore.token}` }
    })
    const data = await res.json()
    if (data.code === 200) {
      favoriteList.value = data.data || []
    }
  } catch (err) {
    showToast('加载收藏失败')
  }
}

const filteredList = computed(() => favoriteList.value)

// 点击跳转（已适配）
const handleItemClick = (item) => {
  if (item.targetType === 'GOODS') {
    if (!item.goods || item.goods.status !== 'ON_SALE') {
      showToast('商品已下架或不存在')
      return
    }
    router.push(`/goods/detail/${item.targetId}`)
  } else {
    if (!item.merchant || item.merchant.status !== 'NORMAL') {
      showToast('商家已冻结')
      return
    }
    router.push({ path: '/merchant', query: { merchantId: item.targetId } })
  }
}

// 取消收藏
const handleDeleteFavorite = async (item) => {
  try {
    await showConfirmDialog({ message: '确定取消收藏？' })

    const res = await fetch(`/api/favorite/remove?targetType=${item.targetType}&targetId=${item.targetId}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${userStore.token}` }
    })

    const data = await res.json()
    if (data.code === 200) {
      favoriteList.value = favoriteList.value.filter(i => i.id !== item.id)
      showToast('已取消')
    }
  } catch {
    showToast('操作失败')
  }
}

watch(activeTab, () => loadFavorites())
onMounted(() => loadFavorites())
</script>

<style scoped>
.favorite-page {
  background: #fff9e6;
  min-height: 100vh;
}
.moon-nav-bar {
  background: #FFE181;
}
.tab-bar {
  margin-top: 46px;
  background: #fff;
}
.list-wrapper {
  padding: 10px;
}
.card-disabled {
  opacity: 0.6;
  pointer-events: none;
}
.empty-tip {
  padding: 60px 0;
  text-align: center;
}
</style>