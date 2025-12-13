<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <el-icon class="logo-icon"><Shop /></el-icon>
        <h1>零食平台</h1>
        <p>面向多门店协同的连锁零食电商平台</p>
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
            placeholder="用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
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
            登录
          </el-button>
        </el-form-item>

        <div class="login-footer">
          <span>还没有账号？</span>
          <el-link type="primary" @click="router.push('/register')">立即注册</el-link>
        </div>
      </el-form>

      <el-divider>测试账号</el-divider>
      <div class="test-accounts">
        <el-tag @click="fillTestAccount('admin', 'admin123')">系统管理员</el-tag>
        <el-tag type="success" @click="fillTestAccount('store1', 'store123')">门店管理员</el-tag>
        <el-tag type="warning" @click="fillTestAccount('user1', 'user123')">普通用户</el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Shop, User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

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
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = await userStore.login(form)
    ElMessage.success('登录成功')
    
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
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo-icon {
  font-size: 48px;
  color: #409eff;
}

.login-header h1 {
  margin: 10px 0 5px;
  font-size: 24px;
  color: #333;
}

.login-header p {
  color: #999;
  font-size: 14px;
}

.login-form {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
}

.login-footer {
  text-align: center;
  color: #999;
}

.test-accounts {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.test-accounts .el-tag {
  cursor: pointer;
}
</style>