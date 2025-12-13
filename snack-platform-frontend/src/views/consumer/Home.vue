<template>
  <div class="home">
    <!-- 欢迎横幅 -->
    <div class="banner">
      <div class="banner-decoration"></div>
      <div class="banner-content">
        <div class="banner-emoji">🍿</div>
        <h1>欢迎来到零食平台</h1>
        <p>精选零食，门店自提，新鲜便捷</p>
        <el-button type="primary" size="large" round @click="router.push('/stores')">
          <el-icon><Shop /></el-icon>
          选择门店开始购物
        </el-button>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-entry">
      <el-row :gutter="16">
        <el-col :xs="12" :sm="6">
          <div class="entry-card entry-stores" @click="router.push('/stores')">
            <div class="entry-icon-wrapper">
              <el-icon class="entry-icon"><Shop /></el-icon>
            </div>
            <span>附近门店</span>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="entry-card entry-products" @click="goToCurrentStore">
            <div class="entry-icon-wrapper">
              <el-icon class="entry-icon"><Goods /></el-icon>
            </div>
            <span>商品浏览</span>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="entry-card entry-cart" @click="router.push('/cart')">
            <div class="entry-icon-wrapper">
              <el-icon class="entry-icon"><ShoppingCart /></el-icon>
            </div>
            <span>购物车</span>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="entry-card entry-orders" @click="router.push('/orders')">
            <div class="entry-icon-wrapper">
              <el-icon class="entry-icon"><List /></el-icon>
            </div>
            <span>我的订单</span>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 当前门店 -->
    <div class="current-store" v-if="currentStore">
      <div class="section-header">
        <h2>📍 当前门店</h2>
        <el-button type="primary" link @click="router.push('/stores')">切换门店 →</el-button>
      </div>
      <div class="current-store-card" @click="goToCurrentStore">
        <div class="store-banner-mini" :style="{ background: 'linear-gradient(135deg, #4ECDC4 0%, #44A08D 100%)' }">
          <el-icon class="store-icon-mini"><Shop /></el-icon>
        </div>
        <div class="store-info">
          <div class="store-name">{{ currentStore.name }}</div>
          <div class="store-meta">
            <span class="meta-item">
              <el-icon><Location /></el-icon>
              {{ currentStore.address }}
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ currentStore.businessHours || '09:00-22:00' }}
            </span>
          </div>
        </div>
        <el-button type="primary" round>进入购物</el-button>
      </div>
    </div>

    <!-- 推荐门店 -->
    <div class="recommend-stores">
      <div class="section-header">
        <h2>🏪 推荐门店</h2>
        <el-button type="primary" link @click="router.push('/stores')">查看全部 →</el-button>
      </div>
      <el-row :gutter="16" v-loading="loading">
        <el-col :xs="24" :sm="8" v-for="store in stores" :key="store.id">
          <div class="store-item" @click="selectStore(store)">
            <div class="store-item-banner" :style="{ background: getStoreGradient(store.id) }">
              <el-icon><Shop /></el-icon>
            </div>
            <div class="store-item-content">
              <div class="store-name">{{ store.name }}</div>
              <div class="store-address">
                <el-icon><Location /></el-icon>
                {{ store.address }}
              </div>
              <div class="store-status">
                <el-tag :type="store.status === 1 ? 'success' : 'info'" size="small" effect="plain">
                  {{ store.status === 1 ? '营业中' : '休息中' }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Shop, Goods, ShoppingCart, List, Location, Clock } from '@element-plus/icons-vue'
import { getStores, getStoreDetail } from '@/api/store'
import { useCartStore } from '@/stores'

const router = useRouter()
const cartStore = useCartStore()

const stores = ref([])
const currentStore = ref(null)
const loading = ref(false)

