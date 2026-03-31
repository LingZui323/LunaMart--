<template>
  <div class="chat-list-page">
    <van-nav-bar
      title="聊天对象"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <div class="container">
      <van-empty v-if="sessionList.length === 0 && !loading" description="暂无会话" />

      <div
        v-for="item in sessionList"
        :key="item.id"
        class="session-item"
        @click="goToChat(item)"
      >
        <div class="session-info">
          <div class="session-top">
            <div class="session-name">
              {{ item.targetName }}
              <span v-if="item.targetType" class="target-tag">
                {{ item.targetType === 'ORDER' ? '订单' : '商品' }} {{ item.targetId }}
              </span>
            </div>
            <div class="session-time">{{ formatTime(item.lastMsgTime) }}</div>
          </div>
          <div class="session-bottom">
            <div class="last-msg">{{ item.lastMessage || '暂无消息' }}</div>
            <div v-if="item.unreadCount > 0" class="unread-badge">
              {{ item.unreadCount > 99 ? '99+' : item.unreadCount }}
            </div>
          </div>
        </div>
      </div>

      <van-loading v-if="loading" class="loading">加载中...</van-loading>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../store/user'
import { showToast } from 'vant'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const currentUserId = computed(() => userStore.userInfo?.id || null)
const sessionList = ref([])
const loading = ref(false)
const isMounted = ref(true)

// 获取对方用户ID
const getTargetUserId = (session) => {
  return session.user1Id === currentUserId.value ? session.user2Id : session.user1Id
}

// 时间格式化
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const dayDiff = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (dayDiff === 0) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (dayDiff === 1) {
    return '昨天'
  } else if (dayDiff < 7) {
    return `${dayDiff}天前`
  } else {
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
  }
}

// 进入聊天（携带完整参数：sessionId + 订单信息）
const goToChat = (session) => {
  const targetId = getTargetUserId(session)
  router.push({
    path: `/chat/${targetId}`,
    query: {
      sessionId: session.id,
      targetName: session.targetName,
      targetType: session.targetType,
      targetId: session.targetId
    }
  })
}

// 加载会话列表（后端已返回：用户名、最后消息、未读数）
const loadSessionList = async () => {
  if (!currentUserId.value) return
  loading.value = true
  try {
    const res = await fetch('/api/chat/session/list', {
      headers: {
        Authorization: `Bearer ${userStore.token}`
      }
    })
    const data = await res.json()
    if (data.code === 200) {
      sessionList.value = data.data || []
    }
  } catch (err) {
    showToast('加载会话失败')
  } finally {
    loading.value = false
  }
}

// ------------------------------
// WebSocket 实时刷新
// ------------------------------
let ws = null
let timer = null
let reconnectTimer = null
const RECONNECT_DELAY = 3000

const connectWs = () => {
  if (!currentUserId.value || !isMounted.value) {
    if (ws) { ws.close(); ws = null }
    if (reconnectTimer) clearTimeout(reconnectTimer)
    return
  }

  if (ws && ws.readyState === WebSocket.OPEN) return
  if (ws) { ws.close(); ws = null }

  const wsUrl = `ws://localhost:10010/chat/ws/${currentUserId.value}`
  ws = new WebSocket(wsUrl)

  ws.onopen = () => {
    console.log('✅ WebSocket 连接成功')
  }

  ws.onmessage = () => {
    loadSessionList()
  }

  ws.onerror = (e) => {
    console.error('❌ WebSocket 错误', e)
  }

  ws.onclose = () => {
    if (!isMounted.value) return
    reconnectTimer = setTimeout(() => connectWs(), RECONNECT_DELAY)
  }
}

watch(currentUserId, () => connectWs(), { immediate: true })

onMounted(() => {
  isMounted.value = true
  loadSessionList()
  timer = setInterval(loadSessionList, 5000)
})

onUnmounted(() => {
  isMounted.value = false
  clearInterval(timer)
  if (ws) ws.close()
  if (reconnectTimer) clearTimeout(reconnectTimer)
})
</script>

<style scoped>
.chat-list-page {
  background: #fff9e6;
  min-height: 100vh;
  padding: 46px 0 80px;
  box-sizing: border-box;
}

.moon-nav-bar {
  background: #FFE181;
}
.moon-nav-bar .van-nav-bar__title {
  color: #333;
  font-weight: 500;
}

.container { padding: 0; }

.session-item {
  padding: 12px 15px;
  background: #ffffff;
  border-bottom: 1px solid #f6f6f6;
}
.session-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.session-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.session-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  display: flex;
  align-items: center;
  gap: 6px;
}
.target-tag {
  font-size: 12px;
  color: #ff9f00;
  background: #fff3e0;
  padding: 1px 5px;
  border-radius: 4px;
}
.session-time {
  font-size: 12px;
  color: #999;
}
.session-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.last-msg {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 280px;
}
.unread-badge {
  background: #ff4444;
  color: #fff;
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 10px;
  min-width: 16px;
  text-align: center;
}

.loading {
  text-align: center;
  padding: 15px 0;
  color: #999;
}
</style>