<template>
  <div class="order-detail">
    <div class="page-head">
      <button class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回订单列表</span>
      </button>
      <h1>订单详情</h1>
    </div>

    <div class="detail-body" v-loading="loading">
      <!-- Status card -->
      <div class="card status-card">
        <div class="status-row">
          <div class="status-left">
            <span class="status-tag" :class="getStatusClass(order.status)">
              {{ getStatusText(order.status) }}
            </span>
            <span class="order-no">订单号：{{ order.orderNo }}</span>
          </div>
          <div class="status-actions">
            <el-button v-if="order.status === 'PENDING'" type="danger" size="small" @click="cancelOrder">
              取消订单
            </el-button>
            <el-button v-if="order.status === 'PENDING'" type="primary" size="small" @click="payOrder">
              去支付
            </el-button>
          </div>
        </div>

        <!-- Steps -->
        <el-steps :active="getStepActive(order.status)" finish-status="success" class="order-steps">
          <el-step title="提交订单" :description="formatTime(order.createTime)" />
          <el-step title="支付成功" :description="order.payTime ? formatTime(order.payTime) : ''" />
          <el-step title="商家确认" :description="order.confirmTime ? formatTime(order.confirmTime) : ''" />
          <el-step title="待取货" :description="order.readyTime ? formatTime(order.readyTime) : ''" />
          <el-step title="已完成" :description="order.completeTime ? formatTime(order.completeTime) : ''" />
        </el-steps>
      </div>

      <!-- Pickup info -->
      <div class="card pickup-card" v-if="order.status === 'READY'">
        <div class="card-title">取货信息</div>
        <div class="pickup-body">
          <div class="pickup-label">取货码</div>
          <div class="pickup-code">{{ order.pickupCode }}</div>
          <div class="pickup-tip">请凭此取货码到门店取货</div>
        </div>
      </div>

      <!-- Store info -->
      <div class="card">
        <div class="card-title">门店信息</div>
        <div class="info-list">
          <div class="info-row">
            <el-icon :size="16"><Shop /></el-icon>
            <span>{{ order.store?.name }}</span>
          </div>
          <div class="info-row">
            <el-icon :size="16"><Location /></el-icon>
            <span>{{ order.store?.address }}</span>
          </div>
          <div class="info-row">
            <el-icon :size="16"><Phone /></el-icon>
            <span>{{ order.store?.phone || '暂无电话' }}</span>
          </div>
        </div>
      </div>

      <!-- Items -->
      <div class="card">
        <div class="card-title">商品信息</div>
        <div class="items-list">
          <div class="order-item" v-for="item in order.items" :key="item.id">
            <el-image :src="item.productImage" fit="cover" class="item-img">
              <template #error>
                <div class="img-fallback">
                  <el-icon :size="20"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="item-info">
              <div class="item-name">{{ item.productName }}</div>
              <div class="item-spec">¥{{ item.price?.toFixed(2) }} x {{ item.quantity }}</div>
            </div>
            <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
          </div>
        </div>
        <div class="items-summary">
          <div class="sum-row">
            <span>商品总数</span>
            <span>{{ totalCount }} 件</span>
          </div>
          <div class="sum-row sum-total">
            <span>订单总额</span>
            <span class="total-price">¥{{ order.totalAmount?.toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- Order info -->
      <div class="card">
        <div class="card-title">订单信息</div>
        <div class="detail-grid">
          <div class="detail-cell">
            <span class="cell-label">订单编号</span>
            <span class="cell-value mono">{{ order.orderNo }}</span>
          </div>
          <div class="detail-cell">
            <span class="cell-label">下单时间</span>
            <span class="cell-value">{{ formatTime(order.createTime) }}</span>
          </div>
          <div class="detail-cell">
            <span class="cell-label">支付时间</span>
            <span class="cell-value">{{ order.payTime ? formatTime(order.payTime) : '-' }}</span>
          </div>
          <div class="detail-cell">
            <span class="cell-label">订单状态</span>
            <span class="cell-value">{{ getStatusText(order.status) }}</span>
          </div>
          <div class="detail-cell full">
            <span class="cell-label">备注</span>
            <span class="cell-value">{{ order.remark || '无' }}</span>
          </div>
          <div class="detail-cell full" v-if="order.status === 'REJECTED'">
            <span class="cell-label">拒绝原因</span>
            <span class="cell-value reject">{{ order.rejectReason }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Shop, Location, Phone, Picture } from '@element-plus/icons-vue'
import { getOrderById, cancelOrder as cancelOrderApi, payOrder as payOrderApi } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatTime } from '@/utils/format'
import { getStatusText } from '@/utils/constants'

const route = useRoute()
const router = useRouter()

const order = ref({})
const loading = ref(false)

const orderId = computed(() => route.params.id)

const totalCount = computed(() => {
  return order.value.items?.reduce((sum, item) => sum + item.quantity, 0) || 0
})

const goBack = () => {
  router.push('/orders')
}

const getStatusClass = (status) => {
  const map = {
    PENDING: 'pending',
    PAID: 'paid',
    CONFIRMED: 'paid',
    READY: 'ready',
    COMPLETED: 'completed',
    CANCELLED: 'cancelled',
    REJECTED: 'cancelled'
  }
  return map[status] || ''
}

const getStepActive = (status) => {
  const steps = {
    'PENDING': 0,
    'PAID': 1,
    'CONFIRMED': 2,
    'READY': 3,
    'COMPLETED': 4,
    'CANCELLED': -1,
    'REJECTED': -1
  }
  return steps[status] ?? 0
}

const cancelOrder = () => {
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
      await cancelOrderApi(orderId.value)
      ElMessage.success('订单已取消')
      fetchOrder()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '取消失败')
    }
  }).catch(() => {})
}

