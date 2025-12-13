
<template>
  <div class="order-list">
    <div class="page-header">
      <h1>我的订单</h1>
    </div>

    <!-- 订单状态筛选 -->
    <el-tabs v-model="activeStatus" @tab-change="handleStatusChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待支付" name="PENDING" />
      <el-tab-pane label="待备货" name="PAID" />
      <el-tab-pane label="待取货" name="READY" />
      <el-tab-pane label="已完成" name="COMPLETED" />
      <el-tab-pane label="已取消" name="CANCELLED" />
    </el-tabs>

    <div class="order-content" v-loading="loading">
      <div class="order-card" v-for="order in orders" :key="order.id">
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-time">{{ formatTime(order.createTime) }}</span>
          </div>
          <el-tag :type="getStatusType(order.status)">
            {{ getStatusText(order.status) }}
          </el-tag>
        </div>

        <div class="order-store">
          <el-icon><Shop /></el-icon>
          <span>{{ order.store?.name || '门店' }}</span>
        </div>

        <div class="order-items">
          <div class="item" v-for="item in order.items" :key="item.id">
            <el-image
              :src="item.productImage || 'https://via.placeholder.com/60?text=Product'"
              fit="cover"
              class="item-image"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="item-info">
              <div class="item-name">{{ item.productName }}</div>
              <div class="item-price">¥{{ item.price?.toFixed(2) }} × {{ item.quantity }}</div>
            </div>
          </div>
        </div>

        <div class="order-footer">
          <div class="order-total">
            <span>共 {{ order.items?.length || 0 }} 件商品</span>
            <span class="total-price">
              合计：<em>¥{{ order.totalAmount?.toFixed(2) }}</em>
            </span>
          </div>
          <div class="order-actions">
            <el-button
              v-if="order.status === 'PENDING'"
              type="primary"
              size="small"
              @click="payOrder(order)"
            >
              去支付
            </el-button>
            <el-button
              v-if="order.status === 'PENDING'"
              size="small"
              @click="cancelOrder(order)"
            >
              取消订单
            </el-button>
            <el-button
              v-if="order.status === 'READY'"
              type="success"
              size="small"
              @click="showPickupCode(order)"
            >
              查看取货码
            </el-button>
            <el-button
              size="small"
              @click="viewDetail(order)"
            >
              查看详情
            </el-button>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 30]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 取货码对话框 -->
    <el-dialog
      v-model="pickupDialogVisible"
      title="取货码"
      width="400px"
      center
    >
      <div class="pickup-code-content">
        <div class="pickup-code">{{ currentPickupCode }}</div>
        <p class="pickup-tip">请向门店工作人员出示此取货码</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Shop, Picture } from '@element-plus/icons-vue'
import { getMyOrders, payOrder as payOrderApi, cancelOrder as cancelOrderApi } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const orders = ref([])
const loading = ref(false)
const activeStatus = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const pickupDialogVisible = ref(false)
const currentPickupCode = ref('')

const statusMap = {
  PENDING: { text: '待支付', type: 'warning' },
  PAID: { text: '待备货', type: 'primary' },
  READY: { text: '待取货', type: 'success' },
  COMPLETED: { text: '已完成', type: 'info' },
  CANCELLED: { text: '已取消', type: 'info' }
}

const getStatusText = (status) => statusMap[status]?.text || status
const getStatusType = (status) => statusMap[status]?.type || 'info'

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const handleStatusChange = () => {
  currentPage.value = 1
  fetchOrders()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchOrders()
}

const handlePageChange = () => {
  fetchOrders()
}

const viewDetail = (order) => {
  router.push(`/orders/${order.id}`)
}

const payOrder = async (order) => {
  try {
    await payOrderApi(order.id)
    ElMessage.success('支付成功')
    fetchOrders()
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error(error.response?.data?.message || '支付失败')
  }
}

const cancelOrder = (order) => {
  ElMessageBox.confirm(
    '确定要取消此订单吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await cancelOrderApi(order.id)
      ElMessage.success('订单已取消')
      fetchOrders()
    } catch (error) {
      console.error('取消订单失败:', error)
      ElMessage.error(error.response?.data?.message || '取消订单失败')
    }
  }).catch(() => {})
}

const showPickupCode = (order) => {
  currentPickupCode.value = order.pickupCode
  pickupDialogVisible.value = true
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    if (activeStatus.value !== 'all') {
      params.status = activeStatus.value
    }
    
    const res = await getMyOrders(params)
    orders.value = res.data.records || res.data || []
    total.value = res.data.total || orders.value.length
  } catch (error) {
    console.error('获取订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.order-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 10px;
}

.page-header {
  margin-bottom: 20px;
  padding: 0 10px;
}

.page-header h1 {
  font-size: 24px;
  color: #333;
}

.order-content {
  min-height: 400px;
}

.order-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.order-info {
  display: flex;
  gap: 20px;
}

.order-no {
  font-weight: bold;
  color: #333;
}

.order-time {
  color: #999;
  font-size: 13px;
}

.order-store {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  margin-bottom: 15px;
  font-size: 14px;
}

.order-items {
  margin-bottom: 15px;
}

.item {
  display: flex;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.item:last-child {
  border-bottom: none;
}

.item-image {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.image-placeholder .el-icon {
  font-size: 20px;
  color: #c0c4cc;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.item-name {
  font-size: 14px;
  color: #333;
}

.item-price {
  font-size: 13px;
  color: #999;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.order-total {
  display: flex;
  align-items: center;
  gap: 20px;
  color: #666;
}

.total-price {
  font-size: 16px;
  color: #333;
}

.total-price em {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
  font-style: normal;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.pickup-code-content {
  text-align: center;
  padding: 20px;
}

.pickup-code {
  font-size: 48px;
  font-weight: bold;
  color: #409eff;
  letter-spacing: 8px;
  margin-bottom: 20px;
}

.pickup-tip {
  color: #999;
  font-size: 14px;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .order-list {
    padding: 0 5px;
  }
  
  .page-header {
    margin-bottom: 15px;
    padding: 0 10px;
  }
  
  .page-header h1 {
    font-size: 20px;
  }
  
  /* 标签页优化 */
  .el-tabs {
    margin-bottom: 15px;
  }
  
  .order-card {
    padding: 15px;
    margin-bottom: 15px;
  }
  
  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    margin-bottom: 12px;
    padding-bottom: 12px;
  }
  
  .order-info {
    flex-direction: column;
    gap: 6px;
    width: 100%;
  }
  
  .order-no {
    font-size: 13px;
  }
  
  .order-time {
    font-size: 12px;
  }
  
  .order-store {
    font-size: 13px;
    margin-bottom: 12px;
  }
  
  .item {
    padding: 8px 0;
  }
  
  .item-image {
    width: 70px;
    height: 70px;
  }
  
  .item-name {
    font-size: 13px;
    margin-bottom: 4px;
  }
  
  .item-price {
    font-size: 12px;
  }
  
  .order-footer {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
    padding-top: 12px;
  }
  
  .order-total {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .total-price {
    font-size: 15px;
  }
  
  .total-price em {
    font-size: 18px;
  }
  
  .order-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .order-actions .el-button {
    width: 100%;
    height: 40px;
  }
  
  .pagination {
    margin-top: 15px;
  }
  
  /* 取货码对话框优化 */
  .pickup-code {
    font-size: 36px;
    letter-spacing: 6px;
  }
  
  .pickup-tip {
    font-size: 13px;
  }
}

/* 平板优化 */
@media (min-width: 769px) and (max-width: 1024px) {
  .order-list {
    padding: 0 15px;
  }
}
</style>
