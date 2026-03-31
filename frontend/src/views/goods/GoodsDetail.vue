<template>
  <div class="goods-detail-page">
    <!-- 顶部导航栏（和首页一致） -->
    <div class="page-header fixed">
      <van-icon name="arrow-left" size="20" @click="$router.back()" />
      <div class="header-title">{{ goods.title || '商品详情' }}</div>
      <div class="header-right">
        <van-icon name="search" size="20" @click="$router.push('/search')" />
        <van-icon name="chat-o" size="20" class="ml-15" @click="$router.push('/chat')" />
      </div>
    </div>

    <!-- 轮播图 -->
    <van-swipe class="banner" :autoplay="0" indicator-color="#E5B94C">
      <van-swipe-item v-for="img in goods.imageList" :key="img.id">
        <van-image :src="img.imageUrl" width="100%" height="350" fit="cover" />
      </van-swipe-item>
    </van-swipe>

    <!-- 商品信息区 -->
    <div class="goods-info">
      <div class="title">{{ goods.title }}</div>
      <div class="price-row">
        <span class="price">¥{{ goods.price?.toFixed(2) }}</span>
        <span class="stock">库存：{{ goods.stock }}件</span>
      </div>

      <div class="stats-row-new">
        <div class="stat" @click="handleLike">
          <van-icon :name="isLiked ? 'like' : 'like-o'" :color="isLiked ? '#ff4444' : '#999'" />
          <span>{{ goods.likeCount || 0 }}</span>
        </div>
        <div class="stat">
          <van-icon name="eye-o" />
          <span>{{ goods.viewCount || 0 }}</span>
        </div>
        <div class="stat" @click="toggleCollect">
          <van-icon :name="isCollected ? 'star' : 'star-o'" :color="isCollected ? '#ff9f00' : '#999'" />
          <span>{{ goods.collectCount || 0 }}</span>
        </div>
        <div class="stat">
          <van-icon name="comment-o" />
          <span>{{ goods.commentCount || 0 }}</span>
        </div>
      </div>
    </div>

    <!-- 规格选择区 -->
    <div class="spec-section">
      <div class="spec-item">
        <span class="label">数量</span>
        <div class="stepper">
          <van-button size="mini" icon="minus" @click="count = Math.max(1, count - 1)" />
          <span class="count">{{ count }}</span>
          <van-button size="mini" icon="plus" @click="count = Math.min(goods.stock, count + 1)" />
        </div>
      </div>
    </div>

    <!-- 商家信息 -->
    <div class="merchant-section" @click="goMerchant">
      <div class="merchant-info">
        <van-icon name="shop-o" size="20" />
        <span class="merchant-name">{{ goods.merchant?.shopName || '店铺' }}</span>
      </div>
      <van-icon name="arrow" />
    </div>

    <!-- 商品描述 -->
    <div class="desc-section">
      <div class="section-title">商品描述</div>
      <div class="desc-content">{{ goods.description || '暂无描述' }}</div>
    </div>

    <!-- ====================== ✅ 评论区（新增发布 + 删除） ====================== -->
    <div class="comment-section">
      <div class="section-title">用户评价 ({{ comments.length }})</div>

      <!-- 发布评论 -->
      <div class="comment-publish">
        <van-field
          v-model="commentContent"
          rows="2"
          placeholder="说点什么..."
          class="comment-input"
        />
        <van-button
          type="primary"
          size="small"
          class="publish-btn"
          @click="publishComment"
        >
          发布
        </van-button>
      </div>

      <div v-if="comments.length === 0" class="empty-tip">暂无评价</div>

      <div v-for="item in comments" :key="item.id" class="comment-item">
        <!-- 🔥 新增：显示用户名 -->
        <div class="comment-user">{{ item.username || '匿名用户' }}</div>
        <div class="comment-content">{{ item.content }}</div>
        <div class="comment-bottom">
          <span class="comment-time">{{ formatTime(item.createTime) }}</span>

          <!-- 🔥 新增：评论点赞按钮 -->
          <div class="comment-like" @click.stop="handleCommentLike(item.id)">
            <van-icon name="like-o" size="14" />
            <span>{{ item.likeCount || 0 }}</span>
          </div>
         <!-- 只有自己的评论显示删除按钮 -->
          <van-button
            v-if="item.userId === userStore.userInfo.id"
            size="mini"
            type="danger"
            plain
            @click.stop="deleteComment(item.id)"
          >
            删除
          </van-button>
        </div>
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div class="bottom-bar fixed">
      <div class="bar-item" @click="toggleCollect">
        <van-icon :name="isCollected ? 'star' : 'star-o'" size="24" :color="isCollected ? '#ff9f00' : '#666'" />
        <span>收藏</span>
      </div>
      <van-button type="primary" color="#FFE181" class="cart-btn" @click="addToCart">加入购物车</van-button>
      <van-button type="primary" color="#ff9f00" class="buy-btn" @click="buyNow">立即购买</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { useUserStore } from '../../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const goodsId = route.params.id

