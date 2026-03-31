<template>
  <div class="chat-window-page">
    <van-nav-bar
      :title="targetName"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <div class="info-bar">
      <div v-if="targetIdFromQuery" class="info-item">
        <span>订单号：{{ targetIdFromQuery }}</span>
      </div>
      <div class="info-item">
        <span>{{ targetName }}</span>
      </div>
    </div>

    <div class="message-list" ref="messageListRef">
      <div
        v-for="msg in messageList"
        :key="msg.id"
        class="message-item"
        :class="{ self: msg.senderId === currentUserId }"
      >
        <div v-if="msg.senderId !== currentUserId" class="message-bubble receive">
          <div class="message-content">{{ msg.content }}</div>
          <div class="message-time">{{ formatTime(msg.createTime) }}</div>
        </div>
        <div v-if="msg.senderId === currentUserId" class="message-bubble send">
          <div class="message-content">{{ msg.content }}</div>
          <div class="message-time">
            {{ formatTime(msg.createTime) }}
            <span v-if="msg.isRead === 1" class="read-status">已读</span>
          </div>
        </div>
      </div>
    </div>

    <div class="input-bar">
      <van-field v-model="inputContent" placeholder="请输入消息..." @keypress.enter="sendMessage" />
      <van-button type="primary" color="#ff9f00" size="small" :disabled="!inputContent.trim()" @click="sendMessage">发送</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../store/user'
import { showToast } from 'vant'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const messageListRef = ref(null)

const currentUserId = computed(() => userStore.userInfo?.id || null)

// 👇 从列表页直接传递的核心参数（真正实现隔离）
const sessionId = ref(route.query.sessionId || null)
const targetName = computed(() => route.query.targetName || '商家')
const targetType = computed(() => route.query.targetType || 'ORDER')
const targetIdFromQuery = computed(() => route.query.targetId || null)

const inputContent = ref('')
const messageList = ref([])

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// ==============================================
// ✅ 关键修复：直接使用列表传来的 sessionId，不再重新创建
// ==============================================
const initSession = async () => {
  if (!sessionId.value) {
    showToast('会话不存在')
    return
  }
  // 直接加载当前会话消息
  loadMessageList()
}

// 加载消息列表
const loadMessageList = async () => {
  if (!sessionId.value) return
  try {
    const res = await fetch(`/api/chat/msg/list?sessionId=${sessionId.value}`, {
      headers: { Authorization: `Bearer ${userStore.token}` }
    })
    const data = await res.json()
    if (data.code === 200) {
      messageList.value = data.data
      await nextTick()
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  } catch (e) {}
}

// 发送消息
const sendMessage = async () => {
  const content = inputContent.value.trim()
  if (!content) return
  if (!sessionId.value) {
    showToast('会话不存在，请重试')
    return
  }

  try {
    const res = await fetch('/api/chat/send', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': `Bearer ${userStore.token}`
      },
      body: `sessionId=${sessionId.value}&content=${encodeURIComponent(content)}`
    })

    const data = await res.json()
    if (data.code === 200) {
      inputContent.value = ''
      loadMessageList()
    } else {
      showToast(data.msg || '发送失败')
    }
  } catch (err) {
    showToast('发送失败')
  }
}

// WebSocket 实时刷新
let ws = null
const connectWs = () => {
  if (!currentUserId.value) return
  if (ws) ws.close()
  ws = new WebSocket(`ws://localhost:10010/chat/ws/${currentUserId.value}`)
  ws.onmessage = () => loadMessageList()
}

watch(currentUserId, (id) => {
  if (id) connectWs()
}, { immediate: true })

onMounted(() => {
  initSession()
})

onUnmounted(() => {
  if (ws) ws.close()
})
</script>

<style scoped>
.chat-window-page {
  background: #f5f5f5;
  min-height: 100vh;
  padding: 46px 0 110px;
  box-sizing: border-box;
}

.moon-nav-bar {
  background: #FFE181;
}
.moon-nav-bar .van-nav-bar__title {
  color: #333;
  font-weight: 500;
}

.info-bar {
  background: #fff;
  padding: 8px 15px;
  font-size: 13px;
  color: #666;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}
.info-item {
  background: #fff9e6;
  padding: 4px 8px;
  border-radius: 4px;
}

.message-list {
  padding: 15px;
  height: calc(100vh - 180px);
  overflow-y: auto;
}
.message-item {
  display: flex;
  margin-bottom: 15px;
}
.message-item.self {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 16px;
}
.message-bubble.receive {
  background: #fff;
  border: 1px solid #e5e5e5;
  border-bottom-left-radius: 4px;
}
.message-bubble.send {
  background: #ff9f00;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message-content {
  font-size: 15px;
  line-height: 1.6;
}
.message-time {
  font-size: 11px;
  color: #999;
  display: flex;
  justify-content: flex-end;
  gap: 6px;
}
.read-status {
  color: #07c160;
}

.input-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 15px;
  background: #fff;
  border-top: 1px solid #e5e5e5;
  position: fixed;
  bottom: 75px;
  left: 0;
  right: 0;
}
.input-bar .van-field {
  flex: 1;
  border-radius: 20px;
  background: #f5f5f5;
  padding: 6px 12px;
}
</style>