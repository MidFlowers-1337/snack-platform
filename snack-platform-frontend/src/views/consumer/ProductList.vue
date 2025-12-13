<template>
  <div class="product-list">
    <!-- 门店信息头部 -->
    <div class="store-header" v-if="currentStore">
      <div class="store-banner" :style="{ background: getStoreGradient(currentStore.id) }">
        <div class="store-banner-content">
          <div class="store-icon">🏪</div>
          <div class="store-details">
            <h2>{{ currentStore.name }}</h2>
            <div class="store-meta">
              <span class="store-address">
                <el-icon><Location /></el-icon>
                {{ currentStore.address }}
              </span>
              <span class="store-status" :class="{ open: currentStore.status === 1 }">
                <span class="status-dot"></span>
                {{ currentStore.status === 1 ? '营业中' : '休息中' }}
              </span>
            </div>
          </div>
        </div>
      </div>
      <div class="store-actions">
        <div class="product-count">
          <span class="count-number">{{ total }}</span>
          <span class="count-label">件商品</span>
        </div>
        <el-button type="primary" round class="cart-btn" @click="goToCart">
          <el-icon><ShoppingCart /></el-icon>
          <span>购物车</span>
          <el-badge :value="cartStore.totalCount" v-if="cartStore.totalCount > 0" :max="99" />
        </el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 分类侧边栏 -->
      <el-col :xs="0" :sm="6" :md="5" :lg="4">
        <div class="category-sidebar">
          <div class="category-header">
            <el-icon><Grid /></el-icon>
            <span>商品分类</span>
          </div>
          <div class="category-list">
            <div 
              class="category-item" 
              :class="{ active: currentCategory === 'all' }"
              @click="handleCategorySelect('all')"
            >
              <span class="category-icon">📦</span>
              <span class="category-name">全部商品</span>
              <span class="category-count">{{ total }}</span>
            </div>
            <div 
              v-for="cat in categories"
              :key="cat.id"
              class="category-item"
              :class="{ active: currentCategory === String(cat.id) }"
              @click="handleCategorySelect(String(cat.id))"
            >
              <span class="category-icon">{{ getCategoryIcon(cat.name) }}</span>
              <span class="category-name">{{ cat.name }}</span>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 商品列表 -->
      <el-col :xs="24" :sm="18" :md="19" :lg="20">
        <div class="product-content">
          <!-- 搜索栏 -->
          <div class="search-bar">
            <div class="search-input-wrapper">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索您想要的零食..."
                clearable
                size="large"
                @clear="handleSearch"
                @keyup.enter="handleSearch"
              >
                <template #prefix>
                  <el-icon class="search-icon"><Search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" size="large" class="search-btn" @click="handleSearch">
                搜索
              </el-button>
            </div>
            <div class="search-tags" v-if="!searchKeyword">
              <span class="tag-label">热门：</span>
              <el-tag 
                v-for="tag in hotTags" 
                :key="tag" 
                class="hot-tag"
                @click="searchKeyword = tag; handleSearch()"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>

          <!-- 商品网格 -->
          <div class="product-grid" v-loading="loading">
            <div class="product-card" v-for="sku in skuList" :key="sku.id">
              <div class="product-image">
                <el-image
                  :src="sku.product?.image || 'https://via.placeholder.com/200?text=Product'"
                  fit="cover"
                >
                  <template #error>
                    <div class="image-placeholder">
                      <span class="placeholder-emoji">🍿</span>
                    </div>
                  </template>
                </el-image>
                <div class="stock-badge sold-out" v-if="sku.stock <= 0">
                  <span>已售罄</span>
                </div>
                <div class="stock-badge low-stock" v-else-if="sku.stock <= 10">
                  <span>仅剩{{ sku.stock }}件</span>
                </div>
                <div class="discount-badge" v-if="sku.product?.price > sku.price">
                  <span>特价</span>
                </div>
              </div>

              <div class="product-info">
                <div class="product-name">{{ sku.product?.name }}</div>
                <div class="product-desc">{{ sku.product?.description || '美味零食，品质保证' }}</div>
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
              </div>

              <div class="product-action">
                <div class="quantity-control">
                  <el-input-number
                    v-model="sku.quantity"
                    :min="1"
                    :max="sku.stock"
                    :disabled="sku.stock <= 0"
                    size="small"
                    controls-position="right"
                    @change="(val) => handleQuantityChange(sku, val)"
                  />
                </div>
                <el-button
                  type="primary"
                  round
                  class="add-cart-btn"
                  :disabled="sku.stock <= 0"
                  @click="addToCart(sku)"
                >
                  <el-icon><ShoppingCart /></el-icon>
                  <span>加入购物车</span>
                </el-button>
              </div>
            </div>
          </div>

          <el-empty v-if="!loading && skuList.length === 0" description="暂无商品">
            <template #image>
              <span style="font-size: 80px;">🔍</span>
            </template>
          </el-empty>

          <!-- 分页 -->
          <div class="pagination" v-if="total > 0">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[12, 24, 36, 48]"
              layout="total, sizes, prev, pager, next"
              background
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Location, ShoppingCart, Grid, Search, Picture } from '@element-plus/icons-vue'
import { getStoreById } from '@/api/store'
import { getCategories } from '@/api/product'
import { getStoreSkus } from '@/api/sku'
import { useCartStore } from '@/stores'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const storeId = computed(() => route.params.storeId)
const currentStore = ref(null)
const categories = ref([])
const skuList = ref([])
const loading = ref(false)

