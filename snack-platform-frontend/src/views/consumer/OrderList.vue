<template>
  <div class="order-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-text">
          <h1>我的订单</h1>
          <p v-if="total > 0">共 {{ total }} 个订单</p>
        </div>
      </div>
    </div>

    <!-- 订单状态筛选 -->
    <div class="status-tabs">
      <button class="tab-btn" :class="{ active: activeStatus === 'all' }" @click="handleStatusChange('all')">全部</button>
      <button class="tab-btn" :class="{ active: activeStatus === 'PENDING' }" @click="handleStatusChange('PENDING')">待支付</button>
      <button class="tab-btn" :class="{ active: activeStatus === 'PAID' }" @click="handleStatusChange('PAID')">待备货</button>
      <button class="tab-btn" :class="{ active: activeStatus === 'READY' }" @click="handleStatusChange('READY')">待取货</button>
      <button class="tab-btn" :class="{ active: activeStatus === 'COMPLETED' }" @click="handleStatusChange('COMPLETED')">已完成</button>
      <button class="tab-btn" :class="{ active: activeStatus === 'CANCELLED' }" @click="handleStatusChange('CANCELLED')">已取消</button>
    </div>

    <div class="order-content" v-loading="loading">
      <div class="order-card" v-for="order in orders" :key="order.id">
        <!-- 订单头部 -->
        <div class="order-header">
          <div class="store-info">
            <el-icon :size="16"><Shop /></el-icon>
            <span class="store-name">{{ order.store?.name || '门店' }}</span>
          </div>
          <div class="status-badge" :class="getStatusClass(order.status)">
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
                  <el-icon :size="24"><Goods /></el-icon>
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
              size="small"
              class="action-btn pay-btn"
              @click.stop="payOrder(order)"
            >
              去支付
            </el-button>
            <el-button
              v-if="order.status === 'PENDING'"
              size="small"
              class="action-btn"
              @click.stop="cancelOrder(order)"
            >
              取消
            </el-button>
            <el-button
              v-if="order.status === 'READY'"
              type="success"
              size="small"
              class="action-btn pickup-btn"
              @click.stop="showPickupCode(order)"
            >
              <el-icon><Ticket /></el-icon>
              取货码
            </el-button>
            <el-button
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
        <el-icon :size="48" class="empty-icon"><Document /></el-icon>
        <h3>暂无订单</h3>
        <p>快去挑选心仪的零食吧~</p>
        <el-button type="primary" @click="$router.push('/stores')">
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
          <el-icon :size="32" class="pickup-icon"><Ticket /></el-icon>
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
import { ArrowRight, InfoFilled, Clock, Shop, Goods, Document, Ticket } from '@element-plus/icons-vue'
import { getMyOrders, payOrder as payOrderApi, cancelOrder as cancelOrderApi } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatTime } from '@/utils/format'

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
  PENDING: { text: '待支付', class: 'pending' },
  PAID: { text: '待备货', class: 'paid' },
  READY: { text: '待取货', class: 'ready' },
  COMPLETED: { text: '已完成', class: 'completed' },
  CANCELLED: { text: '已取消', class: 'cancelled' }
}

const getStatusText = (status) => statusConfig[status]?.text || status
const getStatusClass = (status) => statusConfig[status]?.class || ''

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
      pageNum: currentPage.value,
      pageSize: pageSize.value
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
  --color-primary: #D94F04;
  --color-primary-hover: #C24503;
  --color-primary-light: #FFF4ED;
  --color-accent: #2D6A4F;
  --color-accent-light: #EDF5F0;
  --color-text: #1A1A1A;
  --color-text-secondary: #666;
  --color-text-muted: #999;
  --color-bg: #FAF9F6;
  --color-surface: #FFF;
  --color-border: #EAEAEA;
  --color-hover: #F5F5F3;
  --shadow-sm: 0 1px 2px rgba(0,0,0,0.04);
  --shadow-md: 0 2px 8px rgba(0,0,0,0.06);
  --radius-sm: 6px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --transition-fast: 150ms ease;
  --transition-normal: 200ms ease;

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

.header-text h1 {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text);
  margin: 0 0 4px;
}

