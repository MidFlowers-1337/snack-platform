<template>
  <div class="admin-dashboard">
    <h2>平台概览</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #409eff;">
            <el-icon><Shop /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ dashboard.totalStores }}</div>
            <div class="stat-label">门店总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #67c23a;">
            <el-icon><Goods /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ dashboard.totalProducts }}</div>
            <div class="stat-label">商品总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #e6a23c;">
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
          <div class="stat-icon" style="background: #f56c6c;">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ dashboard.todaySales?.toFixed(2) || '0.00' }}</div>
            <div class="stat-label">今日销售额</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 门店销售排行 -->
      <el-col :span="12">
        <el-card class="rank-card">
          <template #header>
            <div class="card-header">
              <span>门店销售排行（本月）</span>
              <el-button type="primary" text @click="goToStores">查看全部</el-button>
            </div>
          </template>
          <el-table :data="storeRanking" style="width: 100%">
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="name" label="门店名称" />
            <el-table-column prop="orderCount" label="订单数" width="100" align="center" />
            <el-table-column prop="salesAmount" label="销售额" width="120" align="center">
              <template #default="{ row }">
                <span class="price">¥{{ row.salesAmount?.toFixed(2) }}</span>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="storeRanking.length === 0" description="暂无数据" />
        </el-card>
      </el-col>

      <!-- 商品销售排行 -->
      <el-col :span="12">
        <el-card class="rank-card">
          <template #header>
            <div class="card-header">
              <span>商品销售排行（本月）</span>
              <el-button type="primary" text @click="goToProducts">查看全部</el-button>
            </div>
          </template>
          <el-table :data="productRanking" style="width: 100%">
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="salesCount" label="销量" width="100" align="center" />
            <el-table-column prop="salesAmount" label="销售额" width="120" align="center">
              <template #default="{ row }">
                <span class="price">¥{{ row.salesAmount?.toFixed(2) }}</span>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="productRanking.length === 0" description="暂无数据" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 最近订单 -->
      <el-col :span="16">
        <el-card class="recent-orders-card">
          <template #header>
            <span>最近订单</span>
          </template>
          <el-table :data="recentOrders" style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="store.name" label="门店" width="150" />
            <el-table-column prop="totalAmount" label="金额" width="100" align="center">
              <template #default="{ row }">
                ¥{{ row.totalAmount?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="下单时间" width="160">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="recentOrders.length === 0" description="暂无订单" />
        </el-card>
      </el-col>

      <!-- 快捷操作 -->
      <el-col :span="8">
        <el-card class="quick-actions-card">
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="action-grid">
            <div class="action-item" @click="goToCategories">
              <el-icon><Grid /></el-icon>
              <span>分类管理</span>
            </div>
            <div class="action-item" @click="goToProducts">
              <el-icon><Goods /></el-icon>
              <span>商品管理</span>
            </div>
            <div class="action-item" @click="goToStores">
              <el-icon><Shop /></el-icon>
              <span>门店管理</span>
            </div>
            <div class="action-item" @click="goToReport">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据报表</span>
            </div>
          </div>
        </el-card>

        <!-- 系统信息 -->
        <el-card class="system-info-card">
          <template #header>
            <span>系统信息</span>
          </template>
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
            <el-descriptions-item label="运行环境">Production</el-descriptions-item>
            <el-descriptions-item label="当前时间">{{ currentTime }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Shop, Goods, ShoppingCart, Money, Grid, DataAnalysis } from '@element-plus/icons-vue'
import { getAdminDashboard, getSalesReport } from '@/api/report'
import { ElMessage } from 'element-plus'

const router = useRouter()

const dashboard = reactive({
  totalStores: 0,
  totalProducts: 0,
  todayOrders: 0,
  todaySales: 0
})

const storeRanking = ref([])
const productRanking = ref([])
const recentOrders = ref([])
const currentTime = ref('')

let timeInterval = null

const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'PAID': 'primary',
    'CONFIRMED': 'primary',
    'READY': 'success',
    'COMPLETED': 'success',
    'CANCELLED': 'info',
    'REJECTED': 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'PENDING': '待支付',
    'PAID': '已支付',
    'CONFIRMED': '已确认',
    'READY': '待取货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'REJECTED': '已拒绝'
  }
  return texts[status] || status
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const updateTime = () => {
  currentTime.value = new Date().toLocaleString('zh-CN')
}

const goToCategories = () => {
  router.push('/admin/categories')
}

const goToProducts = () => {
  router.push('/admin/products')
}

const goToStores = () => {
  router.push('/admin/stores')
}

const goToReport = () => {
  router.push('/admin/report')
}

const fetchDashboard = async () => {
  try {
    const res = await getAdminDashboard()
    Object.assign(dashboard, res.data)
    storeRanking.value = res.data.storeRanking || []
    productRanking.value = res.data.productRanking || []
    recentOrders.value = res.data.recentOrders || []
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  }
}

onMounted(() => {
  fetchDashboard()
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style scoped>
.admin-dashboard h2 {
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

.rank-card,
.recent-orders-card,
.quick-actions-card,
.system-info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-item:hover {
  background: #ecf5ff;
  color: #409eff;
}

.action-item .el-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.action-item span {
  font-size: 14px;
}
</style>