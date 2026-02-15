<template>
  <div class="login-container">
    <!-- Left panel -->
    <div class="login-side">
      <div class="side-content">
        <div class="side-brand">
          <span class="side-mark">S</span>
          <span class="side-name">SNACK</span>
        </div>
        <h2 class="side-title">连锁零食<br>门店自提平台</h2>
        <ul class="side-features">
          <li>多门店协同管理</li>
          <li>便捷在线选购</li>
          <li>新鲜门店自提</li>
        </ul>
      </div>
      <div class="side-decor">
        <div class="decor-circle decor-1"></div>
        <div class="decor-circle decor-2"></div>
      </div>
    </div>

    <!-- Right form -->
    <div class="login-box">
      <div class="login-header">
        <h1>欢迎回来</h1>
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
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>

        <div class="login-footer">
          <span>还没有账号？</span>
          <el-link type="primary" :underline="false" @click="router.push('/register')">立即注册</el-link>
        </div>
      </el-form>

      <!-- Test accounts -->
      <template v-if="showTestAccounts">
        <el-divider><span class="divider-text">快速体验</span></el-divider>
        <div class="test-accounts">
          <div class="account-card" @click="fillTestAccount('admin', 'admin123')">
            <el-icon :size="20"><Setting /></el-icon>
            <div class="account-info">
              <span class="account-name">系统管理员</span>
              <span class="account-desc">管理平台</span>
            </div>
          </div>
          <div class="account-card" @click="fillTestAccount('store1', 'admin123')">
            <el-icon :size="20"><Shop /></el-icon>
            <div class="account-info">
              <span class="account-name">门店管理员</span>
              <span class="account-desc">管理门店</span>
            </div>
          </div>
          <div class="account-card" @click="fillTestAccount('user1', 'admin123')">
            <el-icon :size="20"><User /></el-icon>
            <div class="account-info">
              <span class="account-name">普通用户</span>
              <span class="account-desc">购物体验</span>
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
import { Shop, User, Lock, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const showTestAccounts = computed(() => {
  const envValue = import.meta.env.VITE_SHOW_TEST_ACCOUNTS
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
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const fillTestAccount = (username, password) => {
  form.username = username
  form.password = password
  ElMessage.success(`已填充 ${username} 账号`)
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = await userStore.login(form)
    ElMessage.success('登录成功')
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
  background: var(--color-bg);
}

/* Left side */
.login-side {
  flex: 1;
  background: var(--color-text);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 48px;
}

.side-content {
  position: relative;
  z-index: 2;
  color: #fff;
  max-width: 380px;
}

.side-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 48px;
}

.side-mark {
  width: 36px;
  height: 36px;
  background: var(--color-primary);
  color: #fff;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 17px;
}

.side-name {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: #fff;
}

.side-title {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.3;
  margin-bottom: 32px;
  letter-spacing: -0.02em;
}

.side-features {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.side-features li {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.6);
  padding-left: 20px;
  position: relative;
}

.side-features li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary);
}

.side-decor {
  position: absolute;
  inset: 0;
}

.decor-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.04);
}

.decor-1 {
  width: 300px;
  height: 300px;
  top: -80px;
  right: -80px;
}

.decor-2 {
  width: 180px;
  height: 180px;
  bottom: -40px;
  left: -40px;
}

/* Right form */
.login-box {
  width: 480px;
  padding: 60px 50px;
  background: var(--color-surface);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-header {
  margin-bottom: 36px;
}

.login-header h1 {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 6px;
}

.login-header p {
  color: var(--color-text-muted);
  font-size: 15px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 15px;
  font-weight: 500;
  border-radius: var(--radius-md);
}

.login-footer {
  text-align: center;
  color: var(--color-text-muted);
  font-size: 14px;
}

.divider-text {
  color: var(--color-text-muted);
  font-size: 13px;
}

/* Test accounts */
.test-accounts {
  display: flex;
  gap: 10px;
}

.account-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.account-card:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

.account-card .el-icon {
  color: var(--color-text-secondary);
  flex-shrink: 0;
}

.account-name {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
}

.account-desc {
  display: block;
  font-size: 11px;
  color: var(--color-text-muted);
}

/* Responsive */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }

  .login-side {
    display: none;
  }

  .login-box {
    width: 100%;
    padding: 40px 24px;
    min-height: 100vh;
  }

  .test-accounts {
    flex-direction: column;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .login-side {
    flex: 0.8;
  }

  .login-box {
    width: 420px;
    padding: 48px 36px;
  }
}
</style>
