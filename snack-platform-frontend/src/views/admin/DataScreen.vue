<template>
  <div class="data-screen" :class="{ fullscreen: isFullscreen }">
    <!-- 顶部标题栏 -->
    <div class="screen-header">
      <h1>零食连锁平台 · 运营数据大屏</h1>
      <div class="header-right">
        <span class="current-time">{{ currentTime }}</span>
        <el-button type="primary" text @click="toggleFullscreen">
          <el-icon><FullScreen /></el-icon>
          {{ isFullscreen ? '退出全屏' : '全屏' }}
        </el-button>
        <el-button type="primary" text @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 核心指标卡片 -->
    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-card-value">{{ stats.todayOrders || 0 }}</div>
        <div class="stat-card-label">今日订单</div>
        <div class="stat-card-trend" :class="orderTrendClass">
          {{ orderTrendText }}
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card-value">¥{{ formatMoney(stats.todaySales) }}</div>
        <div class="stat-card-label">今日营收</div>
        <div class="stat-card-trend" :class="salesTrendClass">
          {{ salesTrendText }}
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card-value">{{ stats.activeStores || 0 }}/{{ stats.totalStores || 0 }}</div>
        <div class="stat-card-label">活跃门店</div>
        <div class="stat-card-sub">营业中</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-value">{{ stats.monthOrders || 0 }}</div>
        <div class="stat-card-label">本月订单</div>
        <div class="stat-card-sub">¥{{ formatMoney(stats.monthSales) }}</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-row">
      <!-- 左侧：7天销售趋势 -->
      <div class="chart-box chart-wide">
        <div class="chart-title">销售趋势（近7天）</div>
        <div ref="trendChartRef" class="chart-canvas"></div>
      </div>
      <!-- 右侧：门店排行 -->
      <div class="chart-box">
        <div class="chart-title">门店销售排行（本月）</div>
        <div ref="rankChartRef" class="chart-canvas"></div>
      </div>
    </div>

    <!-- 底部区域 -->
    <div class="bottom-row">
      <!-- 实时订单流水 -->
      <div class="order-flow-box">
        <div class="chart-title">实时订单流水</div>
        <div class="order-list" ref="orderListRef">
          <div
            v-for="(order, idx) in recentOrders"
            :key="idx"
            class="order-item"
          >
            <span class="order-time">{{ formatOrderTime(order.createTime) }}</span>
            <span class="order-store">{{ order.storeName }}</span>
            <span class="order-amount">¥{{ Number(order.amount || 0).toFixed(2) }}</span>
            <span class="order-status" :class="'status-' + order.status">
              {{ order.statusText }}
            </span>
          </div>
          <div v-if="recentOrders.length === 0" class="order-empty">暂无订单</div>
        </div>
      </div>
    </div>

    <!-- 自动刷新倒计时 -->
    <div class="refresh-indicator">
      下次刷新：{{ countdown }}s
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { FullScreen, Refresh } from '@element-plus/icons-vue'
import { getRealtimeStats } from '@/api/report'
import echarts from '@/utils/echarts'

// ========== 数据状态 ==========
const stats = ref({})
const recentOrders = ref([])
const currentTime = ref('')
const isFullscreen = ref(false)
const countdown = ref(30)

// ========== 图表引用 ==========
const trendChartRef = ref(null)
const rankChartRef = ref(null)
let trendChart = null
let rankChart = null
let refreshTimer = null
let timeTimer = null
let countdownTimer = null

// ========== 工具函数 ==========
const formatMoney = (val) => {
  return Number(val || 0).toFixed(2)
}

const formatOrderTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// ========== 环比计算 ==========
const calcTrend = (today, yesterday) => {
  if (!yesterday || yesterday === 0) return today > 0 ? 100 : 0
  return (((today - yesterday) / yesterday) * 100).toFixed(1)
}

const orderTrendClass = computed(() => {
  const t = calcTrend(stats.value.todayOrders, stats.value.yesterdayOrders)
  return t >= 0 ? 'trend-up' : 'trend-down'
})

const orderTrendText = computed(() => {
  const t = calcTrend(stats.value.todayOrders, stats.value.yesterdayOrders)
  return t >= 0 ? `↑${t}%` : `↓${Math.abs(t)}%`
})

