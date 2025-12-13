<template>
  <div class="order-detail">
    <div class="page-header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回订单列表
      </el-button>
      <h1>订单详情</h1>
    </div>

    <div class="order-content" v-loading="loading">
      <!-- 订单状态 -->
      <el-card class="status-card">
        <div class="status-header">
          <div class="status-info">
            <el-tag :type="getStatusType(order.status)" size="large">
              {{ getStatusText(order.status) }}
            </el-tag>
            <span class="order-no">订单号：{{ order.orderNo }}</span>
          </div>
          <div class="status-actions">
            <el-button
              v-if="order.status === 'PENDING'"
              type="danger"
              @click="cancelOrder"
            >
              取消订单
            </el-button>
            <el-button
              v-if="order.status === 'PENDING'"
              type="primary"
              @click="payOrder"
            >
              去支付
            </el-button>
          </div>
        </div>

        <!-- 订单进度 -->
        <el-steps
          :active="getStepActive(order.status)"
          finish-status="success"
          class="order-steps"
        >
          <el-step title="提交订单" :description="formatTime(order.createTime)" />
          <el-step title="支付成功" :description="order.payTime ? formatTime(order.payTime) : ''" />
          <el-step title="商家确认" :description="order.confirmTime ? formatTime(order.confirmTime) : ''" />
          <el-step title="待取货" :description="order.readyTime ? formatTime(order.readyTime) : ''" />
          <el-step title="已完成" :description="order.completeTime ? formatTime(order.completeTime) : ''" />
        </el-steps>
      </el-card>

      <!-- 取货信息 -->
      <el-card class="pickup-card" v-if="order.status === 'READY'">
        <template #header>
          <span>取货信息</span>
        </template>
        <div class="pickup-info">
          <div class="pickup-code">
            <span class="label">取货码</span>
            <span class="code">{{ order.pickupCode }}</span>
          </div>
          <div class="pickup-tip">
            请凭此取货码到门店取货
          </div>
        </div>
      </el-card>

      <!-- 门店信息 -->
      <el-card class="store-card">
        <template #header>
          <span>门店信息</span>
        </template>
        <div class="store-info">
          <div class="info-item">
            <el-icon><Shop /></el-icon>
            <span>{{ order.store?.name }}</span>
          </div>
          <div class="info-item">
            <el-icon><Location /></el-icon>
            <span>{{ order.store?.address }}</span>
          </div>
          <div class="info-item">
            <el-icon><Phone /></el-icon>
            <span>{{ order.store?.phone || '暂无电话' }}</span>
          </div>
        </div>
      </el-card>

      <!-- 商品列表 -->
      <el-card class="items-card">
        <template #header>
          <span>商品信息</span>
        </template>
        <div class="order-items">
          <div class="order-item" v-for="item in order.items" :key="item.id">
            <el-image
              :src="item.productImage || 'https://via.placeholder.com/80?text=Product'"
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
            <div class="item-subtotal">
              ¥{{ (item.price * item.quantity).toFixed(2) }}
            </div>
          </div>
        </div>
        <div class="order-summary">
          <div class="summary-row">
            <span>商品总数</span>
            <span>{{ totalCount }} 件</span>
          </div>
          <div class="summary-row total">
            <span>订单总额</span>
            <span class="total-price">¥{{ order.totalAmount?.toFixed(2) }}</span>
          </div>
        </div>
      </el-card>

      <!-- 订单信息 -->
      <el-card class="info-card">
        <template #header>
          <span>订单信息</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatTime(order.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ order.payTime ? formatTime(order.payTime) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ getStatusText(order.status) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ order.remark || '无' }}</el-descriptions-item>
          <el-descriptions-item label="拒绝原因" :span="2" v-if="order.status === 'REJECTED'">
            <span class="reject-reason">{{ order.rejectReason }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Shop, Location, Phone, Picture } from '@element-plus/icons-vue'
import { getOrderById, cancelOrder as cancelOrderApi, payOrder as payOrderApi } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

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

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
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

.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.order-content .el-card {
  margin-bottom: 20px;
}

.status-card .status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.order-no {
  color: #666;
  font-size: 14px;
}

.order-steps {
  margin-top: 20px;
}

.pickup-card .pickup-info {
  text-align: center;
  padding: 20px;
}

.pickup-code {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.pickup-code .label {
  font-size: 14px;
  color: #666;
}

.pickup-code .code {
  font-size: 48px;
  font-weight: bold;
  color: #409eff;
  letter-spacing: 8px;
}

.pickup-tip {
  margin-top: 15px;
  color: #999;
  font-size: 14px;
}

.store-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

.info-item .el-icon {
  color: #999;
}

.order-items {
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
  margin-bottom: 15px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 0;
}

.order-item:not(:last-child) {
  border-bottom: 1px dashed #eee;
}

.item-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
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
}

.item-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.item-price {
  font-size: 12px;
  color: #999;
}

.item-subtotal {
  font-size: 14px;
  font-weight: bold;
  color: #333;
}

.order-summary {
  text-align: right;
}

.summary-row {
  padding: 8px 0;
  color: #666;
}

.summary-row.total {
  font-size: 16px;
  font-weight: bold;
}

.total-price {
  color: #f56c6c;
  font-size: 20px;
}

.reject-reason {
  color: #f56c6c;
}
</style>