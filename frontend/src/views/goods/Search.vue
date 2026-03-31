<template>
  <div>
    <van-nav-bar title="搜索" fixed left-arrow @click-left="$router.back()" />

    <van-search
      v-model="keyword"
      placeholder="请输入搜索关键词"
      show-action
      @search="onSearch"
    />

    <!-- 真实商品列表 -->
    <div style="padding: 15px; margin-top: 45px">
      <!-- 无数据提示 -->
      <div v-if="goodsList.length === 0 && searched" style="text-align:center; color:#999; padding: 50px 0">
        暂无相关商品
      </div>

      <!-- 搜索结果 -->
      <van-card
        v-for="item in goodsList"
        :key="item.id"
        :title="item.title"
        :price="item.price"
        :thumb="item.imageList?.[0]?.imageUrl || 'https://picsum.photos/80/80'"
        @click="$router.push(`/goods/detail/${item.id}`)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '../../store/user'
import { showToast } from 'vant'

const userStore = useUserStore()
const keyword = ref('')         // 搜索关键词
const goodsList = ref([])       // 搜索结果商品
const searched = ref(false)     // 是否触发过搜索

// 搜索商品（真实后端接口）
const onSearch = async () => {
  const key = keyword.value?.trim()
  if (!key) {
    showToast('请输入搜索内容')
    return
  }

  try {
    const res = await fetch(`/api/goods/list?keyword=${key}&categoryId=&sort=default`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + userStore.token
      }
    })

    const data = await res.json()
    if (data.code === 200) {
      goodsList.value = data.data || []
      searched.value = true
    } else {
      showToast('搜索失败')
    }
  } catch (e) {
    console.error(e)
    showToast('网络异常')
  }
}
</script>