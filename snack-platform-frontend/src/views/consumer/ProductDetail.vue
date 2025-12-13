<template>
  <div class="product-detail">
    <!-- 返回按钮 -->
    <div class="back-header">
      <el-button text @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <el-empty description="商品不存在或加载失败">
        <template #image>
          <span style="font-size: 80px;">😢</span>
        </template>
        <el-button type="primary" @click="goBack">返回商品列表</el-button>
      </el-empty>
    </div>

    <!-- 商品详情 -->
    <div v-else-if="sku" class="detail-content">
      <!-- 商品图片 -->
      <div class="product-image-section">
        <el-image
          :src="sku.product?.image || 'https://via.placeholder.com/400?text=Product'"
          fit="cover"
          class="main-image"
        >
          <template #error>
            <div class="image-placeholder">
              <span class="placeholder-emoji">🍿</span>
            </div>
          </template>
        </el-image>
        <!-- 库存徽章 -->
        <div class="stock-badge sold-out" v-if="sku.stock <= 0">
          <span>已售罄</span>
        </div>
        <div class="stock-badge low-stock" v-else-if="sku.stock <= 10">
          <span>仅剩{{ sku.stock }}件</span>
        </div>
        <!-- 特价徽章 -->
        <div class="discount-badge" v-if="sku.product?.price > sku.price">
          <span>特价</span>
        </div>
      </div>

      <!-- 商品信息 -->
      <div class="product-info-section">
        <div class="info-card">
          <h1 class="product-name">{{ sku.product?.name }}</h1>
          
          <div class="product-price">
            <span class="current-price">
              <span class="currency">¥</span>
              <span class="amount">{{ Math.floor(sku.price) }}</span>
              <span class="decimal">.{{ ((sku.price % 1) * 100).toFixed(0).padStart(2, '0') }}</span>
            </span>
            <span class="original-price" v-if="sku.product?.price > sku.price">
              ¥{{ sku.product?.price?.toFixed(2) }}
            </span>
          </div>

          <div class="product-meta">
            <div class="meta-item">
              <span class="meta-label">分类：</span>
              <el-tag size="small" type="info">{{ sku.product?.category?.name || '未分类' }}</el-tag>
            </div>
            <div class="meta-item">
              <span class="meta-label">库存：</span>
              <span :class="['stock-text', { 'low': sku.stock <= 10, 'out': sku.stock <= 0 }]">
                {{ sku.stock > 0 ? sku.stock + ' 件' : '已售罄' }}
              </span>
            </div>
          </div>
        </div>

        <!-- 商品描述 -->
        <div class="description-card">
          <h3 class="section-title">商品描述</h3>
          <p class="description-text">
            {{ sku.product?.description || '美味零食，品质保证，新鲜直达！' }}
          </p>
        </div>

        <!-- 购买操作 -->
        <div class="action-card">
          <div class="quantity-row">
            <span class="quantity-label">数量：</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="sku.stock"
              :disabled="sku.stock <= 0"
              size="large"
            />
          </div>
          
          <el-button
            type="primary"
            size="large"
            class="add-cart-btn"
            :disabled="sku.stock <= 0"
            @click="addToCart"
          >
            <el-icon><ShoppingCart /></el-icon>
            <span>{{ sku.stock <= 0 ? '已售罄' : '加入购物车' }}</span>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ShoppingCart } from '@element-plus/icons-vue'
import { getStoreSku } from '@/api/sku'
import { useCartStore } from '@/stores'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const storeId = ref(route.params.storeId)
const skuId = ref(route.params.skuId)
const sku = ref(null)
const loading = ref(true)
const error = ref(false)
const quantity = ref(1)

const goBack = () => {
  router.back()
}

const fetchSkuDetail = async () => {
  loading.value = true
  error.value = false
  try {
    const res = await getStoreSku(storeId.value, skuId.value)
    sku.value = res.data
    // 设置当前门店
    cartStore.setCurrentStore(storeId.value)
  } catch (err) {
    console.error('获取商品详情失败:', err)
    error.value = true
  } finally {
    loading.value = false
  }
}

