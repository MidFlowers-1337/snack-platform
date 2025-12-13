<template>
  <div class="consumer-layout">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="router.push('/')">
          <span class="logo-emoji">🍿</span>
          <span class="logo-text">零食平台</span>
        </div>
        
        <div class="nav-menu">
          <div class="nav-item" :class="{ active: activeMenu === '/' }" @click="router.push('/')">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </div>
          <div class="nav-item" :class="{ active: activeMenu === '/stores' }" @click="router.push('/stores')">
            <el-icon><Shop /></el-icon>
            <span>门店</span>
          </div>
          <div class="nav-item" :class="{ active: activeMenu === '/orders' }" @click="router.push('/orders')" v-if="userStore.isLoggedIn">
            <el-icon><List /></el-icon>
            <span>订单</span>
          </div>
        </div>

        <div class="header-right">
          <!-- 购物车 -->
          <div class="cart-btn" @click="router.push('/cart')">
            <el-badge :value="cartStore.totalCount" :hidden="cartStore.isEmpty" :max="99">
              <div class="cart-icon-wrapper">
                <el-icon><ShoppingCart /></el-icon>
              </div>
            </el-badge>
          </div>

          <!-- 用户信息 -->
          <template v-if="userStore.isLoggedIn">
            <el-dropdown @command="handleCommand" trigger="click">
              <div class="user-info">
                <el-avatar :size="36" class="user-avatar">
                  {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U').charAt(0).toUpperCase() }}
                </el-avatar>
                <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
                <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="orders">
                    <el-icon><List /></el-icon>
                    我的订单
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" round class="login-btn" @click="router.push('/login')">
              登录
            </el-button>
          </template>
        </div>
      </div>
    </el-header>

    <!-- 移动端底部导航 -->
    <div class="mobile-nav">
      <div class="mobile-nav-item" :class="{ active: activeMenu === '/' }" @click="router.push('/')">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </div>
      <div class="mobile-nav-item" :class="{ active: activeMenu === '/stores' }" @click="router.push('/stores')">
        <el-icon><Shop /></el-icon>
        <span>门店</span>
      </div>
      <div class="mobile-nav-item cart-nav" @click="router.push('/cart')">
        <el-badge :value="cartStore.totalCount" :hidden="cartStore.isEmpty" :max="99">
          <div class="cart-nav-icon">
            <el-icon><ShoppingCart /></el-icon>
          </div>
        </el-badge>
        <span>购物车</span>
      </div>
      <div class="mobile-nav-item" :class="{ active: activeMenu === '/orders' }" @click="handleMobileOrders">
        <el-icon><List /></el-icon>
        <span>订单</span>
      </div>
      <div class="mobile-nav-item" :class="{ active: activeMenu === '/profile' }" @click="handleMobileProfile">
        <el-icon><User /></el-icon>
        <span>我的</span>
      </div>
    </div>

    <!-- 主内容区 -->
    <el-main class="main">
      <router-view />
    </el-main>

    <!-- 底部 -->
    <el-footer class="footer">
      <div class="footer-content">
        <div class="footer-brand">
          <span class="footer-emoji">🍿</span>
          <span>零食平台</span>
        </div>
        <p class="footer-desc">面向多门店协同的连锁零食电商平台</p>
        <p class="footer-copyright">© 2024 Snack Platform. All rights reserved.</p>
      </div>
    </el-footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Shop, ShoppingCart, User, HomeFilled, List, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
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

const handleMobileOrders = () => {
  if (userStore.isLoggedIn) {
    router.push('/orders')
  } else {
    router.push('/login')
  }
}

const handleMobileProfile = () => {
  if (userStore.isLoggedIn) {
    router.push('/profile')
  } else {
    router.push('/login')
  }
}
</script>

<style scoped>
.consumer-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

/* 顶部导航 */
.header {
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: 64px;
  padding: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 24px;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.logo:hover {
  transform: scale(1.02);
}

.logo-emoji {
  font-size: 28px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 导航菜单 */
.nav-menu {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 48px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #666;
  font-size: 15px;
  font-weight: 500;
}

.nav-item:hover {
  background: rgba(255, 107, 107, 0.08);
  color: #FF6B6B;
}

.nav-item.active {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  color: #fff;
}

.nav-item .el-icon {
  font-size: 18px;
}

/* 右侧区域 */
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 购物车按钮 */
.cart-btn {
  cursor: pointer;
  padding: 8px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.cart-btn:hover {
  background: rgba(255, 107, 107, 0.08);
}

.cart-icon-wrapper {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.1) 0%, rgba(255, 142, 83, 0.1) 100%);
  border-radius: 12px;
  color: #FF6B6B;
  font-size: 20px;
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px 6px 6px;
  border-radius: 24px;
  transition: all 0.3s ease;
}

.user-info:hover {
  background: #f5f5f5;
}

.user-avatar {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  color: #fff;
  font-weight: 600;
}

.username {
  color: #333;
  font-size: 14px;
  font-weight: 500;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-arrow {
  font-size: 12px;
  color: #999;
  transition: transform 0.3s ease;
}

.user-info:hover .dropdown-arrow {
  transform: rotate(180deg);
}

/* 登录按钮 */
.login-btn {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border: none;
  font-weight: 500;
  padding: 10px 24px;
}

.login-btn:hover {
  background: linear-gradient(135deg, #FF5252 0%, #FF7043 100%);
}

/* 移动端底部导航 */
.mobile-nav {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #fff;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);
  z-index: 100;
  padding-bottom: env(safe-area-inset-bottom);
}

.mobile-nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #999;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.mobile-nav-item .el-icon {
  font-size: 22px;
}

.mobile-nav-item.active {
  color: #FF6B6B;
}

.mobile-nav-item.cart-nav .cart-nav-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-top: -20px;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
}

.mobile-nav-item.cart-nav .el-icon {
  font-size: 20px;
}

.mobile-nav-item.cart-nav span {
  margin-top: 2px;
}

/* 主内容区 */
.main {
  flex: 1;
  margin-top: 64px;
  padding: 24px;
  background: #f8f9fa;
  min-height: calc(100vh - 64px - 120px);
}

/* 底部 */
.footer {
  background: #fff;
  padding: 32px 24px;
  height: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.footer-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 12px;
}

.footer-emoji {
  font-size: 24px;
}

.footer-brand span:last-child {
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.footer-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.footer-copyright {
  color: #999;
  font-size: 12px;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .header {
    height: 56px;
  }
  
  .header-content {
    padding: 0 16px;
  }
  
  .logo-emoji {
    font-size: 24px;
  }
  
  .logo-text {
    font-size: 18px;
  }
  
  /* 隐藏桌面导航菜单 */
  .nav-menu {
    display: none;
  }
  
  /* 显示移动端底部导航 */
  .mobile-nav {
    display: flex;
  }
  
  .header-right {
    gap: 8px;
  }
  
  .cart-btn {
    padding: 4px;
  }
  
  .cart-icon-wrapper {
    width: 36px;
    height: 36px;
    font-size: 18px;
  }
  
  /* 隐藏用户名和下拉箭头 */
  .username,
  .dropdown-arrow {
    display: none;
  }
  
  .user-info {
    padding: 4px;
  }
  
  .user-avatar {
    width: 32px !important;
    height: 32px !important;
    font-size: 14px !important;
  }
  
  .login-btn {
    padding: 8px 16px;
    font-size: 13px;
  }
  
  .main {
    margin-top: 56px;
    margin-bottom: 60px;
    padding: 16px 12px;
    min-height: calc(100vh - 56px - 60px);
  }
  
  .footer {
    display: none;
  }
}

/* 平板优化 */
@media (min-width: 769px) and (max-width: 1024px) {
  .header-content {
    padding: 0 20px;
  }
  
  .nav-menu {
    margin-left: 32px;
  }
  
  .nav-item {
    padding: 8px 16px;
    font-size: 14px;
  }
  
  .main {
    padding: 20px;
  }
}

/* 下拉菜单样式 */
:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
}

:deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
}
</style>