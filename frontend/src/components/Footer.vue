<template>
  <van-tabbar
    v-model="active"
    fixed
    placeholder
    :border="true"
    class="moon-footer"
    z-index="999"
  >
    <van-tabbar-item icon="home-o" to="/home">首页</van-tabbar-item>
    <van-tabbar-item icon="bag-o" to="/goods/list">商品</van-tabbar-item>
    <van-tabbar-item icon="cart-o" to="/cart">购物车</van-tabbar-item>
    <van-tabbar-item icon="chat-o" to="/chat">聊天</van-tabbar-item>
    <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
  </van-tabbar>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const active = ref(0)

watch(
  () => route.path,
  (newPath) => {
    if (newPath === '/home') active.value = 0
    else if (newPath.startsWith('/goods')) active.value = 1
    else if (newPath === '/cart') active.value = 2
    else if (newPath === '/chat') active.value = 3
    else if (
      newPath.startsWith('/profile') ||
      newPath.startsWith('/favorite') ||
      newPath.startsWith('/address') ||
      newPath.startsWith('/settings') ||
      newPath.startsWith('/admin/user') ||
      newPath.startsWith('/admin/merchant') ||
      newPath.startsWith('/admin/goods/audit') ||
      newPath.startsWith('/order')
    ) active.value = 4
    else active.value = 0
  },
  { immediate: true }
)
</script>

<style scoped>
.moon-footer {
  --van-tabbar-background: #fff9e6;
  --van-tabbar-item-active-color: #e5b94c;
  --van-tabbar-item-inactive-color: #666;
  --van-tabbar-height: 45px;
  position: fixed;
  height: 45px;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 999 !important;
}
</style>