const currentCategory = ref('all')
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 热门搜索标签
const hotTags = ['薯片', '巧克力', '坚果', '饼干', '糖果']

// 门店渐变色
const getStoreGradient = (id) => {
  const gradients = [
    'linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%)',
    'linear-gradient(135deg, #4ECDC4 0%, #44A08D 100%)',
    'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  ]
  return gradients[(id || 0) % gradients.length]
}

// 分类图标
const getCategoryIcon = (name) => {
  const icons = {
    '薯片': '🥔',
    '饼干': '🍪',
    '糖果': '🍬',
    '巧克力': '🍫',
    '坚果': '🥜',
    '果干': '🍇',
    '肉类': '🥓',
    '饮料': '🥤',
    '膨化': '🍿',
    '蛋糕': '🍰',
  }
  for (const key in icons) {
    if (name.includes(key)) return icons[key]
  }
  return '🍿'
}

const goToCart = () => {
  router.push('/cart')
}

const handleCategorySelect = (index) => {
  currentCategory.value = index
  currentPage.value = 1
  fetchSkus()
}

const handleSearch = () => {
  currentPage.value = 1
  fetchSkus()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchSkus()
}

const handlePageChange = () => {
  fetchSkus()
}

const handleQuantityChange = (sku, val) => {
  sku.quantity = val || 1
}

const addToCart = (sku) => {
  if (!sku.quantity || sku.quantity <= 0) {
    sku.quantity = 1
  }
  
  cartStore.addItem({
    skuId: sku.id,
    productId: sku.productId,
    productName: sku.product?.name,
    productImage: sku.product?.image,
    price: sku.price,
    quantity: sku.quantity,
    stock: sku.stock
  })
  
  ElMessage({
    message: `已添加 ${sku.quantity} 件 ${sku.product?.name} 到购物车 🛒`,
    type: 'success',
    duration: 2000
  })
  sku.quantity = 1
}

const fetchStore = async () => {
  try {
    const res = await getStoreById(storeId.value)
    currentStore.value = res.data
    cartStore.setCurrentStore(storeId.value)
  } catch (error) {
    console.error('获取门店信息失败:', error)
  }
}

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const fetchSkus = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    if (currentCategory.value !== 'all') {
      params.categoryId = currentCategory.value
    }
    
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    const res = await getStoreSkus(storeId.value, params)
    skuList.value = (res.data.records || res.data || []).map(sku => ({
      ...sku,
      quantity: 1
    }))
    total.value = res.data.total || skuList.value.length
  } catch (error) {
    console.error('获取商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStore()
  fetchCategories()
  fetchSkus()
})
</script>

<style scoped>
.product-list {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 10px;
}

/* 门店头部 */
.store-header {
  background: #fff;
  border-radius: 16px;
  margin-bottom: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.store-banner {
  padding: 24px;
  color: #fff;
}

.store-banner-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.store-icon {
  font-size: 48px;
  background: rgba(255, 255, 255, 0.2);
  width: 72px;
  height: 72px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.store-details h2 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 700;
}

.store-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  opacity: 0.9;
}

.store-address {
  display: flex;
  align-items: center;
  gap: 4px;
}

.store-status {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
}

.store-status.open .status-dot {
  background: #67C23A;
  box-shadow: 0 0 8px rgba(103, 194, 58, 0.6);
}

.store-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fff;
}

