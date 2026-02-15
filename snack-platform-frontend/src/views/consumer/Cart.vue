<template>
  <div class="cart-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-text">
          <h1>购物车</h1>
          <p v-if="cartStore.items.length > 0">共 {{ cartStore.totalCount }} 件商品</p>
        </div>
      </div>
      <el-button type="primary" class="continue-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>继续购物</span>
      </el-button>
    </div>

    <div class="cart-content" v-if="cartStore.items.length > 0">
      <!-- 当前门店信息 -->
      <div class="store-info-bar" v-if="currentStoreName">
        <div class="store-badge">
          <el-icon :size="16"><Shop /></el-icon>
          <span class="store-name">{{ currentStoreName }}</span>
        </div>
      </div>

      <!-- 桌面端表格视图 -->
      <div class="desktop-view">
        <div class="cart-table">
          <div class="table-header">
            <div class="col-product">商品信息</div>
            <div class="col-price">单价</div>
            <div class="col-quantity">数量</div>
            <div class="col-subtotal">小计</div>
            <div class="col-action">操作</div>
          </div>
          <div class="table-body">
            <div class="cart-row" v-for="item in cartStore.items" :key="item.skuId">
              <div class="col-product">
                <div class="product-cell">
                  <el-image
                    :src="item.productImage || 'https://via.placeholder.com/80?text=Product'"
                    fit="cover"
                    class="product-image"
                  >
                    <template #error>
                      <div class="image-placeholder">
                        <el-icon :size="24"><Goods /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div class="product-info">
                    <div class="product-name">{{ item.productName }}</div>
                    <div class="product-stock">
                      <el-tag size="small" :type="item.stock > 10 ? 'success' : 'warning'">
                        库存 {{ item.stock }}
                      </el-tag>
                    </div>
                  </div>
                </div>
              </div>
              <div class="col-price">
                <span class="price">¥{{ item.price?.toFixed(2) }}</span>
              </div>
              <div class="col-quantity">
                <el-input-number
                  v-model="item.quantity"
                  :min="1"
                  :max="item.stock"
                  size="default"
                  controls-position="right"
                  @change="(val) => updateQuantity(item, val)"
                />
              </div>
              <div class="col-subtotal">
                <span class="subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
              </div>
              <div class="col-action">
                <el-button
                  type="danger"
                  text
                  @click="removeItem(item)"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

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
                  <el-icon :size="24"><Goods /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="item-info">
              <div class="item-name">{{ item.productName }}</div>
              <div class="item-stock">
                <el-tag size="small" :type="item.stock > 10 ? 'success' : 'warning'">
                  库存 {{ item.stock }}
                </el-tag>
              </div>
              <div class="item-price">
                <span class="currency">¥</span>
                <span class="amount">{{ item.price?.toFixed(2) }}</span>
              </div>
            </div>
            <el-button
              type="danger"
              text
              size="small"
              class="delete-btn"
              @click="removeItem(item)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <div class="item-footer">
            <el-input-number
              v-model="item.quantity"
              :min="1"
              :max="item.stock"
              size="small"
              controls-position="right"
              @change="(val) => updateQuantity(item, val)"
            />
            <div class="item-subtotal">
              <span class="label">小计</span>
              <span class="value">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 结算栏 -->
      <div class="cart-footer">
        <div class="footer-left">
          <el-button text type="danger" @click="clearCart">
            <el-icon><Delete /></el-icon>
            清空购物车
          </el-button>
        </div>
        <div class="footer-right">
          <div class="summary-info">
            <div class="summary-row">
              <span class="label">商品数量</span>
              <span class="value">{{ cartStore.totalCount }} 件</span>
            </div>
            <div class="summary-row total">
              <span class="label">合计</span>
              <span class="total-price">
                <span class="currency">¥</span>
                <span class="amount">{{ Math.floor(cartStore.totalPrice) }}</span>
                <span class="decimal">.{{ ((cartStore.totalPrice % 1) * 100).toFixed(0).padStart(2, '0') }}</span>
              </span>
            </div>
          </div>
          <el-button type="primary" size="large" class="checkout-btn" @click="checkout">
            去结算
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空购物车 -->
    <div class="empty-cart" v-else>
      <div class="empty-content">
        <el-icon :size="48" class="empty-icon"><ShoppingCart /></el-icon>
        <h3>购物车是空的</h3>
        <p>快去挑选心仪的零食吧~</p>
        <el-button type="primary" size="large" @click="goToStores">
          去逛逛
        </el-button>
      </div>
    </div>

    <!-- 结算对话框 -->
    <el-dialog
      v-model="checkoutDialogVisible"
      title=""
      width="500px"
      class="checkout-dialog"
    >
      <template #header>
        <div class="dialog-header">
          <el-icon :size="20"><Document /></el-icon>
          <span class="dialog-title">确认订单</span>
        </div>
      </template>

      <div class="order-summary">
        <div class="summary-section">
          <div class="section-title">
            <el-icon><Shop /></el-icon>
            取货门店
          </div>
          <div class="section-content store-name">{{ currentStoreName }}</div>
        </div>

        <div class="summary-section">
          <div class="section-title">
            <el-icon><ShoppingCart /></el-icon>
            商品清单
          </div>
          <div class="section-content">
            <div class="order-items">
              <div class="order-item" v-for="item in cartStore.items" :key="item.skuId">
                <span class="item-name">{{ item.productName }}</span>
                <span class="item-qty">x{{ item.quantity }}</span>
                <span class="item-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="summary-section total-section">
          <div class="section-title">
            <el-icon><Wallet /></el-icon>
            订单金额
          </div>
          <div class="section-content">
            <span class="order-total">¥{{ cartStore.totalPrice.toFixed(2) }}</span>
          </div>
        </div>

        <div class="summary-section">
          <div class="section-title">
            <el-icon><Edit /></el-icon>
            备注信息
          </div>
          <div class="section-content">
            <el-input
              v-model="orderForm.remark"
              type="textarea"
              placeholder="请输入备注信息（选填）"
              :rows="3"
            />
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="checkoutDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitOrder">
            提交订单
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Delete, Shop, ShoppingCart, Wallet, Edit, Goods } from '@element-plus/icons-vue'
import { Document } from '@element-plus/icons-vue'
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
  cartStore.updateQuantity(item.skuId, val)
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

