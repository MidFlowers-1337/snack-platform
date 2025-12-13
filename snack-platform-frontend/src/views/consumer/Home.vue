<template>
  <div class="home">
    <!-- 欢迎横幅 -->
    <div class="banner">
      <div class="banner-content">
        <h1>欢迎来到零食平台</h1>
        <p>精选零食，门店自提，新鲜便捷</p>
        <el-button type="primary" size="large" @click="router.push('/stores')">
          选择门店开始购物
        </el-button>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-entry">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="entry-card" @click="router.push('/stores')">
            <el-icon class="entry-icon"><Shop /></el-icon>
            <span>附近门店</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="entry-card" @click="goToCurrentStore">
            <el-icon class="entry-icon"><Goods /></el-icon>
            <span>商品浏览</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="entry-card" @click="router.push('/cart')">
            <el-icon class="entry-icon"><ShoppingCart /></el-icon>
            <span>购物车</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="entry-card" @click="router.push('/orders')">
            <el-icon class="entry-icon"><List /></el-icon>
            <span>我的订单</span>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 当前门店 -->
    <div class="current-store" v-if="currentStore">
      <div class="section-header">
        <h2>当前门店</h2>
        <el-button type="primary" link @click="router.push('/stores')">切换门店</el-button>
      </div>
      <el-card class="store-card" @click="goToCurrentStore">
        <div class="store-info">
          <div class="store-name">{{ currentStore.name }}</div>
          <div class="store-address">
            <el-icon><Location /></el-icon>
            {{ currentStore.address }}
          </div>
          <div class="store-hours">
            <el-icon><Clock /></el-icon>
            营业时间：{{ currentStore.businessHours }}
          </div>
        </div>
        <el-button type="primary">进入购物</el-button>
      </el-card>
    </div>

    <!-- 推荐门店 -->
    <div class="recommend-stores">
      <div class="section-header">
        <h2>推荐门店</h2>
        <el-button type="primary" link @click="router.push('/stores')">查看全部</el-button>
      </div>
      <el-row :gutter="20" v-loading="loading">
        <el-col :span="8" v-for="store in stores" :key="store.id">
          <el-card class="store-item" shadow="hover" @click="selectStore(store)">
            <div class="store-name">{{ store.name }}</div>
            <div class="store-address">
              <el-icon><Location /></el-icon>
              {{ store.address }}
            </div>
            <div class="store-status">
              <el-tag :type="store.status === 1 ? 'success' : 'info'" size="small">
                {{ store.status === 1 ? '营业中' : '休息中' }}
              </el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Shop, Goods, ShoppingCart, List, Location, Clock } from '@element-plus/icons-vue'
import { getStores, getStoreDetail } from '@/api/store'
import { useCartStore } from '@/stores'

const router = useRouter()
const cartStore = useCartStore()

const stores = ref([])
const currentStore = ref(null)
const loading = ref(false)

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
}

.banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 60px 40px;
  color: #fff;
  margin-bottom: 30px;
}

.banner-content h1 {
  font-size: 32px;
  margin-bottom: 10px;
}

.banner-content p {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 20px;
}

.quick-entry {
  margin-bottom: 30px;
}

.entry-card {
  background: #fff;
  border-radius: 8px;
  padding: 30px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.entry-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.entry-icon {
  font-size: 36px;
  color: #409eff;
  margin-bottom: 10px;
}

.entry-card span {
  display: block;
  color: #333;
  font-size: 14px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 20px;
  color: #333;
}

.current-store {
  margin-bottom: 30px;
}

.store-card {
  cursor: pointer;
}

.store-card :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.store-info {
  flex: 1;
}

.store-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.store-address,
.store-hours {
  color: #666;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
}

.recommend-stores {
  margin-bottom: 30px;
}

.store-item {
  cursor: pointer;
  margin-bottom: 20px;
}

.store-item .store-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
}

.store-item .store-address {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.store-status {
  text-align: right;
}
</style>