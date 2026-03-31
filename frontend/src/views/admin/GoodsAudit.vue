<template>
  <div class="admin-page">
    <van-nav-bar
      title="商品审核"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <div class="container" style="margin-top: 50px; padding-bottom: 80px">
      <div class="filter-bar">
        <van-search
          v-model="searchKey"
          placeholder="搜索商品名称/ID"
          show-action
          @search="handleSearch"
          class="search-bar"
        />
        <van-dropdown-menu active-color="#ff9f00">
          <van-dropdown-item v-model="filterStatus" :options="statusOptions" @change="handleFilter" />
        </van-dropdown-menu>
      </div>

      <van-cell-group inset class="list-card">
        <van-cell
          v-for="goods in filteredGoodsList"
          :key="goods.id"
          is-link
          @click="openGoodsDetail(goods)"
        >
          <template #icon>
            <img :src="goods.coverImage || 'https://picsum.photos/60/60'" class="goods-img" />
          </template>
          <template #title>
            <div class="goods-row">
              <div>
                <div class="goods-title">{{ goods.title }}</div>
                <div class="goods-info-text">
                  商家ID:{{ goods.merchantId }} | 价格:¥{{ goods.price }} | 库存:{{ goods.stock }}
                </div>
              </div>
              <div class="goods-status">
                <van-tag :type="goodsStatusMap[goods.status].type" size="small">
                  {{ goodsStatusMap[goods.status].text }}
                </van-tag>
              </div>
            </div>
          </template>
        </van-cell>

        <div v-if="filteredGoodsList.length === 0" class="empty">
          <van-empty description="暂无待审核商品" />
        </div>
      </van-cell-group>
    </div>

    <van-popup v-model:show="showDetailPopup" position="bottom" :style="{ height: '90%', borderRadius: '16px 16px 0 0' }">
      <div class="detail-popup">
        <div class="popup-header">
          <div class="popup-title">商品审核详情</div>
          <van-icon name="cross" @click="showDetailPopup = false" />
        </div>

        <van-cell-group inset class="info-group">
          <van-cell title="商品ID" :value="currentGoods.id" />
          <van-cell title="商品名称" :value="currentGoods.title" />
          <van-cell title="商家ID" :value="currentGoods.merchantId" />
          <van-cell title="分类ID" :value="currentGoods.categoryId" />
          <van-cell title="价格" :value="`¥${currentGoods.price}`" />
          <van-cell title="库存" :value="currentGoods.stock" />
          <van-cell title="描述" :label="currentGoods.description" />
          <van-cell title="当前状态">
            <template #extra>
              <van-tag :type="goodsStatusMap[currentGoods.status].type">
                {{ goodsStatusMap[currentGoods.status].text }}
              </van-tag>
            </template>
          </van-cell>
        </van-cell-group>

        <div v-if="currentGoods.status === 'PENDING_AUDIT'" class="audit-actions">
          <van-button type="danger" plain block @click="handleAudit('REJECTED')">驳回</van-button>
          <van-button type="primary" color="#ff9f00" block @click="handleAudit('ON_SALE')">通过并上架</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const token = userStore.token

// 搜索筛选
const searchKey = ref('')
const filterStatus = ref('')

const statusOptions = ref([
  { text: '全部', value: '' },
  { text: '待AI审核', value: 'PENDING_AI_AUDIT' },
  { text: '待审核', value: 'PENDING_AUDIT' },
  { text: '在售', value: 'ON_SALE' },
  { text: '已驳回', value: 'REJECTED' },
  { text: 'AI驳回', value: 'AI_REJECTED' },
  { text: '草稿', value: 'DRAFT' },
  { text: '已下架', value: 'OUT_OF_STOCK' }
])

// 商品列表
const goodsList = ref([])

// 状态映射
const goodsStatusMap = {
  DRAFT: { type: 'default', text: '草稿' },
  PENDING_AI_AUDIT: { type: 'primary', text: '待AI审核' },
  PENDING_AUDIT: { type: 'warning', text: '待审核' },
  ON_SALE: { type: 'success', text: '在售' },
  OUT_OF_STOCK: { type: 'default', text: '已下架' },
  REJECTED: { type: 'danger', text: '已驳回' },
  AI_REJECTED: { type: 'danger', text: 'AI驳回' }
}

// 弹窗
const showDetailPopup = ref(false)
const currentGoods = ref({})

// 筛选列表
const filteredGoodsList = computed(() => {
  let res = goodsList.value
  if (searchKey.value) {
    res = res.filter(g => g.title.includes(searchKey.value) || String(g.id).includes(searchKey.value))
  }
  if (filterStatus.value) {
    res = res.filter(g => g.status === filterStatus.value)
  }
  return res
})

// ======================================
// 加载【待人工审核】商品列表
// 对接：/api/goods/admin/pending-audit
// ======================================
const loadGoodsList = async () => {
  try {
    const res = await fetch('/api/goods/admin/pending-audit', {
      headers: { Authorization: 'Bearer ' + token }
    })
    const data = await res.json()
    if (data.code === 200) {
      goodsList.value = data.data || []
    }
  } catch (e) {
    showToast('加载失败')
  }
}

// ======================================
// 打开详情
// ======================================
const openGoodsDetail = (goods) => {
  currentGoods.value = goods
  showDetailPopup.value = true
}

// ======================================
// 管理员审核
// 对接：/goods/audit/{goodsId}?status=xxx
// ======================================
const handleAudit = async (status) => {
  const id = currentGoods.value.id
  const text = status === 'ON_SALE' ? '通过并上架' : '驳回'

  try {
    await showConfirmDialog('确定要' + text + '吗？')

    const res = await fetch(`/api/goods/audit/${id}?status=${status}`, {
      method: 'POST',
      headers: { Authorization: 'Bearer ' + token }
    })

    const data = await res.json()
    if (data.code === 200) {
      showToast(text + '成功')
      loadGoodsList()
      showDetailPopup.value = false
    } else {
      showToast(data.msg || text + '失败')
    }
  } catch (e) {
    showToast('已取消')
  }
}

const handleSearch = () => {}
const handleFilter = () => {}

onMounted(() => {
  loadGoodsList()
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
.filter-bar {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 12px;
}
.search-bar {
  background: #fff;
  border-radius: 12px;
}
.list-card {
  background: #fff;
  border-radius: 12px;
}
.goods-img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
  margin-right: 10px;
}
.goods-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.goods-title {
  font-weight: bold;
  font-size: 15px;
  margin-bottom: 4px;
}
.goods-info-text {
  font-size: 12px;
  color: #999;
}
.goods-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}
.empty {
  padding: 40px 0;
  text-align: center;
}
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
  margin-bottom: 16px;
}
.audit-actions {
  display: flex;
  gap: 10px;
}
.audit-actions .van-button {
  flex: 1;
  border-radius: 8px;
}
</style>