.product-count {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.count-number {
  font-size: 28px;
  font-weight: 700;
  color: #FF6B6B;
}

.count-label {
  font-size: 14px;
  color: #999;
}

.cart-btn {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border: none;
  padding: 12px 24px;
  font-size: 15px;
}

.cart-btn:hover {
  background: linear-gradient(135deg, #FF5252 0%, #FF7043 100%);
}

/* 分类侧边栏 */
.category-sidebar {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 84px;
}

.category-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  font-weight: 600;
  font-size: 16px;
  color: #333;
  border-bottom: 1px solid #f0f0f0;
}

.category-list {
  padding: 8px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 4px;
}

.category-item:hover {
  background: rgba(255, 107, 107, 0.08);
}

.category-item.active {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  color: #fff;
}

.category-icon {
  font-size: 20px;
}

.category-name {
  flex: 1;
  font-size: 14px;
}

.category-count {
  font-size: 12px;
  color: #999;
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 10px;
}

.category-item.active .category-count {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

/* 商品内容区 */
.product-content {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* 搜索栏 */
.search-bar {
  margin-bottom: 24px;
}

.search-input-wrapper {
  display: flex;
  gap: 12px;
}

.search-input-wrapper .el-input {
  flex: 1;
  max-width: 500px;
}

.search-input-wrapper :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px #e4e7ed inset;
}

.search-input-wrapper :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.search-input-wrapper :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #FF6B6B inset;
}

.search-icon {
  color: #999;
}

.search-btn {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border: none;
  border-radius: 12px;
  padding: 0 28px;
}

.search-btn:hover {
  background: linear-gradient(135deg, #FF5252 0%, #FF7043 100%);
}

.search-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
}

.tag-label {
  font-size: 13px;
  color: #999;
}

.hot-tag {
  cursor: pointer;
  border-radius: 20px;
  border: none;
  background: #f5f5f5;
  color: #666;
  transition: all 0.3s ease;
}

.hot-tag:hover {
  background: rgba(255, 107, 107, 0.1);
  color: #FF6B6B;
}

/* 商品网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  min-height: 200px;
}

.product-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.product-image {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.product-image .el-image {
  width: 100%;
  height: 100%;
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
  font-size: 48px;
}

.stock-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
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
  top: 12px;
  left: 12px;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  color: #fff;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.current-price {
  color: #FF6B6B;
  font-weight: 700;
}

.current-price .currency {
  font-size: 14px;
}

.current-price .amount {
  font-size: 22px;
}

.current-price .decimal {
  font-size: 14px;
}

.original-price {
  font-size: 12px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.product-action {
  padding: 0 16px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.quantity-control {
  flex-shrink: 0;
}

.quantity-control .el-input-number {
  width: 90px;
}

.quantity-control :deep(.el-input-number__decrease),
.quantity-control :deep(.el-input-number__increase) {
  border-radius: 8px;
}

.add-cart-btn {
  flex: 1;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border: none;
  font-size: 13px;
}

.add-cart-btn:hover {
  background: linear-gradient(135deg, #FF5252 0%, #FF7043 100%);
}

.add-cart-btn:disabled {
  background: #e4e7ed;
  color: #c0c4cc;
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

/* 移动端优化 */
@media (max-width: 768px) {
  .product-list {
    padding: 0;
  }
  
  .store-header {
    border-radius: 0;
    margin-bottom: 12px;
  }
  
  .store-banner {
    padding: 16px;
  }
  
  .store-icon {
    font-size: 36px;
    width: 56px;
    height: 56px;
  }
  
  .store-details h2 {
    font-size: 18px;
  }
  
  .store-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
    font-size: 12px;
  }
  
  .store-actions {
    padding: 12px 16px;
  }
  
  .count-number {
    font-size: 22px;
  }
  
  .cart-btn {
    padding: 10px 16px;
    font-size: 13px;
  }
  
  .product-content {
    border-radius: 0;
    padding: 16px;
  }
  
  .search-input-wrapper {
    flex-direction: column;
  }
  
  .search-input-wrapper .el-input {
    max-width: 100%;
  }
  
  .search-btn {
    width: 100%;
  }
  
  .search-tags {
    flex-wrap: wrap;
  }
  
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .product-image {
    height: 140px;
  }
  
  .product-info {
    padding: 12px;
  }
  
  .product-name {
    font-size: 13px;
  }
  
  .product-desc {
    font-size: 11px;
    margin-bottom: 8px;
  }
  
  .current-price .amount {
    font-size: 18px;
  }
  
  .product-action {
    flex-direction: column;
    padding: 0 12px 12px;
    gap: 8px;
  }
  
  .quantity-control .el-input-number {
    width: 100%;
  }
  
  .add-cart-btn {
    width: 100%;
  }
}

/* 平板优化 */
@media (min-width: 769px) and (max-width: 1024px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (min-width: 1025px) and (max-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>