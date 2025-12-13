<template>
  <div class="order-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <span class="header-emoji">📋</span>
        <div class="header-text">
          <h1>我的订单</h1>
          <p v-if="total > 0">共 {{ total }} 个订单</p>
        </div>
      </div>
    </div>

    <!-- 订单状态筛选 -->
    <div class="status-tabs">
      <div 
        class="tab-item" 
        :class="{ active: activeStatus === 'all' }"
        @click="handleStatusChange('all')"
      >
        <span class="tab-icon">📦</span>
        <span class="tab-text">全部</span>
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeStatus === 'PENDING' }"
        @click="handleStatusChange('PENDING')"
      >
        <span class="tab-icon">💳</span>
        <span class="tab-text">待支付</span>
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeStatus === 'PAID' }"
        @click="handleStatusChange('PAID')"
      >
        <span class="tab-icon">📦</span>
        <span class="tab-text">待备货</span>
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeStatus === 'READY' }"
        @click="handleStatusChange('READY')"
      >
        <span class="tab-icon">🎁</span>
        <span class="tab-text">待取货</span>
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeStatus === 'COMPLETED' }"
        @click="handleStatusChange('COMPLETED')"
      >
        <span class="tab-icon">✅</span>
        <span class="tab-text">已完成</span>
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeStatus === 'CANCELLED' }"
        @click="handleStatusChange('CANCELLED')"
      >
        <span class="tab-icon">❌</span>
        <span class="tab-text">已取消</span>
      </div>
    </div>

    <div class="order-content" v-loading="loading">
      <div class="order-card" v-for="order in orders" :key="order.id">
        <!-- 订单头部 -->
        <div class="order-header">
          <div class="store-info">
            <span class="store-icon">🏪</span>
            <span class="store-name">{{ order.store?.name || '门店' }}</span>
          </div>
          <div class="status-badge" :class="getStatusClass(order.status)">
            <span class="status-icon">{{ getStatusIcon(order.status) }}</span>
            <span class="status-text">{{ getStatusText(order.status) }}</span>
          </div>
        </div>

        <!-- 订单商品 -->
        <div class="order-items" @click="viewDetail(order)">
          <div class="item" v-for="item in order.items?.slice(0, 3)" :key="item.id">
            <el-image
              :src="item.productImage || 'https://via.placeholder.com/60?text=Product'"
              fit="cover"
              class="item-image"
            >
              <template #error>
                <div class="image-placeholder">
                  <span class="placeholder-emoji">🍿</span>
                </div>
              </template>
            </el-image>
            <div class="item-info">
              <div class="item-name">{{ item.productName }}</div>
              <div class="item-meta">
                <span class="item-price">¥{{ item.price?.toFixed(2) }}</span>
                <span class="item-qty">×{{ item.quantity }}</span>
              </div>
            </div>
          </div>
          <div class="more-items" v-if="order.items?.length > 3">
            <span>还有 {{ order.items.length - 3 }} 件商品</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>

        <!-- 订单信息 -->
        <div class="order-meta">
          <div class="meta-row">
            <span class="meta-label">订单号</span>
            <span class="meta-value order-no">{{ order.orderNo }}</span>
          </div>
          <div class="meta-row">
            <span class="meta-label">下单时间</span>
            <span class="meta-value">{{ formatTime(order.createTime) }}</span>
          </div>
        </div>

        <!-- 订单底部 -->
        <div class="order-footer">
          <div class="order-total">
            <span class="total-label">共 {{ order.items?.length || 0 }} 件</span>
            <span class="total-amount">
              <span class="currency">¥</span>
              <span class="amount">{{ order.totalAmount?.toFixed(2) }}</span>
            </span>
          </div>
          <div class="order-actions">
            <el-button
              v-if="order.status === 'PENDING'"
              type="primary"
              round
              size="small"
              class="action-btn pay-btn"
              @click.stop="payOrder(order)"
            >
              去支付
            </el-button>
            <el-button
              v-if="order.status === 'PENDING'"
              round
              size="small"
              class="action-btn"
              @click.stop="cancelOrder(order)"
            >
              取消
            </el-button>
            <el-button
              v-if="order.status === 'READY'"
              type="success"
              round
              size="small"
              class="action-btn pickup-btn"
              @click.stop="showPickupCode(order)"
            >
              <span class="btn-icon">🎫</span>
              取货码
            </el-button>
            <el-button
              round
              size="small"
              class="action-btn detail-btn"
              @click.stop="viewDetail(order)"
            >
              详情
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-if="!loading && orders.length === 0">
        <span class="empty-emoji">📭</span>
        <h3>暂无订单</h3>
        <p>快去挑选心仪的零食吧~</p>
        <el-button type="primary" round @click="$router.push('/stores')">
          去逛逛
        </el-button>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 30]"
          layout="total, prev, pager, next"
          background
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 取货码对话框 -->
    <el-dialog
      v-model="pickupDialogVisible"
      title=""
      width="400px"
      center
      class="pickup-dialog"
    >
      <div class="pickup-code-content">
        <div class="pickup-header">
          <span class="pickup-emoji">🎫</span>
          <h3>取货码</h3>
        </div>
        <div class="pickup-code-box">
          <div class="pickup-code">{{ currentPickupCode }}</div>
        </div>
        <div class="pickup-tips">
          <div class="tip-item">
            <el-icon><InfoFilled /></el-icon>
            <span>请向门店工作人员出示此取货码</span>
          </div>
          <div class="tip-item">
            <el-icon><Clock /></el-icon>
            <span>请在营业时间内前往取货</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, InfoFilled, Clock } from '@element-plus/icons-vue'
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

