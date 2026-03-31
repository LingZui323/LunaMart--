<template>
  <div class="goods-list-page">
    <!-- 顶部导航（完全保持你原来的样子，没动！） -->
    <div class="page-header fixed">
      <div class="logo-box">
        <img class="logo-img" src="../../assets/images/lunalogo.png" alt="logo" />
      </div>

      <van-search
         ref="searchRef"  
        v-model="searchKey"
        placeholder="搜索商品"
        shape="round"
        background="#fff"
        @search="handleSearch"
        @clear="handleClearSearch"  
        class="search-bar"
      />

      <van-icon name="photo-o" size="20" @click="$router.push('/search/ai')" />
    </div>

    <!-- 分类标签栏（搜索时自动隐藏） -->
    <div v-show="!isSearching" class="category-container">
      <div class="category-left-scroll">
        <div class="category-left">
          <div
            v-for="item in categoryList"
            :key="item.id"
            :class="['left-item', { active: activeMainId === item.id }]"
            @click="switchCategory(item.id)"
          >
            {{ item.name }}
          </div>
        </div>
      </div>

      <div class="category-right">
        <div
          v-for="sub in currentSubList"
          :key="sub.id"
          class="right-item"
          @click="selectSubTag(sub.id)"
        >
          {{ sub.name }}
        </div>
      </div>
    </div>

    <!-- 🔥 排序按钮栏 -->
    <div class="sort-bar" v-show="!isSearching">
      <button 
        :class="['sort-btn', { active: currentSort === 'default' }]"
        @click="changeSort('default')"
      >
        最新
      </button>
      <button 
        :class="['sort-btn', { active: currentSort === 'hot' }]"
        @click="changeSort('hot')"
      >
        热门
      </button>
      <button 
        :class="['sort-btn', { active: currentSort === 'price_asc' }]"
        @click="changeSort('price_asc')"
      >
        价格↑
      </button>
      <button 
        :class="['sort-btn', { active: currentSort === 'price_desc' }]"
        @click="changeSort('price_desc')"
      >
        价格↓
      </button>
    </div>

    <!-- 商品列表 -->
    <div class="goods-wrapper">
      <div v-if="showGoods.length === 0" class="empty-tip">暂无商品</div>

      <GoodsCard
        v-for="item in showGoods"
        :key="item.id"
        :goods="item"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router' // 🔥 只加了 useRoute
import GoodsCard from '../../components/GoodsCard.vue'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute() // 🔥 只加了 route
const searchKey = ref('')
const isSearching = ref(false)
const searchRef = ref(null)

// ====================== 分类数据 ======================
const categoryList = ref([])
const subCategoryMap = ref({})
const activeMainId = ref(null)
const selectedSubId = ref(null)

const currentSubList = computed(() => {
  return subCategoryMap.value[activeMainId.value] || []
})

// 加载分类
const loadCategories = async () => {
  try {
    const res = await fetch('/api/category/list', {
      headers: { 'Content-Type': 'application/json' }
    })
    const data = await res.json()
    if (data.code === 200) {
      const allCates = data.data || []
      categoryList.value = allCates.filter(c => c.parentId === 0 || c.level === 1)
      
      const subMap = {}
      allCates.forEach(cate => {
        if (cate.parentId !== 0 && (cate.level === 2 || cate.level === 3)) {
          if (!subMap[cate.parentId]) subMap[cate.parentId] = []
          subMap[cate.parentId].push(cate)
        }
      })
      subCategoryMap.value = subMap
    }
  } catch (e) {
    console.error('分类加载失败', e)
  }
}

// ====================== 商品统一查询 ======================
const goodsList = ref([])
const currentSort = ref('default')

const loadGoods = async () => {
  try {
    const keyword = searchKey.value?.trim() || ''

    // ✅【关键修改】
    // 首次进入页面 → categoryId = null → 查询所有商品
    const categoryId = selectedSubId.value || activeMainId.value || ''

    const sort = currentSort.value

    const res = await fetch(`/api/goods/list?keyword=${keyword}&categoryId=${categoryId}&sort=${sort}`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + userStore.token
      }
    })

    const data = await res.json()
    if (data.code === 200) {
      goodsList.value = data.data || []
    }
  } catch (e) {
    console.error('商品加载失败', e)
  }
}

const showGoods = computed(() => goodsList.value)

// ====================== 交互逻辑 ======================
const switchCategory = (id) => {
  activeMainId.value = id
  selectedSubId.value = null
  loadGoods()
}

const selectSubTag = (id) => {
  selectedSubId.value = id
  loadGoods()
}

const handleSearch = () => {
  isSearching.value = !!searchKey.value.trim()
  loadGoods()
}

const handleClearSearch = () => {
  searchKey.value = ''
  isSearching.value = false
  // 🔥 强制清空 van-search 内部值
  if (searchRef.value) {
    searchRef.value.clear()
  }
  
  loadGoods()
}

const changeSort = (sortType) => {
  currentSort.value = sortType
  loadGoods()
}

// ✅【最终正确逻辑】
onMounted(async () => {
  await loadCategories()

  // 🔥 只加这段：从主页接收关键词并自动搜索
  const keyword = route.query.keyword
  if (keyword) {
    searchKey.value = keyword
    isSearching.value = true
  }

  loadGoods()
})
</script>

<style scoped>
/* 你的样式完全没动！ */
.goods-list-page {
  background: #fff9e6;
  min-height: 100vh;
  padding-bottom: 70px;
}
.page-header {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: #FFE181;
  z-index: 99;
  position: fixed;
  top: 0; left: 0; right: 0;
}
.logo-box { width: 50px; height: 40px; display: flex; align-items: center; justify-content: center; margin-right: 8px; }
.logo-img { width: 100%; height: 100%; object-fit: contain; }
.search-bar { flex: 1; margin: 0 10px; }

.category-container {
  display: flex;
  background: #fff;
  border-radius: 12px;
  margin: 55px 10px 10px;
  overflow: hidden;
}

.category-left-scroll {
  width: 26%;
  max-height: 320px;
  overflow-y: auto;
  border-right: 1px solid #f2f2f2;
}
.category-left-scroll::-webkit-scrollbar { width: 0; }
.category-left { display: flex; flex-direction: column; }
.left-item { padding: 20px 10px; text-align: center; font-size: 14px; color: #666; flex-shrink: 0; }
.left-item.active { color: #ff9f00; font-weight: bold; background: #fff9e6; }

.category-right { width: 74%; padding: 10px; display: flex; flex-wrap: wrap; gap: 8px; }
.right-item { padding: 8px 12px; background: #f7f7f7; border-radius: 8px; font-size: 13px; color: #333; }
.right-item:active { background: #FFE181; }

.sort-bar {
  display: flex;
  background: #fff;
  border-radius: 12px;
  margin: 0 10px 10px;
  padding: 8px 12px;
  gap: 12px;
}
.sort-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  background: #f7f7f7;
  font-size: 13px;
  color: #666;
  cursor: pointer;
}
.sort-btn.active {
  background: #FFE181;
  color: #ff9f00;
  font-weight: bold;
}

.goods-wrapper {
  display: flex;
  flex-wrap: wrap;
  padding: 10px;
  gap: 4%;
}
.goods-card {
  width: 48%;
  margin-bottom: 12px;
}
.empty-tip { width: 100%; text-align: center; padding: 40px 0; color: #999; }
</style>