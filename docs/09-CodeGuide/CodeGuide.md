# 毕业设计答辩技术说明文档

> **项目名称**：面向多门店协同的连锁零食电商平台  
> **文档版本**：v1.0  
> **适用对象**：毕业设计答辩  
> **编写日期**：2024年12月

---

## 目录

1. [项目概述](#1-项目概述)
2. [技术架构](#2-技术架构)
3. [技术栈详解](#3-技术栈详解)
4. [核心技术实现](#4-核心技术实现)
5. [系统功能模块](#5-系统功能模块)
6. [数据库设计](#6-数据库设计)
7. [安全机制](#7-安全机制)
8. [项目亮点与创新点](#8-项目亮点与创新点)
9. [答辩常见问题](#9-答辩常见问题)

---

## 1. 项目概述

### 1.1 项目背景

随着新零售模式的兴起，连锁零食店面临着线上线下融合的挑战。本项目旨在构建一个面向多门店协同的连锁零食电商平台，实现：

- **消费者端**：在线浏览商品、下单购买、到店自提
- **门店端**：独立管理库存和定价、处理订单、核销自提
- **管理端**：统一管理商品、门店、用户，查看经营数据

### 1.2 系统角色

| 角色 | 说明 | 主要功能 |
|------|------|----------|
| 消费者（CONSUMER） | 普通用户 | 浏览商品、下单、查看订单、到店自提 |
| 门店管理员（STORE_ADMIN） | 门店运营人员 | 管理本店商品库存/价格、处理订单、核销 |
| 系统管理员（SYSTEM_ADMIN） | 平台运营人员 | 管理商品、门店、用户、查看统计报表 |

### 1.3 业务流程

```
消费者浏览商品 → 选择门店 → 加入购物车 → 提交订单 → 支付
                                                    ↓
                                              生成自提码
                                                    ↓
门店接单 → 备货 → 消费者到店 → 出示自提码 → 门店核销 → 订单完成
```

---

## 2. 技术架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              前端层 (Vue.js 3)                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐         │
│  │   消费者端       │  │  门店管理端      │  │  系统管理端      │         │
│  │   (Vue.js)      │  │   (Vue.js)      │  │   (Vue.js)      │         │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘         │
└───────────┼─────────────────────┼─────────────────────┼─────────────────┘
            │                     │                     │
            └─────────────────────┼─────────────────────┘
                                  │ HTTP/HTTPS (RESTful API)
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         后端层 (Spring Boot 3)                           │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    安全过滤器链                                    │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │   │
│  │  │ JWT Filter  │→ │ CORS Filter │→ │ Auth Check  │              │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘              │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    业务模块层                                      │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐   │   │
│  │  │  Auth   │ │  Store  │ │ Product │ │StoreSku │ │  Order  │   │   │
│  │  │ Module  │ │ Module  │ │ Module  │ │ Module  │ │ Module  │   │   │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘   │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    数据访问层 (MyBatis-Plus)                       │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         数据层 (MySQL 8.0)                               │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐               │
│  │ users  │ │ stores │ │products│ │ orders │ │  ...   │               │
│  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘               │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2.2 前后端分离架构

本项目采用**前后端分离**架构：

| 层次 | 技术 | 职责 |
|------|------|------|
| 前端 | Vue.js 3 + Element Plus | 用户界面、交互逻辑、状态管理 |
| 后端 | Spring Boot 3 | 业务逻辑、数据处理、API服务 |
| 通信 | RESTful API + JSON | 前后端数据交换 |

**优势**：
- 前后端独立开发、独立部署
- 接口标准化，便于维护和扩展
- 支持多端复用（Web、移动端等）

---

## 3. 技术栈详解

### 3.1 后端技术栈

| 技术 | 版本 | 作用 | 选型理由 |
|------|------|------|----------|
| **Java** | 17 | 编程语言 | LTS版本，性能优化，新特性支持 |
| **Spring Boot** | 3.5.8 | 应用框架 | 简化配置，快速开发，生态丰富 |
| **Spring Security** | 6.x | 安全框架 | 认证授权，安全防护 |
| **MyBatis-Plus** | 3.5.5 | ORM框架 | 简化CRUD，代码生成，分页插件 |
| **MySQL** | 8.0 | 关系数据库 | 成熟稳定，事务支持，性能优秀 |
| **JWT** | 0.12.3 | 身份认证 | 无状态，分布式友好 |
| **Lombok** | - | 代码简化 | 减少样板代码 |
| **Maven** | - | 项目构建 | 依赖管理，构建自动化 |

#### Spring Boot 核心特性应用

```java
// 1. 自动配置 - 零配置启动
@SpringBootApplication
public class SnackPlatformBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(SnackPlatformBackendApplication.class, args);
    }
}

// 2. 依赖注入 - 松耦合设计
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;  // 自动注入
    private final StoreSkuService storeSkuService;
}

// 3. 声明式事务 - 简化事务管理
@Transactional
public Order createOrder(Long userId, CreateOrderDTO dto) {
    // 事务内的所有操作要么全部成功，要么全部回滚
}
```

### 3.2 前端技术栈

| 技术 | 版本 | 作用 | 选型理由 |
|------|------|------|----------|
| **Vue.js** | 3.5.24 | 前端框架 | 响应式、组件化、生态完善 |
| **Vue Router** | 4.6.4 | 路由管理 | SPA路由，导航守卫 |
| **Pinia** | 3.0.4 | 状态管理 | Vue3官方推荐，TypeScript友好 |
| **Element Plus** | 2.12.0 | UI组件库 | 企业级组件，开箱即用 |
| **Axios** | 1.13.2 | HTTP客户端 | Promise API，拦截器机制 |
| **ECharts** | 6.0.0 | 数据可视化 | 丰富图表，交互性强 |
| **Vite** | 7.2.4 | 构建工具 | 快速冷启动，热更新 |

#### Vue 3 核心特性应用

```javascript
// 1. Composition API - 逻辑复用
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 响应式状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isStoreAdmin = computed(() => userInfo.value?.role === 'STORE_ADMIN')
  
  // 方法
  async function login(credentials) {
    const res = await loginApi(credentials)
    token.value = res.data.token
    userInfo.value = res.data
  }
  
  return { token, userInfo, isLoggedIn, isStoreAdmin, login }
})

// 2. 响应式系统 - 数据驱动视图
const count = ref(0)
count.value++  // 视图自动更新
```

### 3.3 技术栈版本对照表

```
后端依赖 (pom.xml):
├── spring-boot-starter-parent: 3.5.8
├── spring-boot-starter-web
├── spring-boot-starter-security
├── spring-boot-starter-validation
├── spring-boot-starter-aop
├── mybatis-plus-spring-boot3-starter: 3.5.5
├── mysql-connector-j
├── jjwt-api: 0.12.3
└── lombok

前端依赖 (package.json):
├── vue: ^3.5.24
├── vue-router: ^4.6.4
├── pinia: ^3.0.4
├── element-plus: ^2.12.0
├── axios: ^1.13.2
├── echarts: ^6.0.0
└── vite: ^7.2.4 (devDependencies)
```

---

## 4. 核心技术实现

### 4.1 JWT 身份认证

#### 4.1.1 认证流程

```
┌─────────┐                    ┌─────────┐                    ┌─────────┐
│  前端   │                    │  后端   │                    │ 数据库  │
└────┬────┘                    └────┬────┘                    └────┬────┘
     │                              │                              │
     │ 1. POST /api/auth/login      │                              │
     │ {username, password}         │                              │
     │─────────────────────────────▶│                              │
     │                              │ 2. 查询用户                   │
     │                              │─────────────────────────────▶│
     │                              │◀─────────────────────────────│
     │                              │ 3. 验证密码(BCrypt)          │
     │                              │ 4. 生成JWT Token             │
     │ 5. 返回Token + 用户信息      │                              │
     │◀─────────────────────────────│                              │
     │                              │                              │
     │ 6. 存储Token到localStorage   │                              │
     │                              │                              │
     │ 7. 后续请求携带Token         │                              │
     │ Authorization: Bearer xxx    │                              │
     │─────────────────────────────▶│                              │
     │                              │ 8. JWT过滤器验证Token        │
     │                              │ 9. 解析用户信息              │
     │                              │ 10. 设置SecurityContext      │
     │                              │                              │
```

#### 4.1.2 JWT 工具类实现

```java
// JwtUtil.java - JWT令牌生成与解析
@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;  // 密钥（配置文件中）
    
    @Value("${jwt.expiration}")
    private Long expiration;  // 过期时间
    
    /**
     * 生成Token
     * 将用户信息编码到Token中，实现无状态认证
     */
    public String generateToken(Long userId, String username, String role, Long storeId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        if (storeId != null) {
            claims.put("storeId", storeId);  // 门店管理员携带门店ID
        }
        
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())  // 签发时间
                .expiration(new Date(System.currentTimeMillis() + expiration))  // 过期时间
                .signWith(getSecretKey())  // 签名
                .compact();
    }
    
    /**
     * 验证Token有效性
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());  // 检查是否过期
        } catch (Exception e) {
            return false;
        }
    }
}
```

#### 4.1.3 JWT 过滤器实现

```java
// JwtAuthenticationFilter.java - 请求拦截与认证
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) {
        try {
            // 1. 从请求头提取Token
            String token = extractToken(request);
            
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // 2. 解析Token获取用户信息
                Long userId = jwtUtil.getUserId(token);
                String username = jwtUtil.getUsername(token);
                String roleStr = jwtUtil.getRole(token);
                Long storeId = jwtUtil.getStoreId(token);
                
                // 3. 设置用户上下文（供业务层使用）
                UserContext userContext = new UserContext();
                userContext.setUserId(userId);
                userContext.setUsername(username);
                userContext.setRole(UserRole.valueOf(roleStr));
                userContext.setStoreId(storeId);
                UserContextHolder.set(userContext);
                
                // 4. 设置Spring Security认证信息
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        username, null, 
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleStr))
                    );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            
            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();  // 清理上下文，防止内存泄漏
        }
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

### 4.2 前端请求拦截器

```javascript
// request.js - Axios请求封装
import axios from 'axios'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器 - 自动携带Token
request.interceptors.request.use(config => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers['Authorization'] = `Bearer ${userStore.token}`
  }
  return config
})

// 响应拦截器 - 统一错误处理
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        // Token过期，跳转登录
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    // HTTP错误处理
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期')
      // 清除登录状态
    }
    return Promise.reject(error)
  }
)
```

### 4.3 订单业务流程实现

#### 4.3.1 订单状态机

```
┌──────────────────────────────────────────────────────────────────────────┐
│                           订单状态流转图                                   │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌──────────┐    支付成功    ┌──────────┐    门店接单    ┌──────────┐   │
│  │ 待支付   │───────────────▶│ 待接单   │───────────────▶│ 已接单   │   │
│  │ PENDING  │                │ PENDING  │                │ ACCEPTED │   │
│  │ PAYMENT  │                │ ACCEPT   │                │          │   │
│  └────┬─────┘                └────┬─────┘                └────┬─────┘   │
│       │                          │                            │         │
│       │ 用户取消                  │ 门店拒单                    │ 备货完成 │
│       │ 超时取消                  │ 用户取消                    │         │
│       ▼                          ▼                            ▼         │
│  ┌──────────┐              ┌──────────┐                ┌──────────┐    │
│  │ 已取消   │              │ 已取消   │                │ 待自提   │    │
│  │CANCELLED │              │CANCELLED │                │ READY    │    │
│  │(库存回滚) │              │(退款+回滚)│                │ PICKUP   │    │
│  └──────────┘              └──────────┘                └────┬─────┘    │
│                                                             │          │
│                                                             │ 核销成功  │
│                                                             ▼          │
│                                                       ┌──────────┐     │
│                                                       │ 已完成   │     │
│                                                       │COMPLETED │     │
│                                                       └──────────┘     │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

#### 4.3.2 创建订单（含库存扣减）

```java
// OrderServiceImpl.java - 订单创建核心逻辑
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    @Override
    @Transactional  // 声明式事务，保证原子性
    public Order createOrder(Long userId, CreateOrderDTO dto) {
        // 1. 验证门店状态
        Store store = storeMapper.selectById(dto.getStoreId());
        if (store == null) {
            throw new BusinessException(ResultCode.STORE_NOT_FOUND);
        }
        if (store.getStatus() != 1) {
            throw new BusinessException(ResultCode.STORE_CLOSED);
        }
        
        // 2. 计算订单总金额并验证库存
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemDTO itemDto : dto.getItems()) {
            StoreSku sku = storeSkuService.getById(itemDto.getSkuId());
            
            // 验证SKU有效性
            if (sku == null || !sku.getStoreId().equals(dto.getStoreId())) {
                throw new BusinessException(ResultCode.SKU_NOT_FOUND);
            }
            if (sku.getStatus() != 1) {
                throw new BusinessException(ResultCode.PRODUCT_OFF_SHELF);
            }
            // 验证库存充足
            if (sku.getStock() < itemDto.getQuantity()) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
            }
            
            BigDecimal subtotal = sku.getPrice()
                    .multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
        }
        
        // 3. 创建订单主记录
        Order order = new Order();
        order.setOrderNo(generateOrderNo());  // 生成唯一订单号
        order.setUserId(userId);
        order.setStoreId(dto.getStoreId());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        orderMapper.insert(order);
        
        // 4. 创建订单项并扣减库存
        for (OrderItemDTO itemDto : dto.getItems()) {
            StoreSku sku = storeSkuService.getById(itemDto.getSkuId());
            Product product = productMapper.selectById(sku.getProductId());
            
            // 扣减库存（原子操作，防止超卖）
            boolean success = storeSkuService.deductStock(sku.getId(), itemDto.getQuantity());
            if (!success) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
            }
            
            // 创建订单项（保存价格快照）
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setSkuId(sku.getId());
            item.setProductName(product.getName());  // 快照
            item.setProductImage(product.getImage());  // 快照
            item.setPrice(sku.getPrice());  // 下单时价格快照
            item.setQuantity(itemDto.getQuantity());
            item.setSubtotal(sku.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            orderItemMapper.insert(item);
        }
        
        return order;
    }
    
    /**
     * 生成订单编号 - 使用SecureRandom保证安全性
     * 格式：ORD + 时间戳 + 6位随机数
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%06d", SECURE_RANDOM.nextInt(1000000));
        return "ORD" + timestamp + random;
    }
}
```

#### 4.3.3 库存扣减（防止超卖）

```java
// StoreSkuServiceImpl.java - 库存操作
@Service
public class StoreSkuServiceImpl implements StoreSkuService {
    
    /**
     * 扣减库存 - 使用乐观锁防止超卖
     * SQL: UPDATE store_skus SET stock = stock - #{qty} 
     *      WHERE id = #{id} AND stock >= #{qty}
     */
    @Override
    public boolean deductStock(Long skuId, Integer quantity) {
        // 原子操作：只有库存充足时才扣减
        int affected = storeSkuMapper.deductStock(skuId, quantity);
        return affected > 0;  // 返回是否扣减成功
    }
    
    /**
     * 恢复库存 - 订单取消时调用
     */
    @Override
    public void restoreStock(Long skuId, Integer quantity) {
        storeSkuMapper.restoreStock(skuId, quantity);
    }
}

// Mapper层SQL
@Update("UPDATE store_skus SET stock = stock - #{qty} " +
        "WHERE id = #{id} AND stock >= #{qty}")
int deductStock(@Param("id") Long id, @Param("qty") Integer qty);
```

### 4.4 RBAC 权限控制

#### 4.4.1 权限模型

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           RBAC 权限模型                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────┐         ┌──────────┐         ┌──────────┐               │
│  │   用户   │────────▶│   角色   │────────▶│   权限   │               │
│  │  User    │  1:1    │   Role   │  1:N    │Permission│               │
│  └──────────┘         └──────────┘         └──────────┘               │
│                                                                         │
│  角色定义：                                                             │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ CONSUMER     - 消费者：浏览商品、下单、查看自己订单              │   │
│  │ STORE_ADMIN  - 门店管理员：管理本店商品、处理本店订单            │   │
│  │ SYSTEM_ADMIN - 系统管理员：管理全平台商品、门店、用户            │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

#### 4.4.2 权限注解实现

```java
// RequireRole.java - 自定义权限注解
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    UserRole[] value();  // 允许的角色列表
}

// 使用示例
@RestController
@RequestMapping("/api/store")
@RequireRole(UserRole.STORE_ADMIN)  // 类级别：整个控制器需要门店管理员权限
public class StoreAdminOrderController {
    
    @PostMapping("/orders/{id}/accept")
    public Result<Void> acceptOrder(@PathVariable Long id) {
        // 只有门店管理员可以接单
    }
}

@RestController
@RequestMapping("/api/admin")
@RequireRole(UserRole.SYSTEM_ADMIN)  // 系统管理员专属接口
public class AdminProductController {
    // ...
}
```

#### 4.4.3 数据隔离（门店级别）

```java
// 门店管理员只能操作自己门店的数据
@Service
public class StoreOrderServiceImpl {
    
    public void acceptOrder(Long orderId, Long storeId) {
        Order order = orderMapper.selectById(orderId);
        
        // 校验订单归属 - 数据隔离的关键
        if (!order.getStoreId().equals(storeId)) {
            throw new BusinessException("该订单不属于本店");
        }
        
        // 执行接单逻辑...
    }
}
```

---

## 5. 系统功能模块

### 5.1 模块划分

| 模块 | 功能 | 主要接口 |
|------|------|----------|
| **Auth** | 认证授权 | 登录、注册、获取用户信息 |
| **Store** | 门店管理 | 门店CRUD、状态管理、管理员分配 |
| **Product** | 商品管理 | 商品CRUD、分类管理、上下架 |
| **StoreSku** | 门店商品 | 上架、定价、库存管理 |
| **Order** | 订单管理 | 下单、支付、接单、核销 |
| **Report** | 统计报表 | 销售统计、门店对比 |

### 5.2 API 路径规范

| 前缀 | 说明 | 权限要求 |
|------|------|----------|
| `/api/auth/*` | 认证相关 | 公开 |
| `/api/*` | 消费者端接口 | 消费者登录 |
| `/api/store/*` | 门店管理端接口 | 门店管理员 |
| `/api/admin/*` | 系统管理端接口 | 系统管理员 |

### 5.3 统一响应格式

```java
// Result.java - 统一响应封装
@Data
public class Result<T> {
    private Integer code;    // 状态码：200成功，其他失败
    private String message;  // 提示信息
    private T data;          // 业务数据
    
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }
    
    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        return result;
    }
}

// 响应示例
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "orderNo": "ORD20241213143000123456",
        "status": "PENDING_PAYMENT"
    }
}
```

---

## 6. 数据库设计

### 6.1 ER 关系图

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   users     │       │   stores    │       │  categories │
│─────────────│       │─────────────│       │─────────────│
│ id (PK)     │◄──┐   │ id (PK)     │       │ id (PK)     │
│ username    │   │   │ name        │       │ name        │
│ role        │   │   │ admin_id(FK)│───┐   └──────┬──────┘
└──────┬──────┘   │   └──────┬──────┘   │          │
       │          │          │          │          │
       ▼          │          │          │          ▼
┌─────────────┐   │          │          │   ┌─────────────┐
│user_addresses│  │          │          │   │  products   │
│─────────────│   │          │          │   │─────────────│
│ user_id(FK) │───┘          │          └──▶│ category_id │
└─────────────┘              │              └──────┬──────┘
                             │                     │
                             ▼                     ▼
                      ┌─────────────┐       ┌─────────────┐
                      │store_products│◄─────│  (关联)     │
                      │─────────────│       └─────────────┘
                      │ store_id(FK)│
                      │ product_id  │
                      │ price       │  ◄── 门店独立定价
                      │ stock       │  ◄── 门店独立库存
                      └──────┬──────┘
                             │
       ┌─────────────────────┼─────────────────────┐
       │                     │                     │
       ▼                     ▼                     ▼
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│ cart_items  │       │   orders    │       │pickup_records│
│─────────────│       │─────────────│       │─────────────│
│ user_id     │       │ user_id     │       │ order_id(FK)│
│ store_id    │       │ store_id    │       │ pickup_code │
│store_product│       │ status      │       └─────────────┘
│             │       │ pickup_code │
│             │       └──────┬──────┘
│             │              │
│             │              ▼
│             │       ┌─────────────┐
│             │       │ order_items │
│             │       │─────────────│
│             │       │ order_id(FK)│
│             │       │ price       │  ◄── 下单时价格快照
└─────────────┘       └─────────────┘
```

### 6.2 核心表设计

#### 6.2.1 用户表 (users)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名（唯一） |
| password | VARCHAR(100) | 密码（BCrypt加密） |
| role | VARCHAR(20) | 角色：CONSUMER/STORE_ADMIN/SYSTEM_ADMIN |
| status | TINYINT | 状态：0-禁用，1-启用 |

#### 6.2.2 门店商品表 (store_products) - 核心表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| store_id | BIGINT | 门店ID |
| product_id | BIGINT | 平台商品ID |
| **price** | DECIMAL(10,2) | **门店销售价格**（独立定价） |
| **stock** | INT | **当前库存数量**（独立库存） |
| status | TINYINT | 上架状态：0-下架，1-上架 |

**设计要点**：
- `uk_store_product` 唯一索引确保同一门店同一商品只有一条记录
- 每个门店可以独立设置价格和库存
- 实现了"平台统一商品 + 门店独立运营"的业务模式

#### 6.2.3 订单表 (orders)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| order_no | VARCHAR(32) | 订单编号（唯一） |
| user_id | BIGINT | 下单用户ID |
| store_id | BIGINT | 履约门店ID |
| status | VARCHAR(20) | 订单状态 |
| total_amount | DECIMAL(10,2) | 订单总金额 |
| **pickup_code** | VARCHAR(16) | **自提码**（支付后生成） |

### 6.3 索引设计

| 表 | 索引 | 类型 | 用途 |
|-----|------|------|------|
| users | uk_username | 唯一 | 登录查询 |
| store_products | uk_store_product | 唯一 | 门店+商品唯一约束 |
| orders | uk_order_no | 唯一 | 订单号查询 |
| orders | uk_pickup_code | 唯一 | 自提码核销 |
| orders | idx_store_status | 普通 | 门店订单列表 |

---

## 7. 安全机制

### 7.1 认证安全

| 安全措施 | 实现方式 |
|----------|----------|
| 密码加密 | BCrypt 单向哈希，防止密码泄露 |
| Token签名 | HMAC-SHA256 签名，防止篡改 |
| Token过期 | 设置有效期，过期需重新登录 |
| 安全随机 | SecureRandom 生成订单号和自提码 |

### 7.2 授权安全

| 安全措施 | 实现方式 |
|----------|----------|
| 角色控制 | @RequireRole 注解，接口级权限 |
| 数据隔离 | 门店管理员只能操作本店数据 |
| 所有权校验 | 用户只能操作自己的订单 |

### 7.3 输入验证

```java
// 使用 Spring Validation 进行参数校验
public class CreateOrderDTO {
    @NotNull(message = "门店ID不能为空")
    private Long storeId;
    
    @NotEmpty(message = "订单项不能为空")
    @Size(min = 1, max = 50, message = "订单项数量1-50")
    private List<OrderItemDTO> items;
}
```

### 7.4 异常处理

```java
// 全局异常处理器
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getResultCode());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.error(400, message);
    }
}
```

---

## 8. 项目亮点与创新点

### 8.1 技术亮点

| 亮点 | 说明 |
|------|------|
| **前后端分离架构** | Vue.js + Spring Boot，职责清晰，便于维护 |
| **JWT无状态认证** | 支持分布式部署，无需Session共享 |
| **声明式事务管理** | @Transactional 保证数据一致性 |
| **乐观锁防超卖** | 库存扣减使用条件更新，防止并发超卖 |
| **统一响应封装** | 标准化API响应格式，便于前端处理 |
| **全局异常处理** | 统一异常处理，友好错误提示 |

### 8.2 业务创新

| 创新点 | 说明 |
|--------|------|
| **多门店协同** | 平台统一商品 + 门店独立定价库存 |
| **自提码核销** | 线上下单 + 线下自提，O2O模式 |
| **数据隔离** | 门店管理员只能操作本店数据 |
| **订单状态机** | 完整的订单生命周期管理 |

### 8.3 代码质量

| 方面 | 实践 |
|------|------|
| **分层架构** | Controller → Service → Mapper 三层分离 |
| **命名规范** | 统一的类、方法、变量命名规范 |
| **注释文档** | 关键代码有清晰注释 |
| **单元测试** | Service层核心逻辑有测试覆盖 |

---

## 9. 答辩常见问题

### 9.1 技术选型类

**Q1: 为什么选择 Spring Boot 而不是其他框架？**

A: Spring Boot 具有以下优势：
1. **自动配置**：约定优于配置，减少样板代码
2. **生态丰富**：Spring Security、Spring Data 等无缝集成
3. **企业级**：成熟稳定，广泛应用于生产环境
4. **社区活跃**：文档完善，问题易于解决

**Q2: 为什么使用 JWT 而不是 Session？**

A: JWT 的优势：
1. **无状态**：服务端不存储会话，便于水平扩展
2. **跨域友好**：Token 可以在不同域名间传递
3. **自包含**：Token 中包含用户信息，减少数据库查询
4. **分布式友好**：无需 Session 共享机制

**Q3: 为什么选择 MyBatis-Plus 而不是 JPA？**

A: MyBatis-Plus 的优势：
1. **SQL可控**：可以编写和优化复杂SQL
2. **学习曲线**：对于熟悉SQL的开发者更友好
3. **性能优化**：便于进行SQL级别的性能调优
4. **代码生成**：内置代码生成器，提高开发效率

### 9.2 架构设计类

**Q4: 如何保证订单创建时不会超卖？**

A: 采用乐观锁机制：
```sql
UPDATE store_skus SET stock = stock - #{qty}
WHERE id = #{id} AND stock >= #{qty}
```
- 只有当库存充足时才会扣减成功
- 返回影响行数为0表示库存不足
- 配合事务回滚保证数据一致性

**Q5: 如何实现门店数据隔离？**

A: 多层保障：
1. **JWT携带storeId**：登录时将门店ID编码到Token
2. **Service层校验**：操作前验证数据归属
3. **SQL条件过滤**：查询时自动添加storeId条件

**Q6: 订单状态是如何管理的？**

A: 采用状态机模式：
1. 定义明确的状态枚举（PENDING_PAYMENT → PENDING_ACCEPT → ...）
2. 每个状态转换都有前置条件校验
3. 状态变更记录时间戳，便于追踪

### 9.3 实现细节类

**Q7: 自提码是如何生成的？**

A: 使用 SecureRandom 生成6位数字：
```java
private static final SecureRandom SECURE_RANDOM = new SecureRandom();

private String generatePickupCode() {
    return String.format("%06d", SECURE_RANDOM.nextInt(1000000));
}
```
- SecureRandom 比 Random 更安全，不可预测
- 6位数字便于用户记忆和输入

**Q8: 前端是如何管理用户状态的？**

A: 使用 Pinia 状态管理：
1. 登录成功后存储 Token 和用户信息到 localStorage
2. Pinia Store 提供响应式状态和计算属性
3. 请求拦截器自动携带 Token
4. 响应拦截器处理 401 自动登出

**Q9: 如何处理订单超时？**

A: 定时任务扫描：
```java
@Scheduled(cron = "0 * * * * ?")  // 每分钟执行
public void handlePaymentTimeout() {
    // 查询超过30分钟未支付的订单
    // 自动取消并恢复库存
}
```

### 9.4 扩展性问题

**Q10: 如果要支持更多门店，系统如何扩展？**

A: 当前架构支持水平扩展：
1. **无状态后端**：JWT认证，可部署多实例
2. **数据库分表**：按门店ID分表（未来优化）
3. **缓存层**：可引入Redis缓存热点数据
4. **消息队列**：可引入MQ解耦订单处理

**Q11: 如果要增加新的用户角色怎么办？**

A: 扩展步骤：
1. 在 UserRole 枚举中添加新角色
2. 创建对应的 Controller 和接口
3. 使用 @RequireRole 注解配置权限
4. 前端添加对应的路由和页面

---

## 附录

### A. 项目结构

```
snack-platform/
├── snack-platform-backend/          # 后端项目
│   ├── src/main/java/
│   │   └── com/snackchain/snack_platform_backend/
│   │       ├── common/              # 公共模块
│   │       │   ├── config/          # 配置类
│   │       │   ├── exception/       # 异常处理
│   │       │   ├── result/          # 统一响应
│   │       │   └── util/            # 工具类
│   │       ├── security/            # 安全模块
│   │       │   ├── annotation/      # 权限注解
│   │       │   ├── context/         # 用户上下文
│   │       │   └── filter/          # 过滤器
│   │       ├── module/              # 业务模块
│   │       │   ├── auth/            # 认证模块
│   │       │   ├── store/           # 门店模块
│   │       │   ├── product/         # 商品模块
│   │       │   ├── sku/             # 门店商品模块
│   │       │   ├── order/           # 订单模块
│   │       │   └── report/          # 统计模块
│   │       ├── entity/              # 实体类
│   │       ├── mapper/              # 数据访问层
│   │       └── enums/               # 枚举类
│   └── pom.xml
│
├── snack-platform-frontend/         # 前端项目
│   ├── src/
│   │   ├── api/                     # API接口
│   │   ├── stores/                  # Pinia状态管理
│   │   ├── router/                  # 路由配置
│   │   ├── views/                   # 页面组件
│   │   │   ├── consumer/            # 消费者端页面
│   │   │   ├── store/               # 门店端页面
│   │   │   └── admin/               # 管理端页面
│   │   └── layouts/                 # 布局组件
│   └── package.json
│
└── docs/                            # 项目文档
    ├── 01-Requirements/             # 需求文档
    ├── 02-Architecture/             # 架构文档
    ├── 03-API/                      # API文档
    ├── 04-DB/                       # 数据库文档
    └── 09-CodeGuide/                # 代码说明
```

### B. 运行项目

```bash
# 1. 启动后端
cd snack-platform-backend
./mvnw spring-boot:run

# 2. 启动前端
cd snack-platform-frontend
npm install
npm run dev

# 3. 访问
# 前端: http://localhost:5173
# 后端: http://localhost:8080
```

### C. 参考文档

- [项目架构文档](../02-Architecture/Architecture.md)
- [API接口文档](../03-API/openapi.md)
- [数据库设计文档](../04-DB/schema.md)
- [部署文档](../08-Deployment/Deployment.md)

---

> **文档说明**：本文档为毕业设计答辩准备材料，详细介绍了项目的技术架构、核心实现和设计思路。如有疑问，请参考相关技术文档或源代码。