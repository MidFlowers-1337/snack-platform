<template>
  <div class="profile-page">
    <h1 class="page-title">个人中心</h1>

    <div class="profile-grid">
      <!-- Left column -->
      <div class="profile-sidebar">
        <!-- User card -->
        <div class="card user-card">
          <div class="user-avatar">
            {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U').charAt(0).toUpperCase() }}
          </div>
          <div class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</div>
          <span class="role-tag">{{ getRoleText(userStore.userInfo?.role) }}</span>
          <div class="user-phone" v-if="userStore.userInfo?.phone">
            <el-icon :size="14"><Phone /></el-icon>
            {{ userStore.userInfo?.phone }}
          </div>
        </div>

        <!-- Quick actions -->
        <div class="card">
          <div class="card-head">快捷操作</div>
          <div class="action-list">
            <a class="action-item" @click="goToOrders">
              <el-icon :size="18"><Document /></el-icon>
              <span>我的订单</span>
              <el-icon class="arrow" :size="14"><ArrowRight /></el-icon>
            </a>
            <a class="action-item" @click="goToCart">
              <el-icon :size="18"><ShoppingCart /></el-icon>
              <span>购物车</span>
              <el-badge :value="cartStore.totalCount" v-if="cartStore.totalCount > 0" />
              <el-icon class="arrow" :size="14"><ArrowRight /></el-icon>
            </a>
            <a class="action-item" @click="goToStores">
              <el-icon :size="18"><Shop /></el-icon>
              <span>门店列表</span>
              <el-icon class="arrow" :size="14"><ArrowRight /></el-icon>
            </a>
          </div>
        </div>
      </div>

      <!-- Right column -->
      <div class="profile-main">
        <!-- Edit profile -->
        <div class="card">
          <div class="card-head">修改资料</div>
          <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="80px" class="form-compact">
            <el-form-item label="用户名">
              <el-input :value="userStore.userInfo?.username" disabled />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updateProfile" :loading="updating">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- Change password -->
        <div class="card">
          <div class="card-head">修改密码</div>
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="80px" class="form-compact">
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword" :loading="changingPassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- Order stats -->
        <div class="card">
          <div class="card-head">订单统计</div>
          <div class="stats-grid">
            <div class="stat-block">
              <span class="stat-num">{{ orderStats.pending }}</span>
              <span class="stat-label">待支付</span>
            </div>
            <div class="stat-block">
              <span class="stat-num">{{ orderStats.processing }}</span>
              <span class="stat-label">处理中</span>
            </div>
            <div class="stat-block">
              <span class="stat-num">{{ orderStats.ready }}</span>
              <span class="stat-label">待取货</span>
            </div>
            <div class="stat-block">
              <span class="stat-num">{{ orderStats.completed }}</span>
              <span class="stat-label">已完成</span>
            </div>
          </div>
        </div>

        <!-- Points -->
        <div class="card">
          <div class="card-head">
            <span>会员积分</span>
            <span class="points-balance">{{ pointsBalance }} 分</span>
          </div>
          <div class="points-tip">每完成一笔订单可获得积分（1元 = 1积分），积分可用于兑换优惠</div>
          <el-table :data="pointsRecords" v-loading="pointsLoading" size="small" style="width: 100%">
            <el-table-column label="类型" width="80" align="center">
              <template #default="{ row }">
                <span class="type-dot" :class="row.type === 'EARN' ? 'earn' : 'spend'">
                  {{ row.type === 'EARN' ? '获得' : '消耗' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="积分" width="80" align="center">
              <template #default="{ row }">
                <span :class="['points-val', row.points > 0 ? 'plus' : 'minus']">
                  {{ row.points > 0 ? '+' : '' }}{{ row.points }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="说明" prop="description" min-width="160" />
            <el-table-column label="时间" width="160">
              <template #default="{ row }">
                {{ formatPointsTime(row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
          <div class="points-pager" v-if="pointsTotal > 5">
            <el-pagination
              v-model:current-page="pointsPage"
              :page-size="5"
              :total="pointsTotal"
              layout="prev, pager, next"
              small
              @current-change="fetchPointsRecords"
            />
          </div>
          <el-empty v-if="!pointsLoading && pointsRecords.length === 0" description="暂无积分记录" :image-size="60" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Phone, Document, ShoppingCart, Shop, ArrowRight } from '@element-plus/icons-vue'
import { useUserStore, useCartStore } from '@/stores'
import { updateProfile as updateProfileApi, changePassword as changePasswordApi } from '@/api/auth'
import { getOrderStats } from '@/api/order'
import { getPointsBalance, getPointsRecords } from '@/api/points'
import { ElMessage } from 'element-plus'
import { getRoleText } from '@/utils/constants'

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

// Points
const pointsBalance = ref(0)
const pointsRecords = ref([])
const pointsLoading = ref(false)
const pointsPage = ref(1)
const pointsTotal = ref(0)

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

const goToOrders = () => router.push('/orders')
const goToCart = () => router.push('/cart')
const goToStores = () => router.push('/stores')

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
    await changePasswordApi({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    })
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
    const res = await getOrderStats()
    const data = res.data || {}
    orderStats.pending = data.pendingPayment || 0
    orderStats.processing = data.processing || 0
    orderStats.ready = 0
    orderStats.completed = data.completed || 0
  } catch (error) {
    console.error('获取订单统计失败:', error)
  }
}

const fetchPointsBalance = async () => {
  try {
    const res = await getPointsBalance()
    pointsBalance.value = res.data?.balance || 0
  } catch (error) {
    console.error('获取积分余额失败:', error)
  }
}

const fetchPointsRecords = async () => {
  pointsLoading.value = true
  try {
    const res = await getPointsRecords({ pageNum: pointsPage.value, pageSize: 5 })
    const page = res.data || {}
    pointsRecords.value = page.records || []
    pointsTotal.value = page.total || 0
  } catch (error) {
    console.error('获取积分流水失败:', error)
  } finally {
    pointsLoading.value = false
  }
}

const formatPointsTime = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  profileForm.phone = userStore.userInfo?.phone || ''
  fetchOrderStats()
  fetchPointsBalance()
  fetchPointsRecords()
})
</script>

<style scoped>
.profile-page {
  max-width: var(--max-width);
  margin: 0 auto;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text);
  margin: 0 0 24px;
}

/* Grid layout */
.profile-grid {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 20px;
  align-items: start;
}

/* Card */
.card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 24px;
  margin-bottom: 16px;
}