onMounted(async () => {
  // 从后端同步购物车数据
  await cartStore.fetchCart()
  fetchStoreName()
})
</script>

<style scoped>
.cart-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px 100px;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 8px;
}

.header-left {
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

.continue-btn {
  background: var(--color-primary);
  border: none;
  padding: 12px 24px;
}

.continue-btn:hover {
  background: var(--color-primary-hover);
}

/* 购物车内容 */
.cart-content {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--color-border);
}

/* 门店信息栏 */
.store-info-bar {
  padding: 16px 24px;
  background: var(--color-primary-light);
  border-bottom: 1px solid var(--color-border);
}

.store-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: var(--color-surface);
  padding: 8px 16px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.store-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

/* 桌面端表格 */
.desktop-view {
  padding: 0 24px;
}

.cart-table {
  width: 100%;
}

.table-header {
  display: flex;
  padding: 16px 0;
  border-bottom: 1px solid var(--color-border);
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-secondary);
}

.table-body {
  padding: 8px 0;
}

.cart-row {
  display: flex;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid var(--color-border);
  transition: background var(--transition-fast);
}

.cart-row:hover {
  background: var(--color-bg);
}

.cart-row:last-child {
  border-bottom: none;
}

.col-product {
  flex: 2;
  min-width: 300px;
}

.col-price,
.col-quantity,
.col-subtotal,
.col-action {
  flex: 1;
  text-align: center;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 16px;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg);
  color: var(--color-text-muted);
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}

.product-stock {
  font-size: 12px;
}

.price {
  font-size: 15px;
  color: var(--color-text-secondary);
}

.subtotal {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-primary);
}

/* 移动端卡片 */
.mobile-cards {
  display: none;
  padding: 16px;
}

.cart-item-card {
  background: var(--color-bg);
  border-radius: var(--radius-lg);
  padding: 16px;
  margin-bottom: 12px;
}

.item-main {
  display: flex;
  gap: 12px;
  position: relative;
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-md);
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.item-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

.item-price {
  color: var(--color-primary);
  font-weight: 600;
}

.item-price .currency {
  font-size: 12px;
}

.item-price .amount {
  font-size: 16px;
}

.delete-btn {
  position: absolute;
  top: 0;
  right: 0;
}

.item-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}