// 根据门店ID生成不同的渐变色
const getStoreGradient = (id) => {
  const gradients = [
    'linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%)',
    'linear-gradient(135deg, #4ECDC4 0%, #44A08D 100%)',
    'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  ]
  return gradients[id % gradients.length]
}

const goToCurrentStore = () => {
  if (cartStore.currentStoreId) {
    router.push(`/stores/${cartStore.currentStoreId}/products`)
  } else {
    router.push('/stores')
  }
}

const selectStore = (store) => {
  cartStore.setCurrentStore(store.id)
  router.push(`/stores/${store.id}/products`)
}

const fetchStores = async () => {
  loading.value = true
  try {
    const res = await getStores()
    stores.value = res.data.slice(0, 3) // 只显示前3个
  } catch (error) {
    console.error('获取门店列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchCurrentStore = async () => {
  if (cartStore.currentStoreId) {
    try {
      const res = await getStoreDetail(cartStore.currentStoreId)
      currentStore.value = res.data
    } catch (error) {
      console.error('获取当前门店失败:', error)
    }
  }
}

onMounted(() => {
  fetchStores()
  fetchCurrentStore()
})
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

/* 横幅样式 */
.banner {
  position: relative;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border-radius: 20px;
  padding: 50px 40px;
  color: #fff;
  margin-bottom: 30px;
  overflow: hidden;
}

.banner-decoration {
  position: absolute;
  top: -80px;
  right: -80px;
  width: 250px;
  height: 250px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.banner-decoration::after {
  content: '';
  position: absolute;
  top: 60px;
  left: 60px;
  width: 180px;
  height: 180px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.banner-content {
  position: relative;
  z-index: 2;
}

.banner-emoji {
  font-size: 48px;
  margin-bottom: 16px;
}

.banner-content h1 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 12px;
}

.banner-content p {
  font-size: 16px;
  opacity: 0.95;
  margin-bottom: 24px;
}

.banner-content .el-button {
  height: 48px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 500;
}

/* 快捷入口 */
.quick-entry {
  margin-bottom: 32px;
}

.entry-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;
}

.entry-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
}

.entry-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
}

.entry-stores .entry-icon-wrapper {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
}

.entry-products .entry-icon-wrapper {
  background: linear-gradient(135deg, #4ECDC4 0%, #44A08D 100%);
}

.entry-cart .entry-icon-wrapper {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.entry-orders .entry-icon-wrapper {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.entry-icon {
  font-size: 28px;
  color: #fff;
}

.entry-card span {
  display: block;
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

/* 区块标题 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

/* 当前门店 */
.current-store {
  margin-bottom: 32px;
}

.current-store-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.current-store-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
}

.store-banner-mini {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.store-icon-mini {
  font-size: 36px;
  color: #fff;
}

.store-info {
  flex: 1;
  min-width: 0;
}

.store-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.store-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  font-size: 14px;
}

.meta-item .el-icon {
  color: #FF6B6B;
}

/* 推荐门店 */
.recommend-stores {
  margin-bottom: 32px;
}

.store-item {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;
}

.store-item:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
}

.store-item-banner {
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.store-item-banner .el-icon {
  font-size: 40px;
  color: rgba(255, 255, 255, 0.8);
}

.store-item-content {
  padding: 16px;
}

.store-item .store-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}

.store-item .store-address {
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
  display: flex;
  align-items: flex-start;
  gap: 4px;
}

.store-item .store-address .el-icon {
  color: #FF6B6B;
  flex-shrink: 0;
  margin-top: 2px;
}

.store-status {
  text-align: left;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .home {
    padding: 0 12px;
  }
  
  .banner {
    padding: 32px 24px;
    border-radius: 16px;
  }
  
  .banner-emoji {
    font-size: 36px;
  }
  
  .banner-content h1 {
    font-size: 24px;
  }
  
  .banner-content p {
    font-size: 14px;
  }
  
  .entry-card {
    padding: 20px 12px;
  }
  
  .entry-icon-wrapper {
    width: 48px;
    height: 48px;
    border-radius: 12px;
  }
  
  .entry-icon {
    font-size: 24px;
  }
  
  .entry-card span {
    font-size: 13px;
  }
  
  .current-store-card {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .store-meta {
    justify-content: center;
    flex-direction: column;
    gap: 8px;
  }
  
  .meta-item {
    justify-content: center;
  }
}
</style>