<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '200px'" class="aside">
      <div class="logo">
        <el-icon><Shop /></el-icon>
        <span v-show="!isCollapse">{{ isStoreAdmin ? '门店管理' : '系统管理' }}</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :router="true"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <!-- 门店管理员菜单 -->
        <template v-if="isStoreAdmin">
          <el-menu-item index="/store/dashboard">
            <el-icon><DataLine /></el-icon>
            <span>门店概览</span>
          </el-menu-item>
          <el-menu-item index="/store/skus">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/store/orders">
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/store/verify">
            <el-icon><CircleCheck /></el-icon>
            <span>核销</span>
          </el-menu-item>
        </template>

        <!-- 系统管理员菜单 -->
        <template v-if="isSystemAdmin">
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataLine /></el-icon>
            <span>平台概览</span>
          </el-menu-item>
          <el-menu-item index="/admin/categories">
            <el-icon><Menu /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/products">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/stores">
            <el-icon><OfficeBuilding /></el-icon>
            <span>门店管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/logs">
            <el-icon><Document /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: isStoreAdmin ? '/store' : '/admin' }">
              {{ isStoreAdmin ? '门店管理' : '系统管理' }}
            </el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :icon="User" />
              <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="isStoreAdmin" command="consumer">
                  <el-icon><House /></el-icon>消费者端
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Shop, DataLine, Goods, List, CircleCheck, Menu, OfficeBuilding,
  Expand, Fold, User, ArrowDown, House, SwitchButton, Document
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapse = ref(false)

const isStoreAdmin = computed(() => userStore.isStoreAdmin)
const isSystemAdmin = computed(() => userStore.isSystemAdmin)

const activeMenu = computed(() => route.path)

const currentTitle = computed(() => route.meta.title || '')

const handleCommand = (command) => {
  switch (command) {
    case 'consumer':
      router.push('/')
      break
    case 'logout':
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
      break
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #263445;
}

.logo .el-icon {
  font-size: 24px;
}

.el-menu {
  border-right: none;
}

.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #666;
}

.collapse-btn:hover {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #333;
}

.username {
  margin-right: 4px;
}

.main {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .admin-layout {
    height: 100vh;
  }
  
  /* 侧边栏在移动端默认折叠 */
  .aside {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1000;
    width: 200px !important;
    transform: translateX(-100%);
    transition: transform 0.3s;
  }
  
  .aside.show {
    transform: translateX(0);
  }
  
  .logo {
    height: 56px;
    font-size: 16px;
  }
  
  .logo .el-icon {
    font-size: 20px;
  }
  
  .header {
    padding: 0 12px;
    height: 56px;
  }
  
  .header-left {
    gap: 12px;
  }
  
  .collapse-btn {
    font-size: 24px;
  }
  
  /* 隐藏面包屑 */
  .el-breadcrumb {
    display: none;
  }
  
  .header-right {
    gap: 8px;
  }
  
  /* 隐藏用户名 */
  .username {
    display: none;
  }
  
  .main {
    padding: 12px;
    height: calc(100vh - 56px);
  }
  
  /* 遮罩层 */
  .aside::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: -1;
    opacity: 0;
    transition: opacity 0.3s;
    pointer-events: none;
  }
  
  .aside.show::before {
    opacity: 1;
    pointer-events: auto;
  }
}

/* 平板优化 */
@media (min-width: 769px) and (max-width: 1024px) {
  .aside {
    width: 180px !important;
  }
  
  .header {
    padding: 0 15px;
  }
  
  .main {
    padding: 15px;
  }
}
</style>