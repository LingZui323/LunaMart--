<template>
  <div class="ai-log-page">
    <!-- 顶部导航 -->
    <van-nav-bar
      title="AI 内容审核日志"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="nav-bar"
    />

    <!-- 日志列表 -->
    <div class="list-container">
      <!-- 日志卡片 -->
      <div
        class="log-card"
        v-for="log in logList"
        :key="log.id"
      >
        <!-- 顶部：状态 + 时间 -->
        <div class="card-header">
          <van-tag
            :type="log.result === 'PASS' ? 'success' : 'danger'"
            size="medium"
          >
            {{ log.result === 'PASS' ? '审核通过' : '审核拒绝' }}
          </van-tag>
          <span class="time">{{ formatTime(log.createTime) }}</span>
        </div>

        <!-- 内容 -->
        <div class="card-content">
          <div class="row">
            <label>目标类型：</label>
            <span>{{ log.targetType === 'GOODS' ? '商品' : '未知' }}</span>
          </div>
          <div class="row">
            <label>目标ID：</label>
            <span>{{ log.targetId }}</span>
          </div>
          <div class="row content">
            <label>审核内容：</label>
            <span>{{ log.content || '无' }}</span>
          </div>
          <div class="row reason">
            <label>审核原因：</label>
            <span>{{ log.reason || '无' }}</span>
          </div>
        </div>
      </div>

      <!-- 空数据 -->
      <van-empty
        v-if="logList.length === 0 && finished"
        description="暂无审核日志"
        class="empty"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()

// 列表数据
const logList = ref([])
const finished = ref(false)
const page = ref(1)
const size = ref(20)

// 加载 AI 审核日志
const loadAiAuditLog = async () => {
  try {
    const res = await fetch(`/api/ai/audit/log/list?page=${page.value}&size=${size.value}`, {
      headers: {
        Authorization: 'Bearer ' + userStore.token
      }
    })
    const data = await res.json()
    
    if (data.code === 200) {
      const records = data.data.records || []
      logList.value = records
      finished.value = true
    } else {
      showToast(data.msg || '加载失败')
    }
  } catch (e) {
    showToast('网络异常')
    finished.value = true
  }
}

// 时间格式化
const formatTime = (t) => {
  if (!t) return '无'
  return new Date(t).toLocaleString()
}

onMounted(() => {
  loadAiAuditLog()
})
</script>

<style scoped>
.ai-log-page {
  background: #f7f8fa;
  min-height: 100vh;
  padding-bottom: 20px;
}

.nav-bar {
  background: #4e5969;
  color: #fff;
}

:deep(.van-nav-bar__title) {
  color: #fff;
}

.list-container {
  padding: 60px 12px 10px;
}

/* 日志卡片 */
.log-card {
  background: #fff;
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.time {
  font-size: 12px;
  color: #999;
}

.card-content {
  font-size: 14px;
  color: #333;
}

.row {
  margin-bottom: 6px;
  display: flex;
  align-items: flex-start;
}

label {
  color: #666;
  min-width: 72px;
}

.content {
  color: #333;
  font-weight: 500;
}

.reason {
  color: #666;
  font-size: 13px;
}

.empty {
  margin-top: 60px;
}
</style>