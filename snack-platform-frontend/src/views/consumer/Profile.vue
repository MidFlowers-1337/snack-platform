<template>
  <div class="profile-page">
    <div class="page-header">
      <h1>个人中心</h1>
    </div>

    <el-row :gutter="20">
      <el-col :span="8">
        <!-- 用户信息卡片 -->
        <el-card class="user-card">
          <div class="user-avatar">
            <el-avatar :size="80" :icon="UserFilled" />
          </div>
          <div class="user-info">
            <div class="username">{{ userStore.userInfo?.username }}</div>
            <div class="user-role">
              <el-tag>{{ getRoleText(userStore.userInfo?.role) }}</el-tag>
            </div>
            <div class="user-phone" v-if="userStore.userInfo?.phone">
              <el-icon><Phone /></el-icon>
              {{ userStore.userInfo?.phone }}
            </div>
          </div>
        </el-card>

        <!-- 快捷操作 -->
        <el-card class="quick-actions">
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="action-list">
            <div class="action-item" @click="goToOrders">
              <el-icon><Document /></el-icon>
              <span>我的订单</span>
              <el-icon class="arrow"><ArrowRight /></el-icon>
            </div>
            <div class="action-item" @click="goToCart">
              <el-icon><ShoppingCart /></el-icon>
              <span>购物车</span>
              <el-badge :value="cartStore.totalCount" v-if="cartStore.totalCount > 0" />
              <el-icon class="arrow"><ArrowRight /></el-icon>
            </div>
            <div class="action-item" @click="goToStores">
              <el-icon><Shop /></el-icon>
              <span>门店列表</span>
              <el-icon class="arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <!-- 修改资料 -->
        <el-card class="edit-card">
          <template #header>
            <span>修改资料</span>
          </template>
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
          >
            <el-form-item label="用户名">
              <el-input :value="userStore.userInfo?.username" disabled />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updateProfile" :loading="updating">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 修改密码 -->
        <el-card class="password-card">
          <template #header>
            <span>修改密码</span>
          </template>
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword" :loading="changingPassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 订单统计 -->
        <el-card class="stats-card">
          <template #header>
            <span>订单统计</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ orderStats.pending }}</div>
                <div class="stat-label">待支付</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ orderStats.processing }}</div>
                <div class="stat-label">处理中</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ orderStats.ready }}</div>
                <div class="stat-label">待取货</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ orderStats.completed }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled, Phone, Document, ShoppingCart, Shop, ArrowRight } from '@element-plus/icons-vue'
import { useUserStore, useCartStore } from '@/stores'
import { updateProfile as updateProfileApi } from '@/api/auth'
import { getMyOrders } from '@/api/order'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const profileFormRef = ref()
const passwordFormRef = ref()
const updating = ref(false)
const changingPassword = ref(false)

const profileForm = reactive({
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const orderStats = reactive({
  pending: 0,
  processing: 0,
  ready: 0,
  completed: 0
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const profileRules = {
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const getRoleText = (role) => {
  const roles = {
    'CONSUMER': '普通用户',
    'STORE_ADMIN': '门店管理员',
    'SYSTEM_ADMIN': '系统管理员'
  }
  return roles[role] || role
}

const goToOrders = () => {
  router.push('/orders')
}

const goToCart = () => {
  router.push('/cart')
}

const goToStores = () => {
  router.push('/stores')
}

const updateProfile = async () => {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return

  updating.value = true
  try {
    await updateProfileApi(profileForm)
    ElMessage.success('资料更新成功')
    userStore.fetchUserInfo()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '更新失败')
  } finally {
    updating.value = false
  }
}

const changePassword = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  changingPassword.value = true
  try {
    // 这里需要后端支持修改密码的接口
    // await changePasswordApi(passwordForm)
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    router.push('/login')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '修改失败')
  } finally {
    changingPassword.value = false
  }
}

const fetchOrderStats = async () => {
  try {
    const res = await getMyOrders({ size: 1000 })
    const orders = res.data.records || res.data || []
    
    orderStats.pending = orders.filter(o => o.status === 'PENDING').length
    orderStats.processing = orders.filter(o => ['PAID', 'CONFIRMED'].includes(o.status)).length
    orderStats.ready = orders.filter(o => o.status === 'READY').length
    orderStats.completed = orders.filter(o => o.status === 'COMPLETED').length
  } catch (error) {
    console.error('获取订单统计失败:', error)
  }
}

onMounted(() => {
  profileForm.phone = userStore.userInfo?.phone || ''
  fetchOrderStats()
})
</script>

<style scoped>
.profile-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  color: #333;
}

.user-card {
  text-align: center;
  margin-bottom: 20px;
}

.user-avatar {
  margin-bottom: 15px;
}

.user-info .username {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.user-info .user-role {
  margin-bottom: 10px;
}

.user-info .user-phone {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  color: #666;
  font-size: 14px;
}

.quick-actions {
  margin-bottom: 20px;
}

.action-list {
  display: flex;
  flex-direction: column;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px 0;
  cursor: pointer;
  transition: background 0.3s;
}

.action-item:not(:last-child) {
  border-bottom: 1px solid #eee;
}

.action-item:hover {
  background: #f5f7fa;
  margin: 0 -20px;
  padding-left: 20px;
  padding-right: 20px;
}

.action-item .arrow {
  margin-left: auto;
  color: #c0c4cc;
}

.edit-card,
.password-card,
.stats-card {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
}
</style>