<template>
  <div class="product-list">
    <!-- 门店信息 -->
    <div class="store-header" v-if="currentStore">
      <div class="store-info">
        <h2>{{ currentStore.name }}</h2>
        <span class="store-address">
          <el-icon><Location /></el-icon>
          {{ currentStore.address }}
        </span>
      </div>
      <el-button type="primary" plain @click="goToCart">
        <el-icon><ShoppingCart /></el-icon>
        购物车
        <el-badge :value="cartStore.totalCount" v-if="cartStore.totalCount > 0" />
      </el-button>
    </div>

    <el-row :gutter="20">
      <!-- 分类侧边栏 -->
      <el-col :xs="0" :sm="6" :md="5" :lg="4">
        <div class="category-sidebar">
          <div class="category-title">商品分类</div>
          <el-menu
            :default-active="currentCategory"
            @select="handleCategorySelect"
          >
            <el-menu-item index="all">
              <el-icon><Grid /></el-icon>
              <span>全部商品</span>
            </el-menu-item>
            <el-menu-item
              v-for="cat in categories"
              :key="cat.id"
              :index="String(cat.id)"
            >
              <span>{{ cat.name }}</span>
            </el-menu-item>
          </el-menu>
        </div>
      </el-col>

      <!-- 商品列表 -->
      <el-col :xs="24" :sm="18" :md="19" :lg="20">
        <div class="product-content">
          <!-- 搜索栏 -->
          <div class="search-bar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索商品"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </div>

          <!-- 商品网格 -->
          <el-row :gutter="16" v-loading="loading">
            <el-col :xs="12" :sm="12" :md="8" :lg="6" v-for="sku in skuList" :key="sku.id">
              <el-card class="product-card" shadow="hover">
                <div class="product-image">
                  <el-image
                    :src="sku.product?.image || 'https://via.placeholder.com/200?text=Product'"
                    fit="cover"
                  >
                    <template #error>
                      <div class="image-placeholder">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <el-tag
                    v-if="sku.stock <= 0"
                    class="stock-tag"
                    type="danger"
                  >
                    已售罄
                  </el-tag>
                  <el-tag
                    v-else-if="sku.stock <= 10"
                    class="stock-tag"
                    type="warning"
                  >
                    仅剩{{ sku.stock }}件
                  </el-tag>
                </div>

                <div class="product-info">
                  <div class="product-name">{{ sku.product?.name }}</div>
                  <div class="product-desc">{{ sku.product?.description }}</div>
                  <div class="product-price">
                    <span class="price">¥{{ sku.price?.toFixed(2) }}</span>
                    <span class="original-price" v-if="sku.product?.price > sku.price">
                      ¥{{ sku.product?.price?.toFixed(2) }}
                    </span>
                  </div>
                </div>

                <div class="product-action">
                  <el-input-number
                    v-model="sku.quantity"
                    :min="0"
                    :max="sku.stock"
                    :disabled="sku.stock <= 0"
                    size="small"
                    @change="(val) => handleQuantityChange(sku, val)"
                  />
                  <el-button
                    type="primary"
                    size="small"
                    :disabled="sku.stock <= 0 || !sku.quantity"
                    @click="addToCart(sku)"
                  >
                    加入购物车
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <el-empty v-if="!loading && skuList.length === 0" description="暂无商品" />

          <!-- 分页 -->
          <div class="pagination" v-if="total > 0">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[12, 24, 36, 48]"
              layout="total, sizes, prev, pager, next"
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
  sku.quantity = val || 0
}

const addToCart = (sku) => {
  if (!sku.quantity || sku.quantity <= 0) {
    ElMessage.warning('请选择数量')
    return
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
  
  ElMessage.success(`已添加 ${sku.quantity} 件 ${sku.product?.name} 到购物车`)
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

.store-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.store-info h2 {
  margin: 0 0 8px;
  font-size: 20px;
}

.store-address {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  font-size: 14px;
}

.category-sidebar {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.category-title {
  padding: 15px 20px;
  font-weight: bold;
  border-bottom: 1px solid #eee;
}

.category-sidebar .el-menu {
  border-right: none;
}

.product-content {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-bar .el-input {
  flex: 1;
  max-width: 400px;
}

.product-card {
  margin-bottom: 16px;
}

.product-image {
  position: relative;
  height: 160px;
  margin: -20px -20px 15px;
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
  background: #f5f7fa;
}

.image-placeholder .el-icon {
  font-size: 40px;
  color: #c0c4cc;
}

.stock-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

.product-info {
  margin-bottom: 12px;
}

.product-name {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.original-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
}

.product-action {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-action .el-input-number {
  width: 100px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .product-list {
    padding: 0 5px;
  }
  
  .store-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 15px;
    margin-bottom: 15px;
  }
  
  .store-info h2 {
    font-size: 18px;
  }
  
  .store-address {
    font-size: 13px;
  }
  
  .store-header .el-button {
    width: 100%;
    height: 44px;
  }
  
  /* 隐藏分类侧边栏，可以考虑后续添加抽屉式菜单 */
  .category-sidebar {
    display: none;
  }
  
  .product-content {
    padding: 15px;
  }
  
  .search-bar {
    flex-direction: column;
    gap: 8px;
    margin-bottom: 15px;
  }
  
  .search-bar .el-input {
    width: 100%;
    max-width: 100%;
  }
  
  .search-bar .el-button {
    width: 100%;
    height: 44px;
  }
  
  .product-card {
    margin-bottom: 12px;
  }
  
  .product-image {
    height: 140px;
  }
  
  .product-name {
    font-size: 13px;
  }
  
  .product-desc {
    font-size: 11px;
  }
  
  .product-price .price {
    font-size: 16px;
  }
  
  .product-action {
    flex-direction: column;
    gap: 8px;
  }
  
  .product-action .el-input-number {
    width: 100%;
  }
  
  .product-action .el-button {
    width: 100%;
    height: 40px;
  }
  
  .pagination {
    margin-top: 15px;
  }
  
  .pagination .el-pagination {
    justify-content: center;
  }
}

/* 平板优化 */
@media (min-width: 769px) and (max-width: 1024px) {
  .product-list {
    padding: 0 15px;
  }
  
  .search-bar .el-input {
    max-width: 300px;
  }
}
</style>