const salesTrendClass = computed(() => {
  const t = calcTrend(stats.value.todaySales, stats.value.yesterdaySales)
  return t >= 0 ? 'trend-up' : 'trend-down'
})

const salesTrendText = computed(() => {
  const t = calcTrend(stats.value.todaySales, stats.value.yesterdaySales)
  return t >= 0 ? `↑${t}%` : `↓${Math.abs(t)}%`
})

// ========== 全屏切换 ==========
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

// ========== 数据获取 ==========
const refreshData = async () => {
  try {
    const res = await getRealtimeStats()
    const data = res.data || {}
    stats.value = data
    recentOrders.value = data.recentOrders || []
    await nextTick()
    renderTrendChart(data.salesTrend || [])
    renderRankChart(data.storeRanking || [])
    countdown.value = 30
  } catch (e) {
    console.error('获取大屏数据失败:', e)
  }
}

// ========== 销售趋势折线图 ==========
const renderTrendChart = (trendData) => {
  if (!trendChartRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const dates = trendData.map(d => {
    const dt = new Date(d.date)
    return `${dt.getMonth() + 1}/${dt.getDate()}`
  })
  const salesArr = trendData.map(d => Number(d.sales || 0).toFixed(2))
  const ordersArr = trendData.map(d => d.orderCount || 0)

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['销售额', '订单数'], textStyle: { color: '#aedcfa' }, top: 5 },
    grid: { left: 50, right: 30, top: 40, bottom: 30 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#3a5f7f' } },
      axisLabel: { color: '#aedcfa' }
    },
    yAxis: [
      {
        type: 'value', name: '销售额',
        nameTextStyle: { color: '#aedcfa' },
        axisLine: { lineStyle: { color: '#3a5f7f' } },
        axisLabel: { color: '#aedcfa' },
        splitLine: { lineStyle: { color: '#1a3a5c' } }
      },
      {
        type: 'value', name: '订单数',
        nameTextStyle: { color: '#aedcfa' },
        axisLine: { lineStyle: { color: '#3a5f7f' } },
        axisLabel: { color: '#aedcfa' },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '销售额', type: 'line', smooth: true, data: salesArr,
        itemStyle: { color: '#00d4ff' },
        areaStyle: {
          color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(0,212,255,0.3)' },
              { offset: 1, color: 'rgba(0,212,255,0.02)' }
            ]
          }
        }
      },
      {
        name: '订单数', type: 'bar', yAxisIndex: 1, data: ordersArr,
        itemStyle: { color: 'rgba(0,212,255,0.4)', borderRadius: [4, 4, 0, 0] },
        barWidth: '40%'
      }
    ]
  })
}

// ========== 门店排行横向柱状图 ==========
const renderRankChart = (ranking) => {
  if (!rankChartRef.value) return
  if (!rankChart) {
    rankChart = echarts.init(rankChartRef.value)
  }

  const names = ranking.map(r => r.name).reverse()
  const values = ranking.map(r => Number(r.salesAmount || 0)).reverse()

  rankChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 90, right: 30, top: 10, bottom: 20 },
    xAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#3a5f7f' } },
      axisLabel: { color: '#aedcfa' },
      splitLine: { lineStyle: { color: '#1a3a5c' } }
    },
    yAxis: {
      type: 'category', data: names,
      axisLine: { lineStyle: { color: '#3a5f7f' } },
      axisLabel: { color: '#aedcfa' }
    },
    series: [{
      type: 'bar', data: values, barWidth: '50%',
      itemStyle: {
        borderRadius: [0, 8, 8, 0],
        color: { type: 'linear', x: 0, y: 0, x2: 1, y2: 0,
          colorStops: [
            { offset: 0, color: '#0b4a7a' },
            { offset: 1, color: '#00d4ff' }
          ]
        }
      }
    }]
  })
}

// ========== 响应式 ==========
const handleResize = () => {
  trendChart?.resize()
  rankChart?.resize()
}

// ========== 时间更新 ==========
const updateTime = () => {
  currentTime.value = new Date().toLocaleString('zh-CN')
}

// ========== 生命周期 ==========
onMounted(() => {
  updateTime()
  timeTimer = setInterval(updateTime, 1000)
  refreshData()
  refreshTimer = setInterval(refreshData, 30000)
  countdownTimer = setInterval(() => {
    countdown.value = countdown.value > 0 ? countdown.value - 1 : 30
  }, 1000)
  window.addEventListener('resize', handleResize)
  document.addEventListener('fullscreenchange', () => {
    isFullscreen.value = !!document.fullscreenElement
  })
})

