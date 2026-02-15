<template>
  <div class="consumer-layout">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-inner">
        <div class="header-left">
          <a class="brand" @click.prevent="router.push('/')">
            <span class="brand-mark">S</span>
            <span class="brand-name">SNACK</span>
          </a>

          <nav class="nav-menu">
            <a
              v-for="item in navItems"
              :key="item.path"
              class="nav-link"
              :class="{ active: isActive(item.path) }"
              @click.prevent="router.push(item.path)"
            >
              {{ item.label }}
            </a>
          </nav>
        </div>

        <div class="header-right">
          <a class="cart-link" @click.prevent="router.push('/cart')">
            <el-badge :value="cartStore.totalCount" :hidden="cartStore.isEmpty" :max="99">
              <el-icon :size="20"><ShoppingCart /></el-icon>
            </el-badge>
          </a>

          <template v-if="userStore.isLoggedIn">
            <el-dropdown @command="handleCommand" trigger="click">
              <a class="user-trigger">
                <span class="user-avatar-sm">
                  {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U').charAt(0).toUpperCase() }}
                </span>
                <span class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              </a>
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
            <a class="login-link" @click.prevent="router.push('/login')">登录</a>
          </template>
        </div>
      </div>
    </header>

    <!-- 移动端底部导航 -->
    <nav class="mobile-nav">
      <a
        v-for="tab in mobileTabs"
        :key="tab.path"
        class="mobile-tab"
        :class="{ active: isActive(tab.path) }"
        @click.prevent="handleMobileNav(tab)"
      >
        <el-icon :size="22"><component :is="tab.icon" /></el-icon>
        <span>{{ tab.label }}</span>
      </a>
    </nav>

    <!-- 主内容区 -->
    <main class="main">
      <router-view />
    </main>

    <!-- 底部 -->
    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-brand">SNACK</div>
        <p class="footer-desc">连锁零食 · 门店自提 · 新鲜直达</p>
        <p class="footer-copy">&copy; 2024 Snack Platform</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ShoppingCart, HomeFilled, Shop, List, User } from '@element-plus/icons-vue'
import { useUserStore, useCartStore } from '@/stores'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const navItems = [
  { path: '/', label: '首页' },
  { path: '/stores', label: '门店' },
  { path: '/orders', label: '订单', auth: true },
]

const mobileTabs = [
  { path: '/', label: '首页', icon: HomeFilled },
  { path: '/stores', label: '门店', icon: Shop },
  { path: '/cart', label: '购物车', icon: ShoppingCart },
  { path: '/orders', label: '订单', icon: List, auth: true },
  { path: '/profile', label: '我的', icon: User, auth: true },
]

const isActive = (path) => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

const handleCommand = (command) => {
  const actions = {
    profile: () => router.push('/profile'),
    orders: () => router.push('/orders'),
    logout: () => {
      userStore.logout()
      cartStore.clearCart()
      ElMessage.success('已退出登录')
      router.push('/')
    }
  }
  actions[command]?.()
}

const handleMobileNav = (tab) => {
  if (tab.auth && !userStore.isLoggedIn) {
    router.push('/login')
  } else {
    router.push(tab.path)
  }
}
</script>

<style scoped>
.consumer-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
}

/* ===== Header ===== */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: var(--header-height);
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
}

.header-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-lg);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-2xl);
}

/* Brand */
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  text-decoration: none;
}

.brand-mark {
  width: 32px;
  height: 32px;
  background: var(--color-primary);
  color: #fff;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  letter-spacing: -0.02em;
}

.brand-name {
  font-size: 17px;
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: 0.08em;
}

/* Navigation */
.nav-menu {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}

.nav-link {
  padding: 8px 16px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: color var(--transition-fast), background var(--transition-fast);
  text-decoration: none;
}

.nav-link:hover {
  color: var(--color-text);
  background: var(--color-hover);
}

.nav-link.active {
  color: var(--color-primary);
  background: var(--color-primary-light);
}

/* Right section */
.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.cart-link {
  display: flex;
  align-items: center;
  padding: 8px;
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: color var(--transition-fast), background var(--transition-fast);
}

.cart-link:hover {
  color: var(--color-text);
  background: var(--color-hover);
}

/* User */
.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: background var(--transition-fast);
  text-decoration: none;
}

.user-trigger:hover {
  background: var(--color-hover);
}

.user-avatar-sm {
  width: 32px;
  height: 32px;
  background: var(--color-primary);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 13px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.login-link {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-primary);
  padding: 8px 20px;
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);
  text-decoration: none;
}

.login-link:hover {
  background: var(--color-primary);
  color: #fff;
}

/* ===== Mobile Nav ===== */
.mobile-nav {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: var(--mobile-nav-height);
  background: var(--color-surface);
  border-top: 1px solid var(--color-border);
  z-index: 100;
  padding-bottom: env(safe-area-inset-bottom);
}

.mobile-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  color: var(--color-text-muted);
  font-size: 11px;
  cursor: pointer;
  transition: color var(--transition-fast);
  text-decoration: none;
}

.mobile-tab.active {
  color: var(--color-primary);
}

/* ===== Main ===== */
.main {
  flex: 1;
  margin-top: var(--header-height);
  padding: var(--space-lg);
  min-height: calc(100vh - var(--header-height) - 120px);
}

/* ===== Footer ===== */
.footer {
  background: var(--color-surface);
  border-top: 1px solid var(--color-border);
  padding: var(--space-xl) var(--space-lg);
}

.footer-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  text-align: center;
}

.footer-brand {
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.1em;
  color: var(--color-text);
  margin-bottom: 8px;
}

.footer-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 4px;
}

.footer-copy {
  font-size: 12px;
  color: var(--color-text-muted);
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .header {
    height: 52px;
  }

  .header-inner {
    padding: 0 var(--space-md);
  }

  .nav-menu {
    display: none;
  }

  .mobile-nav {
    display: flex;
  }

  .user-name {
    display: none;
  }

  .user-trigger {
    padding: 4px;
  }

  .login-link {
    padding: 6px 14px;
    font-size: 13px;
  }

  .main {
    margin-top: 52px;
    margin-bottom: var(--mobile-nav-height);
    padding: var(--space-md);
    min-height: calc(100vh - 52px - var(--mobile-nav-height));
  }

  .footer {
    display: none;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .header-inner {
    padding: 0 20px;
  }

  .header-left {
    gap: var(--space-xl);
  }
}

/* Dropdown override */
:deep(.el-dropdown-menu__item) {
  padding: 10px 20px;
  font-size: 14px;
}
</style>