const addToCart = () => {
  if (!sku.value || sku.value.stock <= 0) {
    ElMessage.warning('商品已售罄')
    return
  }
  
  if (quantity.value > sku.value.stock) {
    ElMessage.warning('购买数量不能超过库存')
    return
  }
  
  // 添加到购物车
  for (let i = 0; i < quantity.value; i++) {
    cartStore.addItem({
      skuId: sku.value.id,
      productId: sku.value.productId,
      productName: sku.value.product?.name,
      productImage: sku.value.product?.image,
      price: sku.value.price,
      quantity: 1,
      stock: sku.value.stock
    })
  }
  
  ElMessage({
    message: `已添加 ${quantity.value} 件 ${sku.value.product?.name} 到购物车 🛒`,
    type: 'success',
    duration: 2000
  })
  
  // 重置数量
  quantity.value = 1
}

onMounted(() => {
  fetchSkuDetail()
})
</script>

<style scoped>
.product-detail {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 16px 24px;
}

/* 返回按钮 */
.back-header {
  padding: 16px 0;
}

.back-btn {
  font-size: 15px;
  color: #666;
}

.back-btn:hover {
  color: #FF6B6B;
}

/* 加载状态 */
.loading-container {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* 错误状态 */
.error-container {
  background: #fff;
  border-radius: 16px;
  padding: 48px 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  text-align: center;
}

/* 商品详情内容 */
.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 商品图片区域 */
.product-image-section {
  position: relative;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.main-image {
  width: 100%;
  height: 300px;
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
  font-size: 80px;
}

.stock-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.stock-badge.sold-out {
  background: rgba(245, 108, 108, 0.9);
  color: #fff;
}

.stock-badge.low-stock {
  background: rgba(230, 162, 60, 0.9);
  color: #fff;
}

.discount-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  color: #fff;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

/* 商品信息区域 */
.product-info-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card,
.description-card,
.action-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.product-name {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  margin: 0 0 16px;
  line-height: 1.4;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 20px;
}

.current-price {
  color: #FF6B6B;
  font-weight: 700;
}

.current-price .currency {
  font-size: 18px;
}

.current-price .amount {
  font-size: 32px;
}

.current-price .decimal {
  font-size: 18px;
}

.original-price {
  font-size: 16px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.product-meta {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-label {
  font-size: 14px;
  color: #999;
}

.stock-text {
  font-size: 14px;
  color: #67C23A;
  font-weight: 500;
}

.stock-text.low {
  color: #E6A23C;
}

.stock-text.out {
  color: #F56C6C;
}

/* 商品描述 */
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px;
}

.description-text {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  margin: 0;
}

/* 购买操作 */
.quantity-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.quantity-label {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

.add-cart-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border: none;
  border-radius: 12px;
}

.add-cart-btn:hover {
  background: linear-gradient(135deg, #FF5252 0%, #FF7043 100%);
}

.add-cart-btn:disabled {
  background: #e4e7ed;
  color: #c0c4cc;
}

/* 平板和桌面端优化 */
@media (min-width: 768px) {
  .product-detail {
    padding: 0 24px 32px;
  }
  
  .detail-content {
    flex-direction: row;
    align-items: flex-start;
  }
  
  .product-image-section {
    flex: 0 0 45%;
    position: sticky;
    top: 100px;
  }
  
  .main-image {
    height: 400px;
  }
  
  .product-info-section {
    flex: 1;
  }
  
  .product-name {
    font-size: 26px;
  }
  
  .current-price .amount {
    font-size: 36px;
  }
  
  .product-meta {
    flex-direction: row;
    gap: 24px;
  }
}

/* 大屏幕优化 */
@media (min-width: 1024px) {
  .main-image {
    height: 450px;
  }
}
</style>