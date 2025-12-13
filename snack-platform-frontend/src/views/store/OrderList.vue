<template>
  <div class="store-order-list">
    <h2>订单管理</h2>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="搜索订单号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="待支付" value="PENDING" />
            <el-option label="待确认" value="PAID" />
            <el-option label="备货中" value="CONFIRMED" />
            <el-option label="待取货" value="READY" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单统计 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="4">
        <div class="stat-item" :class="{ active: searchForm.status === '' }" @click="filterByStatus('')">
          <div class="stat-value">{{ orderStats.all }}</div>
          <div class="stat-label">全部</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-item" :class="{ active: searchForm.status === 'PAID' }" @click="filterByStatus('PAID')">
          <div class="stat-value">{{ orderStats.paid }}</div>
          <div class="stat-label">待确认</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-item" :class="{ active: searchForm.status === 'CONFIRMED' }" @click="filterByStatus('CONFIRMED')">
          <div class="stat-value">{{ orderStats.confirmed }}</div>
          <div class="stat-label">备货中</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-item" :class="{ active: searchForm.status === 'READY' }" @click="filterByStatus('READY')">
          <div class="stat-value">{{ orderStats.ready }}</div>
          <div class="stat-label">待取货</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-item" :class="{ active: searchForm.status === 'COMPLETED' }" @click="filterByStatus('COMPLETED')">
          <div class="stat-value">{{ orderStats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-item" :class="{ active: searchForm.status === 'CANCELLED' }" @click="filterByStatus('CANCELLED')">
          <div class="stat-value">{{ orderStats.cancelled }}</div>
          <div class="stat-label">已取消</div>
        </div>
      </el-col>
    </el-row>

    <!-- 订单列表 -->
    <el-card>
      <el-table :data="orders" v-loading="loading" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">
            <div class="order-items">
              <span v-for="(item, index) in row.items?.slice(0, 2)" :key="item.id">
                {{ item.productName }} x{{ item.quantity }}
                <span v-if="index < Math.min(row.items.length, 2) - 1">、</span>
              </span>
              <span v-if="row.items?.length > 2">等{{ row.items.length }}件商品</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="100" align="center">
          <template #default="{ row }">
            <span class="price">¥{{ row.totalAmount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pickupCode" label="取货码" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.pickupCode" class="pickup-code">{{ row.pickupCode }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="viewOrder(row)">
              详情
            </el-button>
            <el-button
              v-if="row.status === 'PAID'"
              type="success"
              size="small"
              @click="confirmOrder(row)"
            >
              确认
            </el-button>
            <el-button
              v-if="row.status === 'PAID'"
              type="danger"
              size="small"
              @click="rejectOrder(row)"
            >
              拒绝
            </el-button>
            <el-button
              v-if="row.status === 'CONFIRMED'"
              type="warning"
              size="small"
              @click="readyOrder(row)"
            >
              备货完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentOrder.status)">
            {{ getStatusText(currentOrder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ formatTime(currentOrder.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ currentOrder.payTime ? formatTime(currentOrder.payTime) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="取货码" v-if="currentOrder.pickupCode">
          <span class="pickup-code large">{{ currentOrder.pickupCode }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
      </el-descriptions>

      <h4>商品列表</h4>
      <el-table :data="currentOrder.items" style="width: 100%">
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">
            ¥{{ row.price?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="小计" width="100">
          <template #default="{ row }">
            ¥{{ (row.price * row.quantity).toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="order-total">
        <span>订单总额：</span>
        <span class="total-price">¥{{ currentOrder.totalAmount?.toFixed(2) }}</span>
      </div>
    </el-dialog>

    <!-- 拒绝订单对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝订单" width="400px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            placeholder="请输入拒绝原因"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReject" :loading="rejecting">
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  getStoreOrders,
  confirmOrder as confirmOrderApi,
  rejectOrder as rejectOrderApi,
  readyOrder as readyOrderApi
} from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const orders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  orderNo: '',
  status: '',
  dateRange: []
})

const orderStats = reactive({
  all: 0,
  paid: 0,
  confirmed: 0,
  ready: 0,
  completed: 0,
  cancelled: 0
})

const detailDialogVisible = ref(false)
const currentOrder = ref({})

const rejectDialogVisible = ref(false)
const rejecting = ref(false)
const rejectForm = reactive({
  reason: ''
})

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
    'PAID': '待确认',
    'CONFIRMED': '备货中',
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

const filterByStatus = (status) => {
  searchForm.status = status
  handleSearch()
}

const handleSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

const resetSearch = () => {
  searchForm.orderNo = ''
  searchForm.status = ''
  searchForm.dateRange = []
  handleSearch()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchOrders()
}

const handlePageChange = () => {
  fetchOrders()
}

const viewOrder = (order) => {
  currentOrder.value = order
  detailDialogVisible.value = true
}

const confirmOrder = async (order) => {
  ElMessageBox.confirm(
    '确定要确认此订单吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      await confirmOrderApi(order.id)
      ElMessage.success('订单已确认')
      fetchOrders()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }).catch(() => {})
}

const rejectOrder = (order) => {
  currentOrder.value = order
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const submitReject = async () => {
  if (!rejectForm.reason) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  rejecting.value = true
  try {
    await rejectOrderApi(currentOrder.value.id, { reason: rejectForm.reason })
    ElMessage.success('订单已拒绝')
    rejectDialogVisible.value = false
    fetchOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    rejecting.value = false
  }
}

const readyOrder = async (order) => {
  ElMessageBox.confirm(
    '确定备货已完成，通知顾客取货？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      await readyOrderApi(order.id)
      ElMessage.success('已通知顾客取货')
      fetchOrders()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }).catch(() => {})
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      orderNo: searchForm.orderNo,
      status: searchForm.status
    }
    
    if (searchForm.dateRange?.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    const res = await getStoreOrders(params)
    orders.value = res.data.records || res.data || []
    total.value = res.data.total || orders.value.length
    
    // 更新统计
    updateStats()
  } catch (error) {
    console.error('获取订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

const updateStats = async () => {
  try {
    const res = await getStoreOrders({ size: 1000 })
    const allOrders = res.data.records || res.data || []
    
    orderStats.all = allOrders.length
    orderStats.paid = allOrders.filter(o => o.status === 'PAID').length
    orderStats.confirmed = allOrders.filter(o => o.status === 'CONFIRMED').length
    orderStats.ready = allOrders.filter(o => o.status === 'READY').length
    orderStats.completed = allOrders.filter(o => o.status === 'COMPLETED').length
    orderStats.cancelled = allOrders.filter(o => ['CANCELLED', 'REJECTED'].includes(o.status)).length
  } catch (error) {
    console.error('获取订单统计失败:', error)
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.store-order-list h2 {
  margin-bottom: 20px;
  color: #333;
}

.search-card {
  margin-bottom: 20px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-item {
  background: #fff;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.stat-item:hover {
  border-color: #409eff;
}

.stat-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.stat-item .stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-item .stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.order-items {
  font-size: 13px;
  color: #666;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.pickup-code {
  font-family: monospace;
  font-weight: bold;
  color: #409eff;
}

.pickup-code.large {
  font-size: 24px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.order-total {
  margin-top: 20px;
  text-align: right;
  font-size: 16px;
}

.total-price {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}

h4 {
  margin: 20px 0 10px;
  color: #333;
}
</style>