.header-text p {
  font-size: 14px;
  color: var(--color-text-muted);
  margin: 0;
}

/* 状态标签页 */
.status-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  padding: 12px 16px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  overflow-x: auto;
}

.tab-btn {
  padding: 8px 20px;
  border-radius: var(--radius-md);
  border: 1px solid transparent;
  background: transparent;
  cursor: pointer;
  transition: all var(--transition-fast);
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary);
  white-space: nowrap;
  flex-shrink: 0;
}

.tab-btn:hover {
  background: var(--color-hover);
  border-color: var(--color-border);
}

.tab-btn.active {
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-color: var(--color-primary);
}

/* 订单内容 */
.order-content {
  min-height: 400px;
}

/* 订单卡片 */
.order-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  margin-bottom: 16px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  transition: all var(--transition-fast);
}

.order-card:hover {
  border-color: var(--color-border);
  box-shadow: var(--shadow-md);
}

/* 订单头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: var(--color-bg);
  border-bottom: 1px solid var(--color-border);
}

.store-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-secondary);
}

.store-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-weight: 500;
}

.status-badge.pending {
  background: #FFF7ED;
  color: #D97706;
}

.status-badge.paid {
  background: #EFF6FF;
  color: #2563EB;
}

.status-badge.ready {
  background: var(--color-accent-light);
  color: var(--color-accent);
}

.status-badge.completed {
  background: #F3F4F6;
  color: #6B7280;
}

.status-badge.cancelled {
  background: #FEF2F2;
  color: #DC2626;
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
  border-bottom: 1px solid var(--color-border);
}

.item:last-child {
  border-bottom: none;
}

.item-image {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-md);
  overflow: hidden;
  flex-shrink: 0;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-hover);
  color: var(--color-text-muted);
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
  color: var(--color-text);
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
  color: var(--color-primary);
  font-weight: 500;
}

.item-qty {
  font-size: 13px;
  color: var(--color-text-muted);
}

.more-items {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 12px 0 0;
  color: var(--color-text-muted);
  font-size: 13px;
}

/* 订单信息 */
.order-meta {
  padding: 12px 20px;
  background: var(--color-bg);
  border-top: 1px solid var(--color-border);
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.meta-label {
  font-size: 13px;
  color: var(--color-text-muted);
}

.meta-value {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.meta-value.order-no {
  font-family: monospace;
  color: var(--color-text);
}

/* 订单底部 */
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid var(--color-border);
}

.order-total {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.total-label {
  font-size: 13px;
  color: var(--color-text-muted);
}

.total-amount {
  color: var(--color-primary);
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
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.pay-btn:hover {
  background: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}

.pickup-btn {
  background: var(--color-accent);
  border-color: var(--color-accent);
}

.pickup-btn:hover {
  background: var(--color-accent);
  border-color: var(--color-accent);
  opacity: 0.9;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 24px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
}

.empty-icon {
  color: var(--color-text-muted);
  margin-bottom: 24px;
}

.empty-state h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 8px;
}

.empty-state p {
  font-size: 14px;
  color: var(--color-text-muted);
  margin: 0 0 24px;
}

.empty-state .el-button {
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.empty-state .el-button:hover {
  background: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}

/* 分页 */
.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.pagination :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: var(--color-primary);
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

.pickup-icon {
  color: var(--color-primary);
  margin-bottom: 12px;
}

.pickup-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
}

.pickup-code-box {
  background: var(--color-primary-light);
  border-radius: var(--radius-lg);
  padding: 24px;
  margin-bottom: 24px;
}

.pickup-code {
  font-size: 48px;
  font-weight: 700;
  color: var(--color-primary);
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
  color: var(--color-text-secondary);
  font-size: 14px;
}

.tip-item .el-icon {
  color: var(--color-primary);
}

/* 移动端优化 */
@media (max-width: 768px) {
  .order-list {
    padding: 0 12px;
  }

  .header-text h1 {
    font-size: 22px;
  }

  .status-tabs {
    padding: 10px 12px;
    gap: 6px;
    margin-bottom: 16px;
  }

  .tab-btn {
    padding: 6px 14px;
    font-size: 13px;
  }

  .order-card {
    border-radius: var(--radius-lg);
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
