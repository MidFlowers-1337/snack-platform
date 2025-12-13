<template>
  <div class="consumer-layout">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="router.push('/')">
          <el-icon><Shop /></el-icon>
          <span>零食平台</span>
        </div>
        
        <div class="nav-menu">
          <el-menu
            mode="horizontal"
            :default-active="activeMenu"
            :ellipsis="false"
            router
          >
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/stores">门店</el-menu-item>
            <el-menu-item index="/orders" v-if="userStore.isLoggedIn">订单</el-menu-item>
          </el-menu>
        </div>

        <div class="header-right">
          <!-- 购物车 -->
          <el-badge :value="cartStore.totalCount" :hidden="cartStore.isEmpty" class="cart-badge">
            <el-button :icon="ShoppingCart" circle @click="router.push('/cart')" />
          </el-badge>

          <!-- 用户信息 -->
          <template v-if="userStore.isLoggedIn">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :icon="User" />
                <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="router.push('/login')">登录</el-button>
          </template>
        </div>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <el-main class="main">
      <router-view />
    </el-main>

    <!-- 底部 -->
    <el-footer class="footer">
      <p>© 2024 零食平台 - 面向多门店协同的连锁零食电商平台</p>
    </el-footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Shop, ShoppingCart, User } from '@element-plus/icons-vue'
import { useUserStore, useCartStore } from '@/stores'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const activeMenu = computed(() => {
  return route.path
})

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'logout':
      userStore.logout()
      cartStore.clearCart()
      ElMessage.success('已退出登录')
      router.push('/')
      break
  }
}
</script>

<style scoped>
.consumer-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: 60px;
  padding: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
}

.logo .el-icon {
  font-size: 24px;
}

.nav-menu {
  flex: 1;
  margin-left: 40px;
}

.nav-menu .el-menu {
  border-bottom: none;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.cart-badge {
  margin-right: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  color: #333;
}

.main {
  flex: 1;
  margin-top: 60px;
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.footer {
  background: #fff;
  text-align: center;
  color: #999;
  font-size: 14px;
  height: 60px;
  line-height: 60px;
}
</style>