const statusConfig = {
  PENDING: { text: '待支付', icon: '💳', class: 'pending' },
  PAID: { text: '待备货', icon: '📦', class: 'paid' },
  READY: { text: '待取货', icon: '🎁', class: 'ready' },
  COMPLETED: { text: '已完成', icon: '✅', class: 'completed' },
  CANCELLED: { text: '已取消', icon: '❌', class: 'cancelled' }
}

const getStatusText = (status) => statusConfig[status]?.text || status
const getStatusIcon = (status) => statusConfig[status]?.icon || '📋'
const getStatusClass = (status) => statusConfig[status]?.class || ''

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

const handleStatusChange = (status) => {
  activeStatus.value = status
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
    ElMessage.success('支付成功 🎉')
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
  padding: 0 16px;
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-emoji {
  font-size: 40px;
}

.header-text h1 {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 4px;
}

.header-text p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

/* 状态标签页 */
.status-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding: 16px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow-x: auto;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px 20px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.tab-item:hover {
  background: rgba(255, 107, 107, 0.08);
}

.tab-item.active {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
}

.tab-icon {
  font-size: 24px;
}

.tab-text {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.tab-item.active .tab-text {
  color: #fff;
}

/* 订单内容 */
.order-content {
  min-height: 400px;
}

/* 订单卡片 */
.order-card {
  background: #fff;
  border-radius: 16px;
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.order-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

/* 订单头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.05) 0%, rgba(255, 142, 83, 0.05) 100%);
  border-bottom: 1px solid #f5f5f5;
}

.store-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.store-icon {
  font-size: 20px;
}

.store-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.status-badge.pending {
  background: rgba(230, 162, 60, 0.1);
  color: #E6A23C;
}

.status-badge.paid {
  background: rgba(64, 158, 255, 0.1);
  color: #409EFF;
}

.status-badge.ready {
  background: rgba(103, 194, 58, 0.1);
  color: #67C23A;
}

.status-badge.completed {
  background: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.status-badge.cancelled {
  background: rgba(245, 108, 108, 0.1);
  color: #F56C6C;
}

.status-icon {
  font-size: 14px;
}

/* 订单商品 */
.order-items {
  padding: 16px 20px;
  cursor: pointer;
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
  width: 64px;
  height: 64px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
}

.placeholder-emoji {
  font-size: 28px;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-price {
  font-size: 14px;
  color: #FF6B6B;
  font-weight: 500;
}

.item-qty {
  font-size: 13px;
  color: #999;
}

.more-items {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 12px 0 0;
  color: #999;
  font-size: 13px;
}

/* 订单信息 */
.order-meta {
  padding: 12px 20px;
  background: #fafafa;
  border-top: 1px solid #f5f5f5;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.meta-label {
  font-size: 13px;
  color: #999;
}

.meta-value {
  font-size: 13px;
  color: #666;
}

.meta-value.order-no {
  font-family: monospace;
  color: #333;
}

/* 订单底部 */
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid #f5f5f5;
}

.order-total {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.total-label {
  font-size: 13px;
  color: #999;
}

.total-amount {
  color: #FF6B6B;
  font-weight: 700;
}

.total-amount .currency {
  font-size: 14px;
}

.total-amount .amount {
  font-size: 20px;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  font-size: 13px;
}

.pay-btn {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border: none;
}

.pay-btn:hover {
  background: linear-gradient(135deg, #FF5252 0%, #FF7043 100%);
}

.pickup-btn {
  background: linear-gradient(135deg, #67C23A 0%, #85CE61 100%);
  border: none;
}

.pickup-btn:hover {
  background: linear-gradient(135deg, #5DAF34 0%, #7EC857 100%);
}

.btn-icon {
  margin-right: 4px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.empty-emoji {
  font-size: 80px;
  display: block;
  margin-bottom: 24px;
}

.empty-state h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px;
}

.empty-state p {
  font-size: 14px;
  color: #999;
  margin: 0 0 24px;
}

.empty-state .el-button {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border: none;
}

/* 分页 */
.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.pagination :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
}

/* 取货码对话框 */
.pickup-dialog :deep(.el-dialog__header) {
  display: none;
}

.pickup-code-content {
  text-align: center;
  padding: 20px;
}

.pickup-header {
  margin-bottom: 24px;
}

.pickup-emoji {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.pickup-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.pickup-code-box {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.1) 0%, rgba(255, 142, 83, 0.1) 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
}

.pickup-code {
  font-size: 48px;
  font-weight: 700;
  color: #FF6B6B;
  letter-spacing: 8px;
  font-family: monospace;
}

.pickup-tips {
  text-align: left;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  color: #666;
  font-size: 14px;
}

.tip-item .el-icon {
  color: #FF6B6B;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .order-list {
    padding: 0 12px;
  }
  
  .header-emoji {
    font-size: 32px;
  }
  
  .header-text h1 {
    font-size: 22px;
  }
  
  .status-tabs {
    padding: 12px;
    gap: 8px;
    margin-bottom: 16px;
  }
  
  .tab-item {
    padding: 10px 14px;
  }
  
  .tab-icon {
    font-size: 20px;
  }
  
  .tab-text {
    font-size: 12px;
  }
  
  .order-card {
    border-radius: 12px;
    margin-bottom: 12px;
  }
  
  .order-header {
    padding: 12px 16px;
  }
  
  .store-name {
    font-size: 14px;
  }
  
  .status-badge {
    padding: 4px 10px;
    font-size: 12px;
  }
  
  .order-items {
    padding: 12px 16px;
  }
  
  .item-image {
    width: 56px;
    height: 56px;
  }
  
  .item-name {
    font-size: 13px;
  }
  
  .order-meta {
    padding: 10px 16px;
  }
  
  .order-footer {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
    padding: 12px 16px;
  }
  
  .order-total {
    justify-content: space-between;
  }
  
  .order-actions {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }
  
  .action-btn {
    width: 100%;
  }
  
  .empty-state {
    padding: 60px 24px;
  }
  
  .empty-emoji {
    font-size: 60px;
  }
  
  .pickup-code {
    font-size: 36px;
    letter-spacing: 6px;
  }
}

/* 平板优化 */
@media (min-width: 769px) and (max-width: 1024px) {
  .order-list {
    padding: 0 20px;
  }
}
</style>
