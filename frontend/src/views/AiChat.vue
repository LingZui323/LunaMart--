<template>
  <div class="ai-chat-page">
    <!-- 顶部导航 -->
    <van-nav-bar
      title="AI 智能客服"
      fixed
      left-arrow
      @click-left="goBackHome"  
      class="moon-nav-bar"
    />

    <!-- 消息列表 -->
    <div class="message-list" ref="messageListRef">
      <div
        v-for="(msg, idx) in messageList"
        :key="idx"
        class="message-item"
        :class="{ self: msg.sender === 'user' }"
      >
        <!-- AI 回复 -->
        <div v-if="msg.sender === 'ai'" class="message-bubble receive">
          <div class="message-content">{{ msg.content }}</div>
        </div>

        <!-- 用户消息 -->
        <div v-if="msg.sender === 'user'" class="message-bubble send">
          <div class="message-content">{{ msg.content }}</div>
        </div>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="loading-tip">AI 正在思考...</div>
    </div>

    <!-- 输入框 -->
    <div class="input-bar">
      <van-field
        v-model="inputContent"
        placeholder="请输入你想咨询的问题..."
        @keypress.enter="sendMessage"
      />
      <van-button
        type="primary"
        color="#ff9f00"
        size="small"
        :disabled="!inputContent.trim()"
        @click="sendMessage"
      >
        发送
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()
const messageListRef = ref(null)

// 消息列表
const messageList = ref([
  { sender: 'ai', content: '你好！我是 AI 智能客服，有什么可以帮你的吗😊' }
])

const inputContent = ref('')
const loading = ref(false)

// 发送消息给 AI
const sendMessage = async () => {
  const content = inputContent.value.trim()
  if (!content) return

  // 加入用户消息
  messageList.value.push({ sender: 'user', content })
  inputContent.value = ''
  loading.value = true

  await nextTick()
  scrollToBottom()

  try {
    // 请求后端 /ai/chat 接口
    const res = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        Authorization: 'Bearer ' + userStore.token
      },
      body: `message=${encodeURIComponent(content)}`
    })

    const data = await res.json()
    if (data.code === 200) {
      messageList.value.push({ sender: 'ai', content: data.data })
    } else {
      messageList.value.push({ sender: 'ai', content: 'AI 服务异常，请稍后再试' })
    }
  } catch (err) {
    messageList.value.push({ sender: 'ai', content: '网络异常，连接失败' })
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}
// 返回首页
const goBackHome = () => {
  router.replace('/home')
}

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.ai-chat-page {
  background: #f5f5f5;
  min-height: 100vh;
  padding: 46px 0 110px;
  box-sizing: border-box;
}

.moon-nav-bar {
  background: #ffe181;
}
.moon-nav-bar .van-nav-bar__title {
  color: #333;
  font-weight: 500;
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
  max-width: 75%;
  padding: 12px 16px;
  border-radius: 18px;
  font-size: 15px;
  line-height: 1.6;
}
.message-bubble.receive {
  background: #fff;
  border: 1px solid #e6e6e6;
  border-bottom-left-radius: 6px;
}
.message-bubble.send {
  background: #ff9f00;
  color: #fff;
  border-bottom-right-radius: 6px;
}

.loading-tip {
  text-align: center;
  color: #999;
  font-size: 13px;
  padding: 10px 0;
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