const goods = ref({})
const comments = ref([])
const count = ref(1)
const isCollected = ref(false)
const isLiked = ref(false)

// 评论发布
const commentContent = ref('')

// 加载商品详情
const loadGoodsDetail = async () => {
  try {
    await fetch(`/api/goods/incr/view/${goodsId}`, {
      headers: { Authorization: 'Bearer ' + userStore.token }
    })

    const res = await fetch(`/api/goods/${goodsId}`, {
      headers: { Authorization: 'Bearer ' + userStore.token }
    })
    const data = await res.json()
    if (data.code !== 200) throw new Error('加载失败')
    goods.value = data.data

    await loadComments()
    await checkIsFavorite()
  } catch (err) {
    showToast('加载失败')
  }
}

// ====================== ✅ 加载评论 ======================
const loadComments = async () => {
  try {
    const res = await fetch(`/api/goods/comment/list/${goodsId}`, {
      headers: { Authorization: 'Bearer ' + userStore.token }
    })
    const data = await res.json()
    if (data.code === 200) {
      comments.value = data.data || []
    }
  } catch (err) {
    console.error('加载评论失败', err)
  }
}

// ====================== ✅ 发布评论 ======================
const publishComment = async () => {
  if (!commentContent.value.trim()) {
    showToast('请输入评论内容')
    return
  }
  try {
    const res = await fetch('/api/goods/comment', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + userStore.token
      },
      body: JSON.stringify({
        goodsId: goodsId,
        content: commentContent.value,
        parentId: 0
      })
    })
    const data = await res.json()
    if (data.code === 200) {
      showToast('发布成功')
      commentContent.value = ''
      await loadComments() // 刷新评论
    } else {
      showToast(data.msg || '发布失败')
    }
  } catch (err) {
    showToast('发布失败')
  }
}

// ====================== ✅ 删除评论 ======================
const deleteComment = async (commentId) => {
  try {
    await showConfirmDialog({
      message: '确定删除这条评论？'
    })
    const res = await fetch(`/api/goods/comment/${commentId}`, {
      method: 'DELETE',
      headers: {
        Authorization: 'Bearer ' + userStore.token
      }
    })
    const data = await res.json()
    if (data.code === 200) {
      showToast('删除成功')
      await loadComments()
    } else {
      showToast(data.msg || '删除失败')
    }
  } catch (e) {
    // 取消删除
  }
}

// ====================== 🔥 新增：评论点赞 ======================
const handleCommentLike = async (commentId) => {
  try {
    const res = await fetch(`/api/goods/comment/like/${commentId}`, {
      method: 'POST',
      headers: {
        Authorization: 'Bearer ' + userStore.token
      }
    })
    const data = await res.json()
    if (data.code === 200) {
      showToast(data.msg)
      // 点赞后刷新评论列表
      loadComments()
    } else {
      showToast(data.msg || '操作失败')
    }
  } catch (err) {
    showToast('操作失败')
  }
}

// 检查是否收藏
const checkIsFavorite = async () => {
  try {
    const res = await fetch(`/api/favorite/check?targetType=GOODS&targetId=${goodsId}`, {
      headers: { Authorization: 'Bearer ' + userStore.token }
    })
    const data = await res.json()
    if (data.code === 200) {
      isCollected.value = data.data
    }
  } catch (_) {}
}

// 点赞
const handleLike = async () => {
  try {
    if (isLiked.value) {
      await fetch(`/api/goods/decr/like/${goodsId}`, {
        method: 'POST',
        headers: { Authorization: 'Bearer ' + userStore.token }
      })
      isLiked.value = false
      goods.value.likeCount = Math.max(0, goods.value.likeCount - 1)
      showToast('取消点赞')
    } else {
      await fetch(`/api/goods/incr/like/${goodsId}`, {
        method: 'POST',
        headers: { Authorization: 'Bearer ' + userStore.token }
      })
      isLiked.value = true
      goods.value.likeCount = (goods.value.likeCount || 0) + 1
      showToast('点赞成功')
    }
  } catch (_) {
    showToast('操作失败')
  }
}

// 收藏/取消收藏
const toggleCollect = async () => {
  try {
    if (isCollected.value) {
      await fetch(`/api/favorite/remove?targetType=GOODS&targetId=${goodsId}`, {
        method: 'DELETE',
        headers: { Authorization: 'Bearer ' + userStore.token }
      })
      showToast('取消收藏')
    } else {
      await fetch(`/api/favorite/add?targetType=GOODS&targetId=${goodsId}`, {
        method: 'POST',
        headers: { Authorization: 'Bearer ' + userStore.token }
      })
      showToast('收藏成功')
    }
    await loadGoodsDetail()
  } catch (_) {
    showToast('操作失败')
  }
}

