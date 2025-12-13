import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 布局组件
const ConsumerLayout = () => import('@/layouts/ConsumerLayout.vue')
const AdminLayout = () => import('@/layouts/AdminLayout.vue')

// 公共页面
const Login = () => import('@/views/Login.vue')
const Register = () => import('@/views/Register.vue')

// 消费者端页面
const Home = () => import('@/views/consumer/Home.vue')
const StoreList = () => import('@/views/consumer/StoreList.vue')
const ProductList = () => import('@/views/consumer/ProductList.vue')
const ProductDetail = () => import('@/views/consumer/ProductDetail.vue')
const Cart = () => import('@/views/consumer/Cart.vue')
const OrderList = () => import('@/views/consumer/OrderList.vue')
const OrderDetail = () => import('@/views/consumer/OrderDetail.vue')
const Profile = () => import('@/views/consumer/Profile.vue')

// 门店管理端页面
const StoreDashboard = () => import('@/views/store/Dashboard.vue')
const StoreSkuList = () => import('@/views/store/SkuList.vue')
const StoreOrderList = () => import('@/views/store/OrderList.vue')
const StoreVerify = () => import('@/views/store/Verify.vue')

// 系统管理端页面
const AdminDashboard = () => import('@/views/admin/Dashboard.vue')
const AdminCategoryList = () => import('@/views/admin/CategoryList.vue')
const AdminProductList = () => import('@/views/admin/ProductList.vue')
const AdminStoreList = () => import('@/views/admin/StoreList.vue')
const AdminUserList = () => import('@/views/admin/UserList.vue')
const AdminLogList = () => import('@/views/admin/LogList.vue')

const routes = [
  // 公共路由
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { title: '注册', public: true }
  },

  // 消费者端路由
  {
    path: '/',
    component: ConsumerLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: Home,
        meta: { title: '首页' }
      },
      {
        path: 'stores',
        name: 'StoreList',
        component: StoreList,
        meta: { title: '门店列表' }
      },
      {
        path: 'stores/:storeId/products',
        name: 'ProductList',
        component: ProductList,
        meta: { title: '商品列表' }
      },
      {
        path: 'stores/:storeId/products/:skuId',
        name: 'ProductDetail',
        component: ProductDetail,
        meta: { title: '商品详情' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: Cart,
        meta: { title: '购物车', requireAuth: true }
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: OrderList,
        meta: { title: '我的订单', requireAuth: true }
      },
      {
        path: 'orders/:id',
        name: 'OrderDetail',
        component: OrderDetail,
        meta: { title: '订单详情', requireAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: Profile,
        meta: { title: '个人中心', requireAuth: true }
      }
    ]
  },

  // 门店管理端路由
  {
    path: '/store',
    component: AdminLayout,
    meta: { requireAuth: true, role: 'STORE_ADMIN' },
    children: [
      {
        path: '',
        redirect: '/store/dashboard'
      },
      {
        path: 'dashboard',
        name: 'StoreDashboard',
        component: StoreDashboard,
        meta: { title: '门店概览' }
      },
      {
        path: 'skus',
        name: 'StoreSkuList',
        component: StoreSkuList,
        meta: { title: '商品管理' }
      },
      {
        path: 'orders',
        name: 'StoreOrderList',
        component: StoreOrderList,
        meta: { title: '订单管理' }
      },
      {
        path: 'verify',
        name: 'StoreVerify',
        component: StoreVerify,
        meta: { title: '核销' }
      }
    ]
  },

  // 系统管理端路由
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requireAuth: true, role: 'SYSTEM_ADMIN' },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: AdminDashboard,
        meta: { title: '平台概览' }
      },
      {
        path: 'categories',
        name: 'AdminCategoryList',
        component: AdminCategoryList,
        meta: { title: '分类管理' }
      },
      {
        path: 'products',
        name: 'AdminProductList',
        component: AdminProductList,
        meta: { title: '商品管理' }
      },
      {
        path: 'stores',
        name: 'AdminStoreList',
        component: AdminStoreList,
        meta: { title: '门店管理' }
      },
      {
        path: 'users',
        name: 'AdminUserList',
        component: AdminUserList,
        meta: { title: '用户管理' }
      },
      {
        path: 'logs',
        name: 'AdminLogList',
        component: AdminLogList,
        meta: { title: '操作日志' }
      }
    ]
  },

  // 404
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 零食平台` : '零食平台'

  const userStore = useUserStore()

  // 公开页面直接放行
  if (to.meta.public) {
    // 已登录用户访问登录页，根据角色跳转
    if (userStore.isLoggedIn && (to.name === 'Login' || to.name === 'Register')) {
      if (userStore.isSystemAdmin) {
        return next('/admin')
      } else if (userStore.isStoreAdmin) {
        return next('/store')
      } else {
        return next('/')
      }
    }
    return next()
  }

  // 需要登录的页面
  if (to.meta.requireAuth && !userStore.isLoggedIn) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }

  // 角色权限检查
  if (to.meta.role) {
    if (to.meta.role === 'STORE_ADMIN' && !userStore.isStoreAdmin) {
      return next('/')
    }
    if (to.meta.role === 'SYSTEM_ADMIN' && !userStore.isSystemAdmin) {
      return next('/')
    }
  }

  next()
})

export default router