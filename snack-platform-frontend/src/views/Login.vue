<template>
  <div class="login-container">
    <!-- 左侧装饰区域 -->
    <div class="login-decoration">
      <div class="decoration-content">
        <div class="decoration-emoji">🍿</div>
        <h2>零食平台</h2>
        <p>面向多门店协同的连锁零食电商平台</p>
        <div class="features">
          <div class="feature-item">
            <el-icon><Shop /></el-icon>
            <span>多门店协同</span>
          </div>
          <div class="feature-item">
            <el-icon><ShoppingCart /></el-icon>
            <span>便捷购物</span>
          </div>
          <div class="feature-item">
            <el-icon><Location /></el-icon>
            <span>门店自提</span>
          </div>
        </div>
      </div>
      <div class="decoration-circles">
        <div class="circle circle-1"></div>
        <div class="circle circle-2"></div>
        <div class="circle circle-3"></div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-box">
      <div class="login-header">
        <h1>欢迎回来 👋</h1>
        <p>请登录您的账号</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
            class="custom-input"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            class="custom-input"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            round
            @click="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>

        <div class="login-footer">
          <span>还没有账号？</span>
          <el-link type="primary" :underline="false" @click="router.push('/register')">立即注册</el-link>
        </div>
      </el-form>

      <!-- 测试账号区域 - 仅在开发环境显示 -->
      <template v-if="showTestAccounts">
        <el-divider>
          <span class="divider-text">快速体验</span>
        </el-divider>
        
        <div class="test-accounts">
          <div class="account-card admin" @click="fillTestAccount('admin', 'admin123')">
            <div class="account-icon">👨‍💼</div>
            <div class="account-info">
              <div class="account-name">系统管理员</div>
              <div class="account-desc">管理平台</div>
            </div>
          </div>
          <div class="account-card store" @click="fillTestAccount('store1', 'admin123')">
            <div class="account-icon">🏪</div>
            <div class="account-info">
              <div class="account-name">门店管理员</div>
              <div class="account-desc">管理门店</div>
            </div>
          </div>
          <div class="account-card user" @click="fillTestAccount('user1', 'admin123')">
            <div class="account-icon">🛒</div>
            <div class="account-info">
              <div class="account-name">普通用户</div>
              <div class="account-desc">购物体验</div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Shop, User, Lock, ShoppingCart, Location } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

// 是否显示测试账号 - 通过环境变量控制
// 生产环境设置 VITE_SHOW_TEST_ACCOUNTS=false 或不设置
const showTestAccounts = computed(() => {
  const envValue = import.meta.env.VITE_SHOW_TEST_ACCOUNTS
  // 开发模式默认显示，生产模式默认不显示
  if (envValue === undefined || envValue === '') {
    return import.meta.env.DEV
  }
  return envValue === 'true' || envValue === true
})

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const fillTestAccount = (username, password) => {
  form.username = username
  form.password = password
  ElMessage.success(`已填充 ${username} 账号，点击登录即可`)
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = await userStore.login(form)
    ElMessage.success('登录成功，欢迎回来！')
    
    // 根据角色跳转
    const redirect = route.query.redirect
    if (redirect) {
      router.push(redirect)
    } else if (data.user.role === 'SYSTEM_ADMIN') {
      router.push('/admin')
    } else if (data.user.role === 'STORE_ADMIN') {
      router.push('/store')
    } else {
      router.push('/')
    }
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  background: #f8f9fa;
}

/* 左侧装饰区域 */
.login-decoration {
  flex: 1;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 40px;
}

.decoration-content {
  position: relative;
  z-index: 2;
  color: #fff;
  text-align: center;
  max-width: 400px;
}

.decoration-emoji {
  font-size: 80px;
  margin-bottom: 24px;
}

.decoration-content h2 {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
}

.decoration-content p {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 40px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.15);
  padding: 16px 24px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.feature-item .el-icon {
  font-size: 24px;
}

.feature-item span {
  font-size: 16px;
  font-weight: 500;
}

.decoration-circles {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -100px;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  left: -50px;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  left: 10%;
}

/* 右侧登录表单 */
.login-box {
  width: 480px;
  padding: 60px 50px;
  background: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-header {
  margin-bottom: 40px;
}

.login-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
}

.login-header p {
  color: #999;
  font-size: 15px;
}

.login-form {
  margin-bottom: 20px;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 4px 16px;
  box-shadow: 0 0 0 1px #e4e7ed inset;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #FF6B6B inset;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border: none;
}

.login-btn:hover {
  background: linear-gradient(135deg, #FF5252 0%, #FF7043 100%);
}

.login-footer {
  text-align: center;
  color: #999;
  font-size: 14px;
}

.login-footer .el-link {
  font-size: 14px;
  font-weight: 500;
}

.divider-text {
  color: #999;
  font-size: 13px;
}

/* 测试账号卡片 */
.test-accounts {
  display: flex;
  gap: 12px;
}

.account-card {
  flex: 1;
  padding: 16px 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.account-card:hover {
  transform: translateY(-4px);
}

.account-card.admin {
  background: linear-gradient(135deg, rgba(103, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border: 1px solid rgba(103, 126, 234, 0.2);
}

.account-card.admin:hover {
  background: linear-gradient(135deg, rgba(103, 126, 234, 0.2) 0%, rgba(118, 75, 162, 0.2) 100%);
}

.account-card.store {
  background: linear-gradient(135deg, rgba(78, 205, 196, 0.1) 0%, rgba(68, 160, 141, 0.1) 100%);
  border: 1px solid rgba(78, 205, 196, 0.2);
}

.account-card.store:hover {
  background: linear-gradient(135deg, rgba(78, 205, 196, 0.2) 0%, rgba(68, 160, 141, 0.2) 100%);
}

.account-card.user {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.1) 0%, rgba(255, 142, 83, 0.1) 100%);
  border: 1px solid rgba(255, 107, 107, 0.2);
}

.account-card.user:hover {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.2) 0%, rgba(255, 142, 83, 0.2) 100%);
}

.account-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.account-name {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
}

.account-desc {
  font-size: 11px;
  color: #999;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }
  
  .login-decoration {
    display: none;
  }
  
  .login-box {
    width: 100%;
    padding: 40px 24px;
    min-height: 100vh;
  }
  
  .login-header h1 {
    font-size: 24px;
  }
  
  .test-accounts {
    flex-direction: column;
  }
  
  .account-card {
    display: flex;
    align-items: center;
    gap: 12px;
    text-align: left;
    padding: 12px 16px;
  }
  
  .account-icon {
    font-size: 28px;
    margin-bottom: 0;
  }
}

/* 平板适配 */
@media (min-width: 769px) and (max-width: 1024px) {
  .login-decoration {
    flex: 0.8;
  }
  
  .login-box {
    width: 420px;
    padding: 50px 40px;
  }
}
</style>