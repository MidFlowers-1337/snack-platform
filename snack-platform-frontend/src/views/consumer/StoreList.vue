<template>
  <div class="store-list">
    <div class="page-header">
      <h1>选择门店</h1>
      <p>选择您附近的门店开始购物</p>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <el-col :span="8" v-for="store in stores" :key="store.id">
        <el-card class="store-card" shadow="hover" @click="selectStore(store)">
          <div class="store-image">
            <el-image
              :src="store.image || 'https://via.placeholder.com/300x200?text=Store'"
              fit="cover"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Shop /></el-icon>
                </div>
              </template>
            </el-image>
            <el-tag
              class="store-status-tag"
              :type="store.status === 1 ? 'success' : 'info'"
            >
              {{ store.status === 1 ? '营业中' : '休息中' }}
            </el-tag>
          </div>
          
          <div class="store-info">
            <div class="store-name">{{ store.name }}</div>
            <div class="store-detail">
              <el-icon><Location /></el-icon>
              <span>{{ store.address }}</span>
            </div>
            <div class="store-detail">
              <el-icon><Phone /></el-icon>
              <span>{{ store.phone || '暂无电话' }}</span>
            </div>
            <div class="store-detail">
              <el-icon><Clock /></el-icon>
              <span>{{ store.businessHours || '09:00-22:00' }}</span>
            </div>
          </div>

          <div class="store-action">
            <el-button
              type="primary"
              :disabled="store.status !== 1"
              @click.stop="selectStore(store)"
            >
              {{ store.status === 1 ? '进入购物' : '暂停营业' }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && stores.length === 0" description="暂无门店" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Shop, Location, Phone, Clock } from '@element-plus/icons-vue'
import { getStores } from '@/api/store'
import { useCartStore } from '@/stores'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()

const stores = ref([])
const loading = ref(false)

const selectStore = (store) => {
  if (store.status !== 1) {
    ElMessage.warning('该门店暂停营业')
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
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.page-header p {
  color: #666;
}

.store-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.store-card:hover {
  transform: translateY(-5px);
}

.store-image {
  position: relative;
  height: 160px;
  margin: -20px -20px 15px;
  overflow: hidden;
}

.store-image .el-image {
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
  font-size: 48px;
  color: #c0c4cc;
}

.store-status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

.store-info {
  margin-bottom: 15px;
}

.store-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.store-detail {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 13px;
  margin-bottom: 6px;
}

.store-detail .el-icon {
  color: #999;
}

.store-action {
  text-align: center;
}

.store-action .el-button {
  width: 100%;
}
</style>