.card-head {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* User card */
.user-card {
  text-align: center;
}

.user-avatar {
  width: 64px;
  height: 64px;
  background: var(--color-primary);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 24px;
  margin: 0 auto 12px;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}

.role-tag {
  display: inline-block;
  padding: 3px 12px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 12px;
}

.user-phone {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: var(--color-text-muted);
  font-size: 13px;
}

/* Actions */
.action-list {
  display: flex;
  flex-direction: column;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 0;
  cursor: pointer;
  color: var(--color-text-secondary);
  font-size: 14px;
  transition: color var(--transition-fast);
  text-decoration: none;
}

.action-item:not(:last-child) {
  border-bottom: 1px solid var(--color-border);
}

.action-item:hover {
  color: var(--color-primary);
}

.action-item .arrow {
  margin-left: auto;
  color: var(--color-text-muted);
}

.action-item span {
  flex: 1;
}

/* Form */
.form-compact :deep(.el-form-item) {
  margin-bottom: 20px;
}

/* Stats */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.stat-block {
  text-align: center;
  padding: 16px 8px;
  background: var(--color-bg);
  border-radius: var(--radius-md);
}

.stat-num {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-muted);
}

/* Points */
.points-balance {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-primary);
}

.points-tip {
  font-size: 13px;
  color: var(--color-text-muted);
  margin-bottom: 16px;
  padding: 8px 12px;
  background: var(--color-primary-light);
  border-radius: var(--radius-sm);
}

.points-pager {
  display: flex;
  justify-content: center;
  margin-top: 12px;
}

.type-dot {
  display: inline-block;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
}

.type-dot.earn {
  background: var(--color-accent-light);
  color: var(--color-accent);
}

.type-dot.spend {
  background: #FEF2F2;
  color: #DC2626;
}

.points-val {
  font-weight: 700;
  font-size: 13px;
}

.points-val.plus {
  color: var(--color-accent);
}

.points-val.minus {
  color: #DC2626;
}

/* Responsive */
@media (max-width: 768px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .profile-sidebar {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }

  .card {
    padding: 16px;
    margin-bottom: 12px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .user-avatar {
    width: 52px;
    height: 52px;
    font-size: 20px;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .profile-grid {
    grid-template-columns: 240px 1fr;
    gap: 16px;
  }
}
</style>
