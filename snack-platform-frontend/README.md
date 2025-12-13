# 🍿 Snack Platform Frontend

连锁零食电商平台前端应用

> 📚 **项目文档**：[文档中心](../docs/README.md) | [快速开始](../docs/QUICKSTART.md) | [架构设计](../docs/02-Architecture/Architecture.md)

## 📋 技术栈

- **框架**: Vue.js 3.5
- **UI 组件**: Element Plus 2.12
- **路由**: Vue Router 4.6
- **状态管理**: Pinia 3.0
- **HTTP 客户端**: Axios 1.13
- **构建工具**: Vite 7.2

## 🚀 快速开始

### 环境要求

- Node.js 18+
- npm 或 yarn

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

应用将在 `http://localhost:3000` 启动

### 构建生产版本

```bash
npm run build
```

构建产物位于 `dist/` 目录

### 预览生产版本

```bash
npm run preview
```

## 📁 项目结构

```
src/
├── api/                    # API 接口层
│   ├── auth.js             # 认证接口
│   ├── index.js            # 接口导出
│   ├── order.js            # 订单接口
│   ├── product.js          # 商品接口
│   ├── report.js           # 报表接口
│   ├── request.js          # Axios 封装
│   ├── sku.js              # 库存接口
│   └── store.js            # 门店接口
├── assets/                 # 静态资源
├── components/             # 公共组件
├── layouts/                # 布局组件
│   ├── AdminLayout.vue     # 管理端布局
│   └── ConsumerLayout.vue  # 消费者端布局
├── router/                 # 路由配置
│   └── index.js
├── stores/                 # Pinia 状态管理
│   ├── cart.js             # 购物车状态
│   ├── index.js
│   └── user.js             # 用户状态
├── views/                  # 页面组件
│   ├── admin/              # 系统管理端页面
│   │   ├── CategoryList.vue
│   │   ├── Dashboard.vue
│   │   ├── ProductList.vue
│   │   └── StoreList.vue
│   ├── consumer/           # 消费者端页面
│   │   ├── Cart.vue
│   │   ├── Home.vue
│   │   ├── OrderDetail.vue
│   │   ├── OrderList.vue
│   │   ├── ProductList.vue
│   │   ├── Profile.vue
│   │   └── StoreList.vue
│   ├── store/              # 门店管理端页面
│   │   ├── Dashboard.vue
│   │   ├── OrderList.vue
│   │   ├── SkuList.vue
│   │   └── Verify.vue
│   ├── Login.vue           # 登录页
│   └── Register.vue        # 注册页
├── App.vue                 # 根组件
├── main.js                 # 入口文件
└── style.css               # 全局样式
```

## 🎨 页面说明

### 公共页面

| 页面 | 路径 | 说明 |
|------|------|------|
| 登录 | `/login` | 用户登录 |
| 注册 | `/register` | 用户注册 |

### 消费者端

| 页面 | 路径 | 说明 |
|------|------|------|
| 首页 | `/` | 门店选择入口 |
| 门店列表 | `/stores` | 浏览所有门店 |
| 商品列表 | `/stores/:storeId/products` | 门店商品浏览 |
| 购物车 | `/cart` | 购物车管理 |
| 订单列表 | `/orders` | 我的订单 |
| 订单详情 | `/orders/:id` | 订单详情与自提码 |
| 个人中心 | `/profile` | 个人信息管理 |

### 门店管理端

| 页面 | 路径 | 说明 |
|------|------|------|
| 仪表盘 | `/store/dashboard` | 门店数据概览 |
| 库存管理 | `/store/skus` | SKU 库存管理 |
| 订单管理 | `/store/orders` | 订单处理 |
| 核销 | `/store/verify` | 自提核销 |

### 系统管理端

| 页面 | 路径 | 说明 |
|------|------|------|
| 仪表盘 | `/admin/dashboard` | 系统数据概览 |
| 分类管理 | `/admin/categories` | 商品分类管理 |
| 商品管理 | `/admin/products` | 商品信息管理 |
| 门店管理 | `/admin/stores` | 门店信息管理 |

## 🔧 配置说明

### Vite 配置

