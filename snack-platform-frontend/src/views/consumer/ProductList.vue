<template>
  <div class="product-list">
    <!-- 门店信息头部 -->
    <div class="store-header" v-if="currentStore">
      <div class="store-banner">
        <div class="store-banner-content">
          <div class="store-icon">
            <el-icon :size="32" color="#fff"><Shop /></el-icon>
          </div>
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
              <div class="product-image" @click="goToDetail(sku)">
                <el-image
                  :src="sku.product?.image || 'https://via.placeholder.com/200?text=Product'"
                  fit="cover"
                >
                  <template #error>
                    <div class="image-placeholder">
                      <el-icon :size="40" color="var(--color-text-muted)"><Goods /></el-icon>
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

              <div class="product-info" @click="goToDetail(sku)">
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
              <el-icon :size="80" color="var(--color-text-muted)"><Search /></el-icon>
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
import { Location, ShoppingCart, Grid, Search, Goods, Shop } from '@element-plus/icons-vue'
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

const goToCart = () => {
  router.push('/cart')
}

const goToDetail = (sku) => {
  router.push(`/stores/${storeId.value}/products/${sku.id}`)
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
    message: `已添加 ${sku.quantity} 件 ${sku.product?.name} 到购物车`,
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
  padding: 0 var(--space-sm);
}

/* 门店头部 */
.store-header {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  margin-bottom: var(--space-lg);
  overflow: hidden;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.store-banner {
  padding: var(--space-lg);
  color: #fff;
  background: var(--color-text);
}

.store-banner-content {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.store-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.12);
  flex-shrink: 0;
}

.store-details h2 {
  margin: 0 0 var(--space-sm);
  font-size: 22px;
  font-weight: 600;
}

.store-meta {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  font-size: 14px;
  opacity: 0.85;
}

.store-address {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
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
  background: rgba(255, 255, 255, 0.4);
}

.store-status.open .status-dot {
  background: var(--color-success);
  box-shadow: 0 0 6px rgba(45, 106, 79, 0.5);
}

.store-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-md) var(--space-lg);
  background: var(--color-surface);
}

.product-count {
  display: flex;
  align-items: baseline;
  gap: var(--space-xs);
}

.count-number {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-primary);
}

.count-label {
  font-size: 14px;
  color: var(--color-text-muted);
}

.cart-btn {
  background: var(--color-primary);
  border: none;
  padding: 10px var(--space-lg);
  font-size: 14px;
}

.cart-btn:hover {
  background: var(--color-primary-hover);
}

/* 分类侧边栏 */
.category-sidebar {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 84px;
}

.category-header {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-md) 20px;
  font-weight: 600;
  font-size: 15px;
  color: var(--color-text);
  border-bottom: 1px solid var(--color-border);
}

.category-list {
  padding: var(--space-sm);
}

.category-item {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: 10px var(--space-md);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  margin-bottom: 2px;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.category-item:hover {
  background: var(--color-hover);
  color: var(--color-text);
}

.category-item.active {
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-weight: 500;
}

.category-name {
  flex: 1;
}

.category-count {
  font-size: 12px;
  color: var(--color-text-muted);
  background: var(--color-hover);
  padding: 2px 8px;
  border-radius: var(--radius-full);
}

.category-item.active .category-count {
  background: rgba(217, 79, 4, 0.12);
  color: var(--color-primary);
}

/* 商品内容区 */
.product-content {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

/* 搜索栏 */
.search-bar {
  margin-bottom: var(--space-lg);
}

.search-input-wrapper {
  display: flex;
  gap: var(--space-sm);
}

.search-input-wrapper .el-input {
  flex: 1;
  max-width: 500px;
}

.search-input-wrapper :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  box-shadow: 0 0 0 1px var(--color-border) inset;
}

.search-input-wrapper :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.search-input-wrapper :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

.search-icon {
  color: var(--color-text-muted);
}

.search-btn {
  background: var(--color-primary);
  border: none;
  border-radius: var(--radius-md);
  padding: 0 var(--space-lg);
}

.search-btn:hover {
  background: var(--color-primary-hover);
}

.search-tags {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-top: var(--space-sm);
}

.tag-label {
  font-size: 13px;
  color: var(--color-text-muted);
}

.hot-tag {
  cursor: pointer;
  border-radius: var(--radius-full);
  border: none;
  background: var(--color-hover);
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
}

.hot-tag:hover {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

/* 商品网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-md);
  min-height: 200px;
}

.product-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--color-border);
  transition: border-color var(--transition-normal), box-shadow var(--transition-normal);
  cursor: pointer;
}

.product-card:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-md);
}

.product-image {
  position: relative;
  height: 180px;
  overflow: hidden;
  cursor: pointer;
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
  background: var(--color-bg);
}

.stock-badge {
  position: absolute;
  top: var(--space-sm);
  right: var(--space-sm);
  padding: var(--space-xs) 10px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 500;
}

.stock-badge.sold-out {
  background: var(--color-danger);
  color: #fff;
}

.stock-badge.low-stock {
  background: var(--color-warning);
  color: #fff;
}

.discount-badge {
  position: absolute;
  top: var(--space-sm);
  left: var(--space-sm);
  background: var(--color-primary);
  color: #fff;
  padding: var(--space-xs) 10px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 500;
}

.product-info {
  padding: var(--space-md);
  cursor: pointer;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--space-xs);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: var(--space-sm);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: var(--space-sm);
}

.current-price {
  color: var(--color-primary);
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
  color: var(--color-text-muted);
  text-decoration: line-through;
}

.product-action {
  padding: 0 var(--space-md) var(--space-md);
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.quantity-control {
  flex-shrink: 0;
}

.quantity-control .el-input-number {
  width: 90px;
}

.quantity-control :deep(.el-input-number__decrease),
.quantity-control :deep(.el-input-number__increase) {
  border-radius: var(--radius-sm);
}

.add-cart-btn {
  flex: 1;
  background: var(--color-primary);
  border: none;
  font-size: 13px;
}

.add-cart-btn:hover {
  background: var(--color-primary-hover);
}

.add-cart-btn:disabled {
  background: var(--color-border);
  color: var(--color-text-muted);
}

/* 分页 */
.pagination {
  margin-top: var(--space-lg);
  display: flex;
  justify-content: center;
}

.pagination :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: var(--color-primary);
}

/* 移动端优化 */
@media (max-width: 768px) {
  .product-list {
    padding: 0;
  }

  .store-header {
    border-radius: 0;
    margin-bottom: var(--space-sm);
  }

  .store-banner {
    padding: var(--space-md);
  }

  .store-icon {
    width: 48px;
    height: 48px;
  }

  .store-details h2 {
    font-size: 18px;
  }

  .store-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-xs);
    font-size: 12px;
  }

  .store-actions {
    padding: var(--space-sm) var(--space-md);
  }

  .count-number {
    font-size: 22px;
  }

  .cart-btn {
    padding: 8px var(--space-md);
    font-size: 13px;
  }

  .product-content {
    border-radius: 0;
    padding: var(--space-md);
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
    gap: var(--space-sm);
  }

  .product-image {
    height: 140px;
  }

  .product-info {
    padding: var(--space-sm);
  }

  .product-name {
    font-size: 13px;
  }

  .product-desc {
    font-size: 11px;
    margin-bottom: var(--space-sm);
  }

  .current-price .amount {
    font-size: 18px;
  }

  .product-action {
    flex-direction: column;
    padding: 0 var(--space-sm) var(--space-sm);
    gap: var(--space-sm);
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
