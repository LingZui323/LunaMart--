<template>
  <div class="goods-card" @click="$router.push(`/goods/detail/${goods.id}`)">
    <!-- 👇 从 imageList 取第一张图 -->
    <van-image 
     :src="goods.coverImage || goods.imageList?.[0]?.imageUrl || 'https://picsum.photos/300/160'"
      width="100%" 
      height="160" 
      fit="cover" 
    />
    <div class="goods-info">
      <div class="goods-name">{{ goods.title }}</div>
      <div class="goods-price">¥{{ goods.price.toFixed(2) }}</div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const props = defineProps({
  goods: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.goods-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
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
</style>