const payOrder = async () => {
  try {
    await payOrderApi(orderId.value)
    ElMessage.success('支付成功')
    fetchOrder()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '支付失败')
  }
}

const fetchOrder = async () => {
  loading.value = true
  try {
    const res = await getOrderById(orderId.value)
    order.value = res.data
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOrder()
})
</script>

<style scoped>
.order-detail {
  max-width: 900px;
  margin: 0 auto;
}

/* Page head */
.page-head {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.page-head h1 {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text);
  margin: 0;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: border-color var(--transition-fast), color var(--transition-fast);
}

.back-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

/* Cards */
.card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 24px;
  margin-bottom: 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border);
}

/* Status card */
.status-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.status-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-tag {
  display: inline-block;
  padding: 6px 14px;
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-weight: 600;
}

.status-tag.pending {
  background: #FFF7ED;
  color: #D97706;
}

.status-tag.paid {
  background: #EFF6FF;
  color: #2563EB;
}

.status-tag.ready {
  background: var(--color-accent-light);
  color: var(--color-accent);
}

.status-tag.completed {
  background: #F3F4F6;
  color: #6B7280;
}

.status-tag.cancelled {
  background: #FEF2F2;
  color: #DC2626;
}

.order-no {
  font-size: 13px;
  color: var(--color-text-muted);
  font-family: var(--font-mono, monospace);
}

.status-actions {
  display: flex;
  gap: 8px;
}

.order-steps {
  margin-top: 8px;
}

/* Pickup card */
.pickup-body {
  text-align: center;
  padding: 16px 0;
}

.pickup-label {
  font-size: 13px;
  color: var(--color-text-muted);
  margin-bottom: 12px;
}

.pickup-code {
  font-size: 44px;
  font-weight: 700;
  color: var(--color-primary);
  letter-spacing: 8px;
  font-family: monospace;
  margin-bottom: 12px;
}

.pickup-tip {
  font-size: 13px;
  color: var(--color-text-muted);
}

/* Info list */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.info-row .el-icon {
  color: var(--color-text-muted);
}

/* Items */
.items-list {
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 16px;
  margin-bottom: 16px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
}

.order-item:not(:last-child) {
  border-bottom: 1px dashed var(--color-border);
}

.item-img {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-md);
  overflow: hidden;
  flex-shrink: 0;
}

.img-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg);
  color: var(--color-text-muted);
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-spec {
  font-size: 13px;
  color: var(--color-text-muted);
}

.item-subtotal {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  flex-shrink: 0;
}

/* Summary */
.items-summary {
  text-align: right;
}

.sum-row {
  display: flex;
  justify-content: flex-end;
  gap: 24px;
  padding: 6px 0;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.sum-row.sum-total {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  padding-top: 8px;
}

.total-price {
  color: var(--color-primary);
  font-size: 20px;
  font-weight: 700;
}

/* Detail grid */
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.detail-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-cell.full {
  grid-column: 1 / -1;
}

.cell-label {
  font-size: 12px;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.02em;
}

.cell-value {
  font-size: 14px;
  color: var(--color-text);
}

.cell-value.mono {
  font-family: monospace;
}

.cell-value.reject {
  color: #DC2626;
}

/* Responsive */
@media (max-width: 768px) {
  .order-detail {
    padding: 0 4px;
  }

  .page-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .card {
    padding: 16px;
    border-radius: var(--radius-md);
  }

  .status-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .status-actions {
    width: 100%;
  }

  .status-actions .el-button {
    flex: 1;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .pickup-code {
    font-size: 36px;
    letter-spacing: 6px;
  }
}
</style>
