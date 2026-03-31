<template>
  <div class="dashboard-page">
    <van-nav-bar
      title="数据看板"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <div class="container" style="margin-top:46px; padding-bottom:30px;">

      <!-- 日期切换 -->
      <van-cell-group inset class="filter-box">
        <van-field
          readonly
          clickable
          label="统计日期"
          value="近7天"
          right-icon="arrow-down"
          @click="showDatePicker = true"
        />
      </van-cell-group>

      <!-- 核心数据卡片 -->
      <div class="card-row">
        <div class="stat-card">
          <div class="label">今日订单</div>
          <div class="value">{{ today.orderCount }}</div>
          <div class="change" :class="today.orderChange > 0 ? 'up' : 'down'">
            <van-icon :name="today.orderChange > 0 ? 'arrow-up' : 'arrow-down'" size="12"/>
            {{ Math.abs(today.orderChange) }}%
          </div>
        </div>

        <div class="stat-card">
          <div class="label">今日销售额</div>
          <div class="value">¥{{ today.amount }}</div>
          <div class="change" :class="today.amountChange > 0 ? 'up' : 'down'">
            <van-icon :name="today.amountChange > 0 ? 'arrow-up' : 'arrow-down'" size="12"/>
            {{ Math.abs(today.amountChange) }}%
          </div>
        </div>
      </div>

      <div class="card-row">
        <div class="stat-card">
          <div class="label">总订单</div>
          <div class="value">{{ total.orderCount }}</div>
        </div>
        <div class="stat-card">
          <div class="label">完成率</div>
          <div class="value">{{ total.completeRate }}%</div>
          <div class="change" :class="total.rateChange > 0 ? 'up' : 'down'">
            <van-icon :name="total.rateChange > 0 ? 'arrow-up' : 'arrow-down'" size="12"/>
            {{ Math.abs(total.rateChange) }}%
          </div>
        </div>
      </div>

      <!-- 每日订单柱状图 -->
      <van-cell-group inset class="chart-box">
        <div class="chart-title">近7天订单量</div>
        <div class="bar-chart">
          <div class="bar-wrapper" v-for="(item, idx) in chartData" :key="idx">
            <div class="bar" :style="{ height: item.percent + '%', backgroundColor: '#FF9F00' }"></div>
            <div class="day">{{ item.day }}</div>
          </div>
        </div>
      </van-cell-group>

      <!-- 订单状态 -->
      <van-cell-group inset class="status-box">
        <div class="chart-title">订单状态</div>
        <div class="status-row">
          <div class="status-item">
            <div class="dot paid"></div>
            <span>已支付</span>
            <span class="num">{{ orderStatus.paid }}</span>
          </div>
          <div class="status-item">
            <div class="dot delivered"></div>
            <span>已发货</span>
            <span class="num">{{ orderStatus.delivered }}</span>
          </div>
          <div class="status-item">
            <div class="dot received"></div>
            <span>已完成</span>
            <span class="num">{{ orderStatus.received }}</span>
          </div>
          <div class="status-item">
            <div class="dot cancel"></div>
            <span>已取消</span>
            <span class="num">{{ orderStatus.cancel }}</span>
          </div>
        </div>
      </van-cell-group>

    </div>

    <van-popup v-model:show="showDatePicker" position="bottom">
      <van-picker
        :columns="dateColumns"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const showDatePicker = ref(false)
const dateColumns = ref([
  { text: '近7天', value: '7' },
  { text: '近30天', value: '30' }
])

// 今日数据
const today = ref({
  orderCount: 28,
  orderChange: 12,
  amount: 1680,
  amountChange: 8
})

// 总计
const total = ref({
  orderCount: 196,
  completeRate: 86,
  rateChange: 5
})

// 订单状态
const orderStatus = ref({
  paid: 18,
  delivered: 32,
  received: 126,
  cancel: 20
})

// 柱状图数据
const chartData = ref([
  { day: '20', count: 12 },
  { day: '21', count: 19 },
  { day: '22', count: 15 },
  { day: '23', count: 25 },
  { day: '24', count: 22 },
  { day: '25', count: 30 },
  { day: '26', count: 28 }
])

// 计算高度百分比
chartData.value.forEach(item => {
  item.percent = (item.count / 30) * 100
})

const onDateConfirm = ({ selectedOptions }) => {
  showDatePicker.value = false
}
</script>

<style scoped>
.dashboard-page {
  background: #fff9e6;
  min-height: 100vh;
}
.moon-nav-bar {
  background: #FFE181;
}
.container {
  padding: 10px;
}

/* 卡片行 */
.card-row {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}
.stat-card {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 15px 12px;
}
.label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}
.value {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}
.change {
  font-size: 11px;
  display: flex;
  align-items: center;
  gap: 2px;
}
.up { color: #07c160; }
.down { color: #ff4444; }

/* 图表 */
.chart-box {
  padding: 15px;
  border-radius: 12px;
  margin-bottom: 10px;
}
.chart-title {
  font-size: 15px;
  font-weight: bold;
  margin-bottom: 15px;
}
.bar-chart {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  height: 160px;
}
.bar-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.bar {
  width: 22px;
  border-radius: 4px 4px 0 0;
  margin-bottom: 6px;
}
.day {
  font-size: 11px;
  color: #666;
}

/* 订单状态 */
.status-box {
  padding: 15px;
  border-radius: 12px;
}
.status-row {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}
.status-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}
.dot {
  width: 8px; height: 8px; border-radius: 50%;
}
.dot.paid { background: #1989fa; }
.dot.delivered { background: #ff9f00; }
.dot.received { background: #07c160; }
.dot.cancel { background: #999; }
.num { margin-left: 4px; color: #666; }

.filter-box {
  border-radius: 12px;
  margin-bottom: 10px;
}
</style>