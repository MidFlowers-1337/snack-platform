<template>
  <div class="verify-page">
    <h2>订单核销</h2>

    <el-row :gutter="20">
      <!-- 核销输入 -->
      <el-col :span="12">
        <el-card class="verify-card">
          <template #header>
            <span>输入取货码</span>
          </template>
          <div class="verify-input">
            <el-input
              v-model="pickupCode"
              placeholder="请输入6位取货码"
              size="large"
              maxlength="6"
              @input="handlePickupCodeInput"
              @keyup.enter="verifyOrder"
              class="code-input"
            >
              <template #prefix>
                <el-icon><Ticket /></el-icon>
              </template>
            </el-input>
            <el-button
              type="primary"
              size="large"
              @click="verifyOrder"
              :loading="verifying"
              :disabled="!pickupCode || pickupCode.length !== 6"
            >
              核销
            </el-button>
          </div>
          <div class="verify-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>请输入顾客提供的6位取货码进行核销</span>
          </div>
        </el-card>
      </el-col>

      <!-- 待取货订单 -->
      <el-col :span="12">
        <el-card class="ready-orders-card">
          <template #header>
            <div class="card-header">
              <span>待取货订单</span>
              <el-badge :value="readyOrders.length" type="warning" />
            </div>
          </template>
          <div class="ready-orders" v-loading="loadingOrders">
            <div
              v-for="order in readyOrders"
              :key="order.id"
              class="ready-order-item"
              @click="selectOrder(order)"
            >
              <div class="order-info">
                <div class="order-no">{{ order.orderNo }}</div>
                <div class="order-time">{{ formatTime(order.createTime) }}</div>
              </div>
              <div class="order-code">
                <span class="code">{{ order.pickupCode }}</span>
              </div>
              <div class="order-amount">
                ¥{{ order.totalAmount?.toFixed(2) }}
              </div>
            </div>
            <el-empty v-if="readyOrders.length === 0" description="暂无待取货订单" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 核销结果 -->
    <el-card class="result-card" v-if="verifyResult">
      <template #header>
        <div class="result-header">
          <el-icon :class="verifyResult.success ? 'success' : 'error'">
            <CircleCheckFilled v-if="verifyResult.success" />
            <CircleCloseFilled v-else />
          </el-icon>
          <span>{{ verifyResult.success ? '核销成功' : '核销失败' }}</span>
        </div>
      </template>
      
      <div v-if="verifyResult.success" class="success-message">
        <div>{{ verifyResult.message }}</div>
        <div class="success-time">{{ formatTime(new Date()) }}</div>
      </div>

      <div v-else class="error-message">
        {{ verifyResult.message }}
      </div>
    </el-card>

    <!-- 今日核销记录 -->
    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <span>今日核销记录</span>
          <span class="count">共 {{ completedOrders.length }} 单</span>
        </div>
      </template>
      <el-table :data="completedOrders" style="width: 100%" max-height="300">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="pickupCode" label="取货码" width="100" align="center">
          <template #default="{ row }">
            <span class="pickup-code">{{ row.pickupCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="100" align="center">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="completeTime" label="核销时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.completeTime) }}
          </template>
        </el-table-column>
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">
            <span v-for="(item, index) in row.items?.slice(0, 2)" :key="item.id">
              {{ item.productName }} x{{ item.quantity }}
              <span v-if="index < Math.min(row.items.length, 2) - 1">、</span>
            </span>
            <span v-if="row.items?.length > 2">等</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="completedOrders.length === 0" description="今日暂无核销记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Ticket, InfoFilled, CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import { getStoreOrders, verifyPickup } from '@/api/order'
import { ElMessage } from 'element-plus'
import { formatTime } from '@/utils/format'

const pickupCode = ref('')
const verifying = ref(false)
const verifyResult = ref(null)
const loadingOrders = ref(false)
const readyOrders = ref([])
const completedOrders = ref([])

let refreshInterval = null

const selectOrder = (order) => {
  pickupCode.value = order.pickupCode
}

const handlePickupCodeInput = (value) => {
  pickupCode.value = String(value || '').replace(/\D/g, '').slice(0, 6)
}

const verifyOrder = async () => {
  if (!pickupCode.value || pickupCode.value.length !== 6) {
    ElMessage.warning('请输入6位取货码')
    return
  }

  verifying.value = true
  verifyResult.value = null

  try {
    await verifyPickup(pickupCode.value)
    const verifiedCode = pickupCode.value
    verifyResult.value = {
      success: true,
      message: `核销成功（取货码：${verifiedCode}）`
    }
    ElMessage.success('核销成功')
    pickupCode.value = ''
    await fetchOrders()
  } catch (error) {
    verifyResult.value = {
      success: false,
      message: error.response?.data?.message || '核销失败，请检查取货码是否正确'
    }
    ElMessage.error(verifyResult.value.message)
  } finally {
    verifying.value = false
  }
}

const fetchOrders = async () => {
  loadingOrders.value = true
  try {
    // 获取待取货订单
    const readyRes = await getStoreOrders({ status: 'READY', pageNum: 1, pageSize: 100 })
    readyOrders.value = readyRes.data.records || readyRes.data || []

    // 获取今日已完成订单
    const today = new Date().toISOString().split('T')[0]
    const completedRes = await getStoreOrders({
      status: 'COMPLETED',
      startDate: today,
      endDate: today,
      pageNum: 1,
      pageSize: 100
    })
    completedOrders.value = completedRes.data.records || completedRes.data || []
  } catch (error) {
    console.error('获取订单失败:', error)
  } finally {
    loadingOrders.value = false
  }
}

onMounted(() => {
  fetchOrders()
  // 每30秒刷新一次
  refreshInterval = setInterval(fetchOrders, 30000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<style scoped>
.verify-page h2 {
  margin-bottom: 20px;
  color: #333;
}

.verify-card {
  margin-bottom: 20px;
}

.verify-input {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.code-input {
  flex: 1;
}

.code-input :deep(.el-input__inner) {
  font-size: 24px;
  letter-spacing: 8px;
  text-align: center;
  font-weight: bold;
}

.verify-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
}

.ready-orders-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ready-orders {
  max-height: 300px;
  overflow-y: auto;
}

.ready-order-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s;
}

.ready-order-item:hover {
  background: #f5f7fa;
}

.ready-order-item:not(:last-child) {
  border-bottom: 1px solid #eee;
}

.ready-order-item .order-info {
  flex: 1;
}

.ready-order-item .order-no {
  font-size: 14px;
  color: #333;
}

.ready-order-item .order-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.ready-order-item .order-code .code {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  font-family: monospace;
  letter-spacing: 4px;
}

.ready-order-item .order-amount {
  font-size: 16px;
  font-weight: bold;
  color: #f56c6c;
  margin-left: 20px;
}

.result-card {
  margin-bottom: 20px;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: bold;
}

.result-header .el-icon {
  font-size: 24px;
}

.result-header .el-icon.success {
  color: #67c23a;
}

.result-header .el-icon.error {
  color: #f56c6c;
}

.price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.error-message {
  padding: 20px;
  text-align: center;
  color: #f56c6c;
  font-size: 16px;
}

.success-message {
  padding: 20px;
  text-align: center;
  color: #67c23a;
  font-size: 16px;
}

.success-time {
  margin-top: 8px;
  color: #909399;
  font-size: 14px;
}

h4 {
  margin: 20px 0 10px;
  color: #333;
}

.history-card .count {
  font-size: 14px;
  color: #999;
  font-weight: normal;
}

.pickup-code {
  font-family: monospace;
  font-weight: bold;
  color: #409eff;
}
</style>
