<template>
  <div class="home-page">
    <!-- 顶部固定导航 + 搜索栏 -->
    <div class="home-header fixed">
      <div class="logo-box">
        <img class="logo-img" src="../assets/images/lunalogo.png" alt="logo" />
      </div>

      <van-search
        v-model="searchValue"
        placeholder="搜索商品"
        shape="round"
        background="#fff"
        @search="onSearch"
        class="search-bar"
      />

      <van-icon name="photo-o" size="20" @click="$router.replace('/search/ai')" />
    </div>

    <!-- 下拉刷新 -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <!-- 🔥 修复：自动轮播 + 手动切换按钮 -->
      <div class="banner-wrapper">
        <van-swipe
          class="banner"
          ref="swipeRef"
          autoplay
          interval="5000"
          indicator-color="#E5B94C"
          loop
        >
          <van-swipe-item v-for="img in allBannerImages" :key="img">
            <img :src="img" class="banner-img" alt="" />
          </van-swipe-item>
        </van-swipe>

        <!-- 手动左右切换按钮 -->
        <div class="swipe-controls" v-if="allBannerImages.length > 1">
          <div class="control-btn left" @click="swipePrev">
            <van-icon name="arrow-left" color="#fff" size="18" />
          </div>
          <div class="control-btn right" @click="swipeNext">
            <van-icon name="arrow" color="#fff" size="18" />
          </div>
        </div>
      </div>

      <!-- 金刚位菜单 -->
      <van-grid class="grid-nav" column-num="3" :border="false">
        <van-grid-item
          v-for="item in navList"
          :key="item.path"
          @click="$router.replace(item.path)"
        >
          <div class="grid-icon">
            <van-icon :name="item.icon" size="26" />
          </div>
          <div class="grid-text">{{ item.name }}</div>
        </van-grid-item>
      </van-grid>

      <!-- 精选好物 -->
      <div class="floor-title">
        <span>精选好物</span>
        <span class="more-text" @click="$router.replace('/goods/list')">更多 ></span>
      </div>

      <!-- 商品列表 + 空数据兜底 -->
      <div class="goods-list">
        <van-empty
          v-if="goodsList.length === 0"
          description="暂无商品"
          class="empty"
        />

        <div
          v-for="item in goodsList"
          :key="item.id"
          class="goods-card"
          @click="$router.replace(`/goods/detail/${item.id}`)"
        >
          <van-image
            :src="item.coverImage || item.imageList?.[0]?.imageUrl || 'https://picsum.photos/300/160'"
            width="100%"
            height="160"
            fit="cover"
          />
          <div class="goods-info">
            <div class="goods-name">{{ item.title }}</div>
            <div class="goods-price">¥{{ item.price.toFixed(2) }}</div>
          </div>
        </div>
      </div>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()
const searchValue = ref('')

// 导航菜单
const navList = ref([
  { name: '我的收藏', icon: 'star-o', path: '/favorite' },
  { name: '全部商品', icon: 'bag-o', path: '/goods/list' },
  { name: '购物车', icon: 'cart-o', path: '/cart' },
  { name: '我的订单', icon: 'orders-o', path: '/order/list' },
  { name: 'AI助手', icon: 'chat-o', path: '/chat/ai' },
  { name: '个人中心', icon: 'user-o', path: '/profile' },
])

// 首页数据
const bannerList = ref([])
const goodsList = ref([])
const refreshing = ref(false)
const allBannerImages = ref([])
const swipeRef = ref(null)

// ======================
// 🔥 统一搜索逻辑（和商品列表完全一致）
// ======================
const onSearch = () => {
  const key = searchValue.value?.trim()
  if (!key) {
    showToast('请输入搜索内容')
    return
  }

  // 跳转到商品列表页，并带上关键词
  router.push({
    path: '/goods/list',
    query: {
      keyword: key
    }
  })
}

// 手动切换上一张
const swipePrev = () => {
  if (swipeRef.value) swipeRef.value.prev()
}
// 手动切换下一张
const swipeNext = () => {
  if (swipeRef.value) swipeRef.value.next()
}

// 加载首页商品
const loadHomeData = async () => {
  try {
    const res = await fetch('/api/goods/list?sort=hot', {
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + userStore.token
      }
    })
    const data = await res.json()
    if (data.code === 200) {
      const list = data.data || []
      goodsList.value = list

      const images = []
      for (const goods of list) {
        if (images.length >= 5) break

        let cover = null
        if (goods.coverImage) {
          cover = goods.coverImage
        } else if (goods.imageList && goods.imageList.length > 0) {
          cover = goods.imageList[0].imageUrl
        }
        if (cover) images.push(cover)
      }

      allBannerImages.value = images
    }
  } catch (e) {
    showToast('加载失败')
  }
}

// 下拉刷新
const onRefresh = async () => {
  await loadHomeData()
  refreshing.value = false
}

onMounted(() => {
  loadHomeData()
})
</script>

<style scoped>
.home-page {
  padding-bottom: 60px;
  background: #fff9e6;
  min-height: 100vh;
}

.home-header {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: #FFE181;
  z-index: 99;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
}

.logo-box {
  width: 50px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
}
.logo-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.search-bar {
  flex: 1;
  margin: 0 10px;
}

/* 轮播图外层 */
.banner-wrapper {
  position: relative;
  margin-top: 50px;
  height: 150px;
  padding: 0 10px;
}
.banner {
  height: 100%;
}
.banner-img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 12px;
}

/* 左右切换按钮 */
.swipe-controls {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10px;
  pointer-events: none;
}
.control-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(0,0,0,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: all;
}

.grid-nav {
  background: #fff;
  padding: 15px 0;
  margin-top: 10px;
  border-radius: 12px;
}
.grid-icon {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: #fff0c2;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 8px;
}
.grid-text {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}

.floor-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  background: #fff;
  margin-top: 10px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: bold;
  color: #333;
}
.more-text {
  font-size: 12px;
  color: #ff9f00;
}

.goods-list {
  display: flex;
  flex-wrap: wrap;
  padding: 10px;
  background: #fff;
  margin-top: 10px;
  border-radius: 8px;
}
.goods-card {
  width: 48%;
  margin-bottom: 10px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}
.goods-card:nth-child(2n) {
  margin-left: 4%;
}
.goods-info {
  padding: 8px;
}
.goods-name {
  font-size: 13px;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.goods-price {
  font-size: 15px;
  color: #ff4040;
  font-weight: bold;
}

.empty {
  width: 100%;
  padding: 60px 0;
}
</style>