<template>
  <div class="cart-page">
    <div class="page-header">
      <h1>购物车</h1>
      <el-button type="primary" plain @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        继续购物
      </el-button>
    </div>

    <div class="cart-content" v-if="cartStore.items.length > 0">
      <!-- 桌面端表格视图 -->
      <el-table :data="cartStore.items" style="width: 100%" class="desktop-table">
        <el-table-column label="商品" min-width="300">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image
                :src="row.productImage || 'https://via.placeholder.com/80?text=Product'"
                fit="cover"
                class="product-image"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="product-info">
                <div class="product-name">{{ row.productName }}</div>
                <div class="product-stock">库存: {{ row.stock }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="单价" width="120" align="center">
          <template #default="{ row }">
            <span class="price">¥{{ row.price?.toFixed(2) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="数量" width="180" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              :max="row.stock"
              size="small"
              @change="(val) => updateQuantity(row, val)"
            />
          </template>
        </el-table-column>

        <el-table-column label="小计" width="120" align="center">
          <template #default="{ row }">
            <span class="subtotal">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button
              type="danger"
              text
              @click="removeItem(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 移动端卡片视图 -->
      <div class="mobile-cards">
        <div class="cart-item-card" v-for="item in cartStore.items" :key="item.skuId">
          <div class="item-main">
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
              <div class="item-stock">库存: {{ item.stock }}</div>
              <div class="item-price">¥{{ item.price?.toFixed(2) }}</div>
            </div>
          </div>
          <div class="item-actions">
            <el-input-number
              v-model="item.quantity"
              :min="1"
              :max="item.stock"
              size="small"
              @change="(val) => updateQuantity(item, val)"
            />
            <div class="item-subtotal">
              小计: <span class="subtotal-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
            </div>
            <el-button
              type="danger"
              text
              size="small"
              @click="removeItem(item)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>

      <div class="cart-footer">
        <div class="cart-actions">
          <el-button @click="clearCart">清空购物车</el-button>
        </div>
        <div class="cart-summary">
          <div class="summary-item">
            <span>共</span>
            <span class="highlight">{{ cartStore.totalCount }}</span>
            <span>件商品</span>
          </div>
          <div class="summary-item">
            <span>合计：</span>
            <span class="total-price">¥{{ cartStore.totalPrice.toFixed(2) }}</span>
          </div>
          <el-button type="primary" size="large" @click="checkout">
            去结算
          </el-button>
        </div>
      </div>
    </div>

    <el-empty v-else description="购物车是空的">
      <el-button type="primary" @click="goToStores">去逛逛</el-button>
    </el-empty>

    <!-- 结算对话框 -->
    <el-dialog
      v-model="checkoutDialogVisible"
      title="确认订单"
      width="500px"
    >
      <el-form :model="orderForm" label-width="100px">
        <el-form-item label="取货门店">
          <span>{{ currentStoreName }}</span>
        </el-form-item>
        <el-form-item label="商品数量">
          <span>{{ cartStore.totalCount }} 件</span>
        </el-form-item>
        <el-form-item label="订单金额">
          <span class="total-price">¥{{ cartStore.totalPrice.toFixed(2) }}</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="orderForm.remark"
            type="textarea"
            placeholder="请输入备注信息（选填）"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkoutDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitOrder">
          提交订单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Picture } from '@element-plus/icons-vue'
import { useCartStore } from '@/stores'
import { getStoreById } from '@/api/store'
import { createOrder } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()

const currentStoreName = ref('')
const checkoutDialogVisible = ref(false)
const submitting = ref(false)
const orderForm = ref({
  remark: ''
})

const goBack = () => {
  if (cartStore.currentStoreId) {
    router.push(`/stores/${cartStore.currentStoreId}/products`)
  } else {
    router.push('/stores')
  }
}

const goToStores = () => {
  router.push('/stores')
}

const updateQuantity = (item, val) => {
  cartStore.updateItemQuantity(item.skuId, val)
}

const removeItem = (item) => {
  ElMessageBox.confirm(
    `确定要删除 ${item.productName} 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    cartStore.removeItem(item.skuId)
    ElMessage.success('已删除')
  }).catch(() => {})
}

const clearCart = () => {
  ElMessageBox.confirm(
    '确定要清空购物车吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    cartStore.clearCart()
    ElMessage.success('购物车已清空')
  }).catch(() => {})
}

const checkout = () => {
  if (!cartStore.currentStoreId) {
    ElMessage.warning('请先选择门店')
    return
  }
  checkoutDialogVisible.value = true
}

const submitOrder = async () => {
  submitting.value = true
  try {
    const orderData = {
      storeId: cartStore.currentStoreId,
      remark: orderForm.value.remark,
      items: cartStore.items.map(item => ({
        skuId: item.skuId,
        quantity: item.quantity
      }))
    }
    
    const res = await createOrder(orderData)
    ElMessage.success('订单提交成功')
    checkoutDialogVisible.value = false
    cartStore.clearCart()
    router.push(`/orders/${res.data.id}`)
  } catch (error) {
    console.error('提交订单失败:', error)
    ElMessage.error(error.response?.data?.message || '提交订单失败')
  } finally {
    submitting.value = false
  }
}

const fetchStoreName = async () => {
  if (cartStore.currentStoreId) {
    try {
      const res = await getStoreById(cartStore.currentStoreId)
      currentStoreName.value = res.data.name
    } catch (error) {
      console.error('获取门店信息失败:', error)
    }
  }
}

onMounted(() => {
  fetchStoreName()
})
</script>

<style scoped>
.cart-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 10px;
  padding-bottom: 80px; /* 为固定底部留出空间 */
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 10px;
}

.page-header h1 {
  font-size: 24px;
  color: #333;
}

.cart-content {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 默认隐藏移动端卡片视图 */
.mobile-cards {
  display: none;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 15px;
}

.product-image {
  width: 80px;
  height: 80px;
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
  font-size: 24px;
  color: #c0c4cc;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 6px;
}

.product-stock {
  font-size: 12px;
  color: #999;
}

.price {
  color: #333;
}

.subtotal {
  font-weight: bold;
  color: #f56c6c;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.cart-summary {
  display: flex;
  align-items: center;
  gap: 20px;
}

.summary-item {
  font-size: 14px;
  color: #666;
}

.summary-item .highlight {
  color: #f56c6c;
  font-weight: bold;
  margin: 0 4px;
}

.total-price {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .cart-page {
    padding: 0 5px 100px 5px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 15px;
    padding: 0 10px;
  }
  
  .page-header h1 {
    font-size: 20px;
  }
  
  .page-header .el-button {
    width: 100%;
    height: 44px;
  }
  
  .cart-content {
    padding: 15px;
    margin-bottom: 20px;
  }
  
  /* 隐藏桌面端表格 */
  .desktop-table {
    display: none;
  }
  
  /* 显示移动端卡片 */
  .mobile-cards {
    display: block;
  }
  
  .cart-item-card {
    border: 1px solid #eee;
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 12px;
    background: #fafafa;
  }
  
  .item-main {
    display: flex;
    gap: 12px;
    margin-bottom: 12px;
  }
  
  .item-image {
    width: 80px;
    height: 80px;
    border-radius: 8px;
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
    font-size: 24px;
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
    font-weight: bold;
    color: #333;
    margin-bottom: 4px;
  }
  
  .item-stock {
    font-size: 12px;
    color: #999;
  }
  
  .item-price {
    font-size: 16px;
    font-weight: bold;
    color: #f56c6c;
  }
  
  .item-actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
    padding-top: 12px;
    border-top: 1px solid #eee;
  }
  
  .item-subtotal {
    flex: 1;
    text-align: center;
    font-size: 13px;
    color: #666;
  }
  
  .subtotal-price {
    font-weight: bold;
    color: #f56c6c;
  }
  
  /* 固定底部结算栏 */
  .cart-footer {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: #fff;
    padding: 15px;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
    z-index: 100;
    flex-direction: column;
    gap: 12px;
  }
  
  .cart-actions {
    order: 2;
  }
  
  .cart-summary {
    order: 1;
    width: 100%;
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  
  .summary-item {
    display: flex;
    justify-content: space-between;
    font-size: 14px;
  }
  
  .summary-item .highlight {
    font-size: 16px;
  }
  
  .total-price {
    font-size: 20px;
  }
  
  .cart-summary .el-button {
    width: 100%;
    height: 48px;
    font-size: 16px;
    margin-top: 8px;
  }
}

/* 平板优化 */
@media (min-width: 769px) and (max-width: 1024px) {
  .cart-page {
    padding: 0 15px;
  }
}
</style>