// 去商家主页
const goMerchant = () => {
  const mid = goods.value.merchantId || goods.value.merchant?.id
  if (mid) router.push({ path: '/merchant', query: { merchantId: mid } })
}

// 加入购物车
// 加入购物车（对接后端接口）
const addToCart = async () => {
  // 未登录拦截
  if (!userStore.isLogin) {
    showToast('请先登录')
    return
  }

  try {
    const res = await fetch('/api/cart/add?goodsId=' + goodsId + '&quantity=' + count.value, {
      method: 'POST',
      headers: {
        Authorization: 'Bearer ' + userStore.token
      }
    })

    const data = await res.json()
    if (data.code === 200) {
      showToast('加入购物车成功 ✅')
    } else {
      showToast(data.msg || '加入失败')
    }
  } catch (err) {
    showToast('加入购物车失败')
    console.error(err)
  }
}

// 🔥 商品页 → 联系商家（带商品ID + 商家ID）
const goToChatWithMerchant = () => {
  const merchantId = goods.value.merchantId || goods.value.merchant?.id
  if (!merchantId) {
    showToast('商家信息不存在')
    return
  }

  // 跳转到聊天，携带：商家ID、商品ID、商品名称、店铺名称
  router.push({
    path: `/chat/${merchantId}`,
    query: {
      targetName: goods.value.merchant?.shopName || '商家',
      goodsId: goodsId,               // 商品ID
      goodsTitle: goods.value.title  // 商品名称（聊天页显示）
    }
  })
}

// 立即购买
const buyNow = () => router.push({
  path: '/order/confirm',
  query: { goodsId, count: count.value }
})

const formatTime = (t) => t ? new Date(t).toLocaleString() : ''

onMounted(() => loadGoodsDetail())
</script>

<style scoped>
.goods-detail-page {
  background: #fff;
  min-height: 100vh;
  padding-bottom: 120px;
}

.page-header {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  background: #FFE181;
  z-index: 99;
  position: fixed;
  top: 0; left: 0; right: 0;
}
.header-title { flex: 1; text-align: center; font-size: 16px; }
.header-right { display: flex; gap: 15px; }
.ml-15 { margin-left: 15px; }

.banner { margin-top: 50px; height: 350px; }

.goods-info { padding: 15px; border-bottom: 8px solid #f8f8f8; }
.title { font-size: 16px; font-weight: bold; margin-bottom: 10px; }
.price-row { display: flex; align-items: center; gap: 15px; margin-bottom: 10px; }
.price { font-size: 22px; color: #ff4444; font-weight: bold; }
.stock { font-size: 13px; color: #666; }

.stats-row-new {
  display: flex;
  gap: 20px;
  margin-top: 8px;
}
.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
}

.spec-section { padding: 15px; border-bottom: 8px solid #f8f8f8; }
.spec-item { display: flex; justify-content: space-between; align-items: center; }
.stepper { display: flex; align-items: center; gap: 10px; }
.count { width: 30px; text-align: center; }

.merchant-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 8px solid #f8f8f8;
}
.merchant-info { display: flex; align-items: center; gap: 8px; }

.desc-section, .comment-section {
  padding: 15px;
  border-bottom: 8px solid #f8f8f8;
}
.section-title { font-size: 15px; font-weight: bold; margin-bottom: 10px; }
.desc-content { font-size: 13px; color: #666; line-height: 1.6; }

/* ====================== ✅ 评论样式 ====================== */
.comment-publish {
  margin-bottom: 15px;
}
.comment-input {
  margin-bottom: 8px;
}
.publish-btn {
  width: 100%;
  background: #ff9;
  border-color: #ff9;
  color: #333; 
}
.comment-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.comment-user {
  font-size: 13px;
  color: #ff8c00;
  font-weight: 600;
  margin-bottom: 4px;
}

.comment-content {
  font-size: 13px;
  margin-bottom: 4px;
}
.comment-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.comment-time {
  font-size: 11px;
  color: #999;
}
.empty-tip {
  text-align: center;
  color: #999;
  padding: 20px 0;
}

.bottom-bar {
  display: flex;
  align-items: center;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  padding: 8px 15px;
  position: fixed;
  bottom: 50px;
  left: 0; right: 0;
  z-index: 99;
}
.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 60px;
  font-size: 12px;
  color: #666;
}
.cart-btn { flex: 1; margin: 0 10px; background: #FFE181; color: #333; border: none; }
.buy-btn { flex: 1; background: #ff9f00; color: #fff; border: none; }
</style>