<template>
  <div class="store-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>🏪 选择门店</h1>
        <p>选择您附近的门店，开启美味零食之旅</p>
      </div>
      <div class="header-decoration"></div>
    </div>

    <!-- 门店统计 -->
    <div class="store-stats" v-if="stores.length > 0">
      <div class="stat-item">
        <span class="stat-number">{{ stores.length }}</span>
        <span class="stat-label">家门店</span>
      </div>
      <div class="stat-item">
        <span class="stat-number">{{ openStoreCount }}</span>
        <span class="stat-label">营业中</span>
      </div>
    </div>

    <!-- 门店列表 -->
    <el-row :gutter="24" v-loading="loading" class="store-grid">
      <el-col :xs="24" :sm="12" :md="8" :lg="8" v-for="store in stores" :key="store.id">
        <div class="store-card" :class="{ 'store-closed': store.status !== 1 }" @click="selectStore(store)">
          <!-- 门店图片/装饰区域 -->
          <div class="store-banner" :style="{ background: getStoreGradient(store.id) }">
            <div class="banner-pattern"></div>
            <div class="store-icon">
              <el-icon><Shop /></el-icon>
            </div>
            <el-tag
              class="store-status-tag"
              :type="store.status === 1 ? 'success' : 'info'"
              effect="dark"
            >
              <el-icon v-if="store.status === 1"><CircleCheck /></el-icon>
              <el-icon v-else><Clock /></el-icon>
              {{ store.status === 1 ? '营业中' : '休息中' }}
            </el-tag>
          </div>
          
          <!-- 门店信息 -->
          <div class="store-content">
            <div class="store-name">{{ store.name }}</div>
            
            <div class="store-details">
              <div class="detail-item">
                <el-icon class="detail-icon"><Location /></el-icon>
                <span class="detail-text">{{ store.address }}</span>
              </div>
              <div class="detail-item">
                <el-icon class="detail-icon"><Phone /></el-icon>
                <span class="detail-text">{{ store.phone || '暂无电话' }}</span>
              </div>
              <div class="detail-item">
                <el-icon class="detail-icon"><Clock /></el-icon>
                <span class="detail-text">{{ store.businessHours || '09:00-22:00' }}</span>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="store-action">
              <el-button
                :type="store.status === 1 ? 'primary' : 'info'"
                :disabled="store.status !== 1"
                round
                class="action-btn"
                @click.stop="selectStore(store)"
              >
                <el-icon v-if="store.status === 1"><ShoppingCart /></el-icon>
                {{ store.status === 1 ? '进入购物' : '暂停营业' }}
              </el-button>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <el-empty v-if="!loading && stores.length === 0" description="暂无门店">
      <template #image>
        <div class="empty-icon">🏪</div>
      </template>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Shop, Location, Phone, Clock, ShoppingCart, CircleCheck } from '@element-plus/icons-vue'
import { getStores } from '@/api/store'
import { useCartStore } from '@/stores'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()

const stores = ref([])
const loading = ref(false)

// 计算营业中的门店数量
const openStoreCount = computed(() => {
  return stores.value.filter(s => s.status === 1).length
})

// 根据门店ID生成不同的渐变色
const getStoreGradient = (id) => {
  const gradients = [
    'linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%)',
    'linear-gradient(135deg, #4ECDC4 0%, #44A08D 100%)',
    'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  ]
  return gradients[id % gradients.length]
}

const selectStore = (store) => {
  if (store.status !== 1) {
    ElMessage.warning('该门店暂停营业，请选择其他门店')
    return
  }
  cartStore.setCurrentStore(store.id)
  router.push(`/stores/${store.id}/products`)
}

const fetchStores = async () => {
  loading.value = true
  try {
    const res = await getStores()
    stores.value = res.data
  } catch (error) {
    console.error('获取门店列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStores()
})
</script>

<style scoped>
.store-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

/* 页面头部 */
.page-header {
  position: relative;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border-radius: 20px;
  padding: 40px 30px;
  margin-bottom: 30px;
  overflow: hidden;
}

.header-content {
  position: relative;
  z-index: 2;
}

.page-header h1 {
  font-size: 28px;
  color: #fff;
  margin-bottom: 10px;
  font-weight: 700;
}

.page-header p {
  color: rgba(255, 255, 255, 0.9);
  font-size: 16px;
}

.header-decoration {
  position: absolute;
  top: -50px;
  right: -50px;
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.header-decoration::after {
  content: '';
  position: absolute;
  top: 50px;
  left: 50px;
  width: 150px;
  height: 150px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

/* 门店统计 */
.store-stats {
  display: flex;
  gap: 30px;
  margin-bottom: 24px;
  padding: 0 8px;
}

.stat-item {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #FF6B6B;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 门店网格 */
.store-grid {
  margin: 0 -12px;
}

/* 门店卡片 */
.store-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.store-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.store-card.store-closed {
  opacity: 0.7;
}

.store-card.store-closed:hover {
  transform: none;
}

/* 门店横幅 */
.store-banner {
  position: relative;
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.banner-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    radial-gradient(circle at 20% 80%, rgba(255,255,255,0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.15) 0%, transparent 50%);
}

.store-icon {
  width: 70px;
  height: 70px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.store-icon .el-icon {
  font-size: 36px;
  color: #fff;
}

.store-status-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
}

/* 门店内容 */
.store-content {
  padding: 20px;
}

.store-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  line-height: 1.4;
}

.store-details {
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 10px;
}

.detail-icon {
  color: #FF6B6B;
  font-size: 16px;
  flex-shrink: 0;
  margin-top: 2px;
}

.detail-text {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

/* 操作按钮 */
.store-action {
  text-align: center;
}

.action-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 500;
}

.action-btn .el-icon {
  margin-right: 6px;
}

/* 空状态 */
.empty-icon {
  font-size: 80px;
  margin-bottom: 16px;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .store-list {
    padding: 0 12px;
  }
  
  .page-header {
    padding: 30px 20px;
    border-radius: 16px;
    margin-bottom: 20px;
  }
  
  .page-header h1 {
    font-size: 22px;
  }
  
  .page-header p {
    font-size: 14px;
  }
  
  .store-stats {
    gap: 20px;
    margin-bottom: 16px;
  }
  
  .stat-number {
    font-size: 24px;
  }
  
  .store-card {
    margin-bottom: 16px;
    border-radius: 12px;
  }
  
  .store-banner {
    height: 120px;
  }
  
  .store-icon {
    width: 60px;
    height: 60px;
  }
  
  .store-icon .el-icon {
    font-size: 30px;
  }
  
  .store-content {
    padding: 16px;
  }
  
  .store-name {
    font-size: 16px;
    margin-bottom: 12px;
  }
  
  .detail-item {
    margin-bottom: 8px;
  }
  
  .detail-text {
    font-size: 13px;
  }
  
  .action-btn {
    height: 46px;
    font-size: 16px;
  }
}

/* 平板优化 */
@media (min-width: 769px) and (max-width: 1024px) {
  .store-list {
    padding: 0 20px;
  }
  
  .page-header {
    padding: 35px 25px;
  }
}
</style>