<template>
  <div class="store-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">选择门店</h1>
      <p class="page-desc">选择您附近的门店，开始选购零食</p>
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
    <div v-loading="loading" class="store-grid">
      <div
        v-for="store in stores"
        :key="store.id"
        class="store-card"
        :class="{ 'store-closed': store.status !== 1 }"
        @click="selectStore(store)"
      >
        <!-- 门店顶部区域 -->
        <div class="store-banner">
          <div class="store-icon">
            <el-icon><Shop /></el-icon>
          </div>
          <span
            class="store-card-status"
            :class="{ open: store.status === 1 }"
          >
            <el-icon v-if="store.status === 1"><CircleCheck /></el-icon>
            <el-icon v-else><Clock /></el-icon>
            {{ store.status === 1 ? '营业中' : '休息中' }}
          </span>
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
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && stores.length === 0" description="暂无门店">
      <template #image>
        <div class="empty-icon">
          <el-icon><Shop /></el-icon>
        </div>
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
  max-width: var(--max-width, 1200px);
  margin: 0 auto;
  padding: 0 var(--space-md);
}

/* 页面头部 */
.page-header {
  padding: var(--space-xl) 0 var(--space-lg);
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: var(--space-xs);
  line-height: 1.3;
}

.page-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.5;
}

/* 门店统计 */
.store-stats {
  display: flex;
  gap: var(--space-lg);
  margin-bottom: var(--space-lg);
}

.stat-item {
  display: flex;
  align-items: baseline;
  gap: var(--space-xs);
}

.stat-number {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-primary);
}

.stat-label {
  font-size: 14px;
  color: var(--color-text-secondary);
}

/* 门店网格 — CSS Grid */
.store-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-lg);
}

/* 门店卡片 */
.store-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  overflow: hidden;
  cursor: pointer;
  transition: border-color var(--transition-normal), box-shadow var(--transition-normal);
}

.store-card:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-md);
}

.store-card.store-closed {
  opacity: 0.65;
}

.store-card.store-closed:hover {
  border-color: var(--color-border);
  box-shadow: none;
}

/* 门店顶部区域 */
.store-banner {
  position: relative;
  height: 120px;
  background: var(--color-accent-light);
  display: flex;
  align-items: center;
  justify-content: center;
}

.store-icon {
  width: 56px;
  height: 56px;
  background: var(--color-surface);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
}

.store-icon .el-icon {
  font-size: 28px;
  color: var(--color-accent);
}

/* 营业状态标签 */
.store-card-status {
  position: absolute;
  top: var(--space-sm);
  right: var(--space-sm);
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 500;
  color: var(--color-text-muted);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  line-height: 1;
}

.store-card-status.open {
  color: var(--color-success);
  background: var(--color-accent-light);
  border-color: var(--color-accent);
}

/* 门店内容 */
.store-content {
  padding: var(--space-md);
}

.store-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--space-md);
  line-height: 1.4;
}

.store-details {
  margin-bottom: var(--space-md);
}

.detail-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-sm);
  margin-bottom: var(--space-sm);
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-icon {
  color: var(--color-text-muted);
  font-size: 15px;
  flex-shrink: 0;
  margin-top: 2px;
}

.detail-text {
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.5;
}

/* 操作按钮 */
.store-action {
  margin-top: var(--space-md);
}

.action-btn {
  width: 100%;
  height: 40px;
  font-size: 14px;
  font-weight: 500;
}

.action-btn .el-icon {
  margin-right: 4px;
}

/* 空状态 */
.empty-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  margin: 0 auto var(--space-md);
  background: var(--color-accent-light);
  border-radius: 50%;
}

.empty-icon .el-icon {
  font-size: 36px;
  color: var(--color-accent);
}

/* 移动端 — 单列 */
@media (max-width: 768px) {
  .store-list {
    padding: 0 var(--space-sm);
  }

  .page-header {
    padding: var(--space-lg) 0 var(--space-md);
  }

  .page-title {
    font-size: 20px;
  }

  .page-desc {
    font-size: 13px;
  }

  .store-stats {
    gap: var(--space-md);
    margin-bottom: var(--space-md);
  }

  .stat-number {
    font-size: 22px;
  }

  .store-grid {
    grid-template-columns: 1fr;
    gap: var(--space-md);
  }

  .store-banner {
    height: 100px;
  }

  .store-icon {
    width: 48px;
    height: 48px;
  }

  .store-icon .el-icon {
    font-size: 24px;
  }

  .store-content {
    padding: var(--space-sm) var(--space-md) var(--space-md);
  }

  .store-name {
    font-size: 15px;
    margin-bottom: var(--space-sm);
  }

  .action-btn {
    height: 42px;
    font-size: 15px;
  }
}

/* 平板 — 双列 */
@media (min-width: 769px) and (max-width: 1024px) {
  .store-list {
    padding: 0 var(--space-lg);
  }

  .store-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