onUnmounted(() => {
  clearInterval(timeTimer)
  clearInterval(refreshTimer)
  clearInterval(countdownTimer)
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  rankChart?.dispose()
})
</script>

<style scoped>
.data-screen {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a1628 0%, #0d2137 50%, #0a1628 100%);
  color: #e0f0ff;
  padding: 16px 24px;
  box-sizing: border-box;
  font-family: 'Microsoft YaHei', sans-serif;
  overflow-y: auto;
}

.data-screen.fullscreen {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  z-index: 9999;
  padding: 16px 24px;
}

/* 顶部标题 */
.screen-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  border-bottom: 1px solid #1a3a5c;
  padding-bottom: 12px;
}

.screen-header h1 {
  margin: 0;
  font-size: 22px;
  background: linear-gradient(90deg, #00d4ff, #7ee8fa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 3px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.current-time {
  color: #7eb8dc;
  font-size: 14px;
}

/* 指标卡片 */
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  background: rgba(10, 40, 70, 0.8);
  border: 1px solid #1a4a70;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  transition: border-color 0.3s;
}

.stat-card:hover {
  border-color: #00d4ff;
}

.stat-card-value {
  font-size: 28px;
  font-weight: bold;
  color: #00d4ff;
  margin-bottom: 6px;
}

.stat-card-label {
  font-size: 14px;
  color: #7eb8dc;
  margin-bottom: 4px;
}

.stat-card-trend {
  font-size: 13px;
  font-weight: bold;
}

.stat-card-sub {
  font-size: 12px;
  color: #5a8aaa;
}

.trend-up { color: #67c23a; }
.trend-down { color: #f56c6c; }

/* 图表区域 */
.chart-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.chart-box {
  background: rgba(10, 40, 70, 0.8);
  border: 1px solid #1a4a70;
  border-radius: 8px;
  padding: 14px;
}

.chart-title {
  font-size: 15px;
  color: #aedcfa;
  margin-bottom: 8px;
  padding-left: 8px;
  border-left: 3px solid #00d4ff;
}

.chart-canvas {
  width: 100%;
  height: 280px;
}

/* 底部：订单流水 */
.bottom-row {
  margin-bottom: 16px;
}

.order-flow-box {
  background: rgba(10, 40, 70, 0.8);
  border: 1px solid #1a4a70;
  border-radius: 8px;
  padding: 14px;
}

.order-list {
  max-height: 220px;
  overflow-y: auto;
}

.order-list::-webkit-scrollbar {
  width: 4px;
}
.order-list::-webkit-scrollbar-thumb {
  background: #1a4a70;
  border-radius: 2px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 12px;
  border-bottom: 1px solid #0d2a45;
  font-size: 13px;
}

.order-item:last-child {
  border-bottom: none;
}

.order-time { color: #5a8aaa; width: 50px; }
.order-store { color: #aedcfa; flex: 1; }
.order-amount { color: #00d4ff; font-weight: bold; width: 90px; text-align: right; }
.order-status { width: 60px; text-align: center; font-size: 12px; border-radius: 3px; padding: 2px 6px; }

.status-0 { background: #5a5a5a; color: #ccc; }
.status-1 { background: #e6a23c33; color: #e6a23c; }
.status-2 { background: #409eff33; color: #409eff; }
.status-3 { background: #67c23a33; color: #67c23a; }
.status-4 { background: #67c23a33; color: #67c23a; }
.status-5 { background: #f56c6c33; color: #f56c6c; }

.order-empty {
  text-align: center;
  color: #5a8aaa;
  padding: 30px 0;
}

/* 刷新倒计时 */
.refresh-indicator {
  position: fixed;
  bottom: 12px;
  right: 24px;
  font-size: 12px;
  color: #3a6a8a;
}

/* 响应式 */
@media (max-width: 1024px) {
  .stat-row { grid-template-columns: repeat(2, 1fr); }
  .chart-row { grid-template-columns: 1fr; }
}

@media (max-width: 768px) {
  .stat-row { grid-template-columns: 1fr; }
  .screen-header h1 { font-size: 16px; letter-spacing: 1px; }
  .stat-card-value { font-size: 22px; }
}
</style>