```javascript
// vite.config.js
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

### 环境变量

创建 `.env` 文件配置环境变量：

```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8080/api

# .env.production
VITE_API_BASE_URL=/api
```

## 📦 状态管理

### 用户状态 (user.js)

```javascript
// 状态
state: {
  token: null,
  user: null
}

// 方法
- login(credentials)    // 登录
- logout()              // 登出
- fetchUser()           // 获取用户信息
- updateProfile(data)   // 更新用户信息
```

### 购物车状态 (cart.js)

```javascript
// 状态
state: {
  storeId: null,
  items: []
}

// 方法
- addItem(item)         // 添加商品
- removeItem(skuId)     // 移除商品
- updateQuantity(skuId, quantity)  // 更新数量
- clearCart()           // 清空购物车
- setStore(storeId)     // 设置门店
```

## 🌐 API 封装

### 请求拦截器

```javascript
// 自动添加 Token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
```

### 响应拦截器

```javascript
// 统一错误处理
request.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      // Token 过期，跳转登录
      router.push('/login')
    }
    return Promise.reject(error)
  }
)
```

## 🎯 路由守卫

```javascript
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')

  // 需要登录的页面
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  // 角色权限检查
  if (to.meta.roles && !to.meta.roles.includes(user?.role)) {
    next('/')
    return
  }

  next()
})
```

## 🎨 UI 组件使用

### Element Plus 按需导入

```javascript
// main.js
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

app.use(ElementPlus)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
```

### 常用组件示例

```vue
<!-- 表格 -->
<el-table :data="tableData" v-loading="loading">
  <el-table-column prop="name" label="名称" />
  <el-table-column label="操作">
    <template #default="{ row }">
      <el-button @click="handleEdit(row)">编辑</el-button>
    </template>
  </el-table-column>
</el-table>

<!-- 表单 -->
<el-form :model="form" :rules="rules" ref="formRef">
  <el-form-item label="名称" prop="name">
    <el-input v-model="form.name" />
  </el-form-item>
  <el-form-item>
    <el-button type="primary" @click="handleSubmit">提交</el-button>
  </el-form-item>
</el-form>

<!-- 消息提示 -->
<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'

ElMessage.success('操作成功')
ElMessageBox.confirm('确认删除？').then(() => {
  // 确认操作
})
</script>
```

## 📱 响应式设计

使用 Element Plus 的栅格系统：

```vue
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8" :lg="6">
    <!-- 内容 -->
  </el-col>
</el-row>
```

## 🧪 开发调试

### Vue DevTools

安装 Vue DevTools 浏览器扩展进行调试

### 网络请求调试

在浏览器开发者工具的 Network 面板查看 API 请求

### 状态调试

使用 Vue DevTools 的 Pinia 面板查看状态变化

## 📦 构建优化

### 代码分割

```javascript
// 路由懒加载
const Home = () => import('@/views/consumer/Home.vue')
```

### 生产构建

```bash
npm run build
```

构建产物：
- `dist/index.html` - 入口 HTML
- `dist/assets/` - JS/CSS/图片资源

## 🚢 部署

### Nginx 配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /var/www/snack-platform/dist;
    index index.html;

    # SPA 路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 静态资源缓存
    location /assets {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### Docker 部署

```dockerfile
# 构建阶段
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# 运行阶段
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## 📝 开发规范

### 命名规范

- 组件文件：PascalCase（如 `ProductList.vue`）
- 组合式函数：camelCase（如 `useCart.js`）
- 常量：UPPER_SNAKE_CASE

### 组件规范

```vue
<script setup>
// 1. 导入
import { ref, computed, onMounted } from 'vue'

// 2. Props/Emits
const props = defineProps({
  id: Number
})
const emit = defineEmits(['update'])

// 3. 响应式数据
const data = ref(null)

// 4. 计算属性
const computed = computed(() => {})

// 5. 方法
const handleClick = () => {}

// 6. 生命周期
onMounted(() => {})
</script>

<template>
  <!-- 模板 -->
</template>

<style scoped>
/* 样式 */
</style>
```

## 📄 许可证

MIT License

---

📚 返回 [项目主页](../README.md) | [文档中心](../docs/README.md)
