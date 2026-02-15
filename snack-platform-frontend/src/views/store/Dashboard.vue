<template>
  <div class="store-dashboard">
    <h2>门店概览</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #409eff;">
            <el-icon><ShoppingCart /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ dashboard.todayOrders }}</div>
            <div class="stat-label">今日订单</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #67c23a;">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ dashboard.todaySales?.toFixed(2) || '0.00' }}</div>
            <div class="stat-label">今日销售额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #e6a23c;">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ dashboard.pendingOrders }}</div>
            <div class="stat-label">待处理订单</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #f56c6c;">
            <el-icon><Goods /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ dashboard.lowStockCount }}</div>
            <div class="stat-label">库存预警</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 订单状态分布图 + 热销商品图 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="chart-card" v-loading="chartLoading">
          <template #header>
            <div class="card-header">
              <span>订单状态分布（本月）</span>
            </div>
          </template>
          <div ref="orderStatusChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" v-loading="chartLoading">
          <template #header>
            <div class="card-header">
              <span>热销商品 TOP5（本月）</span>
            </div>
          </template>
          <div ref="topProductsChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 待处理订单 -->
      <el-col :span="12">
        <el-card class="order-card">
          <template #header>
            <div class="card-header">
              <span>待处理订单</span>
              <el-button type="primary" text @click="goToOrders">查看全部</el-button>
            </div>
          </template>
          <el-table :data="pendingOrders" style="width: 100%" max-height="300">
            <el-table-column prop="orderNo" label="订单号" width="150" />
            <el-table-column prop="totalAmount" label="金额" width="100">
              <template #default="{ row }">
                ¥{{ row.totalAmount?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'PAID'"
                  type="primary"
                  size="small"
                  @click="confirmOrder(row)"
                >
                  确认
                </el-button>
                <el-button
                  v-if="row.status === 'CONFIRMED'"
                  type="success"
                  size="small"
                  @click="readyOrder(row)"
                >
                  备货完成
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="pendingOrders.length === 0" description="暂无待处理订单" />
        </el-card>
      </el-col>

      <!-- 库存预警 -->
      <el-col :span="12">
        <el-card class="stock-card">
          <template #header>
            <div class="card-header">
              <span>库存预警</span>
              <el-button type="primary" text @click="goToSkus">管理库存</el-button>
            </div>
          </template>
          <el-table :data="lowStockSkus" style="width: 100%" max-height="300">
            <el-table-column prop="product.name" label="商品名称" />
            <el-table-column prop="stock" label="当前库存" width="100">
              <template #default="{ row }">
                <span :class="{ 'low-stock': row.stock <= 5 }">{{ row.stock }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="100">
              <template #default="{ row }">
                ¥{{ row.price?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="editStock(row)">
                  补货
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="lowStockSkus.length === 0" description="暂无库存预警" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 补货对话框 -->
    <el-dialog v-model="stockDialogVisible" title="补货" width="400px">
      <el-form :model="stockForm" label-width="80px">
        <el-form-item label="商品">
          <span>{{ currentSku?.product?.name }}</span>
        </el-form-item>
        <el-form-item label="当前库存">
          <span>{{ currentSku?.stock }}</span>
        </el-form-item>
        <el-form-item label="补货数量">
          <el-input-number v-model="stockForm.addStock" :min="1" :max="9999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStock" :loading="submittingStock">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ShoppingCart, Money, Clock, Goods } from '@element-plus/icons-vue'
import { getStoreDashboard } from '@/api/report'
import { getStoreOrders, confirmOrder as confirmOrderApi, readyOrder as readyOrderApi } from '@/api/order'
import { getMyStoreSkus, updateStoreSku } from '@/api/sku'
import { ElMessage, ElNotification } from 'element-plus'
import echarts from '@/utils/echarts'
import { getStatusType, getStatusText } from '@/utils/constants'
import { useWebSocket } from '@/composables/useWebSocket'
import { useUserStore } from '@/stores'

const router = useRouter()
const userStore = useUserStore()

// ========== WebSocket 实时新订单通知 ==========
const { connected, lastMessage, connect: wsConnect } = useWebSocket(
  userStore.userInfo?.id,
  userStore.storeId
)

// 监听 WebSocket 收到的新消息，弹出通知并自动刷新数据
watch(lastMessage, (msg) => {
  if (!msg) return
  // 弹出 Element Plus 通知提醒门店管理员
  ElNotification({
    title: '新订单通知',
    message: msg.message || msg.content || '您有一笔新订单，请及时处理',
    type: 'success',
    duration: 5000
  })
  // 自动刷新仪表盘统计数据和待处理订单列表
  fetchDashboard()
  fetchPendingOrders()
})

const dashboard = reactive({
  todayOrders: 0,
  todaySales: 0,
  pendingOrders: 0,
  lowStockCount: 0
})

const pendingOrders = ref([])
const lowStockSkus = ref([])
const stockDialogVisible = ref(false)
const currentSku = ref(null)
const submittingStock = ref(false)
const chartLoading = ref(false)
const stockForm = reactive({
  addStock: 10
})

// ECharts 实例引用
const orderStatusChart = ref(null)
const topProductsChart = ref(null)
let orderStatusChartInstance = null
let topProductsChartInstance = null

const goToOrders = () => {
  router.push('/store/orders')
}

const goToSkus = () => {
  router.push('/store/skus')
}

const confirmOrder = async (order) => {
  try {
    await confirmOrderApi(order.id)
    ElMessage.success('订单已确认')
    fetchPendingOrders()
    fetchDashboard()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const readyOrder = async (order) => {
  try {
    await readyOrderApi(order.id)
    ElMessage.success('备货完成，等待顾客取货')
    fetchPendingOrders()
    fetchDashboard()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const editStock = (sku) => {
  currentSku.value = sku
  stockForm.addStock = 10
  stockDialogVisible.value = true
}

const submitStock = async () => {
  submittingStock.value = true
  try {
    await updateStoreSku(currentSku.value.id, {
      stock: currentSku.value.stock + stockForm.addStock
    })
    ElMessage.success('补货成功')
    stockDialogVisible.value = false
    fetchLowStockSkus()
    fetchDashboard()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submittingStock.value = false
  }
}

const fetchDashboard = async () => {
  try {
    const res = await getStoreDashboard()
    Object.assign(dashboard, res.data)
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  }
}

const fetchPendingOrders = async () => {
  try {
    const res = await getStoreOrders({ status: 'PAID,CONFIRMED', size: 5 })
    pendingOrders.value = res.data.records || res.data || []
  } catch (error) {
    console.error('获取待处理订单失败:', error)
  }
}

const fetchLowStockSkus = async () => {
  try {
    const res = await getMyStoreSkus({ lowStock: true, size: 5 })
    lowStockSkus.value = res.data.records || res.data || []
  } catch (error) {
    console.error('获取库存预警失败:', error)
  }
}

// 初始化订单状态分布图（饼图）
const initOrderStatusChart = () => {
  if (!orderStatusChart.value) return

  orderStatusChartInstance = echarts.init(orderStatusChart.value)

  // 使用真实 dashboard 数据填充订单状态分布
  const pendingAccept = dashboard.pendingAcceptCount || dashboard.pendingOrders || 0
  const pendingPickup = dashboard.pendingPickupCount || 0
  const todayTotal = dashboard.todayOrderCount || dashboard.todayOrders || 0
  // 已完成数 = 总数 - 待处理数（近似估算）
  const completed = Math.max(0, todayTotal - pendingAccept - pendingPickup)

  const statusData = [
    { value: pendingAccept, name: '待确认' },
    { value: pendingPickup, name: '待取货' },
    { value: completed, name: '已完成' }
  ].filter(item => item.value > 0)

  // 如果没有任何数据，显示提示
  if (statusData.length === 0) {
    statusData.push({ value: 1, name: '暂无订单' })
  }
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}单 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '10%',
      top: 'center'
    },
    series: [
      {
        name: '订单数量',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: statusData,
        color: ['#e6a23c', '#409eff', '#67c23a', '#f56c6c', '#909399']
      }
    ]
  }
  
  orderStatusChartInstance.setOption(option)
}

// 初始化热销商品图（横向柱状图）
const initTopProductsChart = () => {
  if (!topProductsChart.value) return
  
  topProductsChartInstance = echarts.init(topProductsChart.value)
  
  // 模拟数据：热销商品 TOP5
  const productNames = [
    '乐事薯片原味',
    '奥利奥饼干',
    '旺旺雪饼',
    '三只松鼠坚果',
    '德芙巧克力'
  ]
  const salesCounts = [156, 142, 128, 115, 98]
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '销量（件）'
    },
    yAxis: {
      type: 'category',
      data: productNames.reverse(),
      axisLabel: {
        interval: 0
      }
    },
    series: [
      {
        name: '销量',
        type: 'bar',
        barWidth: '60%',
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 1,
            y2: 0,
            colorStops: [
              { offset: 0, color: '#409eff' },
              { offset: 1, color: '#67c23a' }
            ]
          },
          borderRadius: [0, 10, 10, 0]
        },
        data: salesCounts.reverse()
      }
    ]
  }
  
  topProductsChartInstance.setOption(option)
}

// 初始化所有图表
const initCharts = () => {
  chartLoading.value = true
  setTimeout(() => {
    initOrderStatusChart()
    initTopProductsChart()
    chartLoading.value = false
  }, 500)
}

// 响应式处理
const handleResize = () => {
  orderStatusChartInstance?.resize()
  topProductsChartInstance?.resize()
}

onMounted(() => {
  fetchDashboard()
  fetchPendingOrders()
  fetchLowStockSkus()
  initCharts()
  window.addEventListener('resize', handleResize)
  // 建立 WebSocket 连接，接收实时新订单通知
  wsConnect()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  
  // 销毁图表实例
  orderStatusChartInstance?.dispose()
  topProductsChartInstance?.dispose()
})
</script>

<style scoped>
.store-dashboard h2 {
  margin-bottom: 20px;
  color: #333;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon .el-icon {
  font-size: 28px;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-card,
.stock-card {
  margin-bottom: 20px;
}

.low-stock {
  color: #f56c6c;
  font-weight: bold;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 400px;
}
</style>