.item-subtotal {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.item-subtotal .label {
  font-size: 12px;
  color: var(--color-text-muted);
}

.item-subtotal .value {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-primary);
}

/* 结算栏 */
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: var(--color-bg);
  border-top: 1px solid var(--color-border);
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 32px;
}

.summary-info {
  text-align: right;
}

.summary-row {
  display: flex;
  align-items: baseline;
  justify-content: flex-end;
  gap: 12px;
  margin-bottom: 4px;
}

.summary-row .label {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.summary-row .value {
  font-size: 14px;
  color: var(--color-text);
}

.summary-row.total .label {
  font-size: 16px;
  font-weight: 600;
}

.total-price {
  color: var(--color-primary);
  font-weight: 700;
}

.total-price .currency {
  font-size: 16px;
}

.total-price .amount {
  font-size: 28px;
}

.total-price .decimal {
  font-size: 16px;
}

.checkout-btn {
  background: var(--color-primary);
  border: none;
  padding: 14px 40px;
  font-size: 16px;
  font-weight: 600;
}

.checkout-btn:hover {
  background: var(--color-primary-hover);
}

/* 空购物车 */
.empty-cart {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 80px 24px;
  text-align: center;
  border: 1px solid var(--color-border);
}

.empty-icon {
  color: var(--color-text-muted);
  margin-bottom: 24px;
}

.empty-content h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 8px;
}

.empty-content p {
  font-size: 14px;
  color: var(--color-text-muted);
  margin: 0 0 24px;
}

.empty-content .el-button {
  background: var(--color-primary);
  border: none;
  padding: 14px 40px;
}

.empty-content .el-button:hover {
  background: var(--color-primary-hover);
}

/* 结算对话框 */
.checkout-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-border);
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--color-text);
}

.dialog-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
}

.order-summary {
  padding: 8px 0;
}

.summary-section {
  padding: 16px 0;
  border-bottom: 1px solid var(--color-border);
}

.summary-section:last-child {
  border-bottom: none;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-secondary);
  margin-bottom: 12px;
}

.section-content {
  padding-left: 24px;
}

.section-content.store-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
}

.order-items {
  max-height: 150px;
  overflow-y: auto;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
}

.order-item .item-name {
  flex: 1;
  color: var(--color-text);
}

.order-item .item-qty {
  width: 50px;
  text-align: center;
  color: var(--color-text-muted);
}

.order-item .item-price {
  width: 80px;
  text-align: right;
  color: var(--color-primary);
  font-weight: 500;
}

.total-section .section-content {
  text-align: right;
  padding-right: 0;
}

.order-total {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-primary);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button--primary {
  background: var(--color-primary);
  border: none;
}

.dialog-footer .el-button--primary:hover {
  background: var(--color-primary-hover);
}

/* 移动端优化 */
@media (max-width: 768px) {
  .cart-page {
    padding: 0 12px 120px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    margin-bottom: 16px;
  }

  .header-text h1 {
    font-size: 22px;
  }

  .continue-btn {
    width: 100%;
  }

  .cart-content {
    border-radius: var(--radius-lg);
  }

  .store-info-bar {
    padding: 12px 16px;
  }

  /* 隐藏桌面端表格 */
  .desktop-view {
    display: none;
  }

  /* 显示移动端卡片 */
  .mobile-cards {
    display: block;
  }

  /* 固定底部结算栏 */
  .cart-footer {
    position: fixed;
    bottom: 60px;
    left: 0;
    right: 0;
    padding: 12px 16px;
    background: var(--color-surface);
    box-shadow: var(--shadow-md);
    z-index: 99;
    flex-direction: row;
  }

  .footer-left {
    display: none;
  }

  .footer-right {
    width: 100%;
    gap: 16px;
  }

  .summary-info {
    flex: 1;
  }

  .summary-row {
    justify-content: flex-start;
  }

  .total-price .amount {
    font-size: 22px;
  }

  .checkout-btn {
    padding: 12px 24px;
    font-size: 14px;
  }

  .empty-cart {
    padding: 60px 24px;
  }

  /* 对话框移动端适配 */
  .checkout-dialog :deep(.el-dialog) {
    width: 90% !important;
    margin: 0 auto;
  }
}

/* 平板优化 */
@media (min-width: 769px) and (max-width: 1024px) {
  .cart-page {
    padding: 0 20px 100px;
  }
}
</style>
