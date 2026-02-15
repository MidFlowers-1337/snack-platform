# 项目优化方案文档

> **项目名称**：面向多门店协同的连锁零食电商平台
> **文档版本**：v1.0
> **编写日期**：2026年2月
> **文档性质**：项目优化与增强方案（毕业设计二期迭代）
> **分析方法**：三维并行深度分析（后端代码审计 × 前端代码审计 × PRD 差距对比）

---

## 目录

1. [项目现状评估](#1-项目现状评估)
2. [MVP 功能缺口分析](#2-mvp-功能缺口分析)
3. [代码质量优化](#3-代码质量优化)
4. [安全性增强方案](#4-安全性增强方案)
5. [性能优化方案](#5-性能优化方案)
6. [新增模块推荐](#6-新增模块推荐)
7. [前端工程化优化](#7-前端工程化优化)
8. [执行路线图](#8-执行路线图)

---

## 1. 项目现状评估

### 1.1 整体评分

| 评估维度 | 评分（/10） | 评价 |
|----------|------------|------|
| 架构设计 | **8** | Controller/Service/Mapper 分层清晰，模块化好 |
| 后端代码质量 | **7** | 事务管理正确、库存乐观锁防超卖，但存在 N+1 查询 |
| 前端代码质量 | **6.5** | 三端页面基本齐全，但公共组件零提取、大量代码重复 |
| 安全性 | **6.5** | BCrypt + JWT 基础安全，缺 Token 黑名单和暴力破解防护 |
| 功能完整性 | **6** | 核心流程打通，但购物车 API 完全缺失 |
| 测试覆盖 | **7** | 有 6 个 Service 单元测试且质量好，缺 Controller/集成测试 |
| 可维护性 | **8** | Lombok、统一 Result、操作日志切面设计良好 |

### 1.2 User Story 完成度

基于 Backlog 定义的 **27 个 User Story、139 个验收标准**：

| 状态 | Story 数量 | 占比 |
|------|-----------|------|
| 已完成（AC ≥ 80%） | 18 | 67% |
| 大部分完成（AC 50-80%） | 6 | 22% |
| 未完成（AC < 50%） | 3 | 11% |

**整体验收标准通过率：约 70%（95-100/139）**

### 1.3 已实现的核心能力

- ✅ 三级角色权限隔离（消费者/门店管理员/系统管理员）
- ✅ 完整订单状态机（待支付→待接单→已接单→待自提→已完成/已取消）
- ✅ SPU-SKU 二级商品架构（平台商品 + 门店独立定价和库存）
- ✅ 库存乐观锁防超卖（CAS 原子操作）
- ✅ 订单超时自动取消（定时任务）
- ✅ 自提码生成与核销
- ✅ 操作日志 AOP 切面
- ✅ BCrypt 密码加密 + JWT 无状态认证
- ✅ 全局异常处理 + 统一 Result 响应

---

## 2. MVP 功能缺口分析

### 2.1 P0 核心功能缺口（必须修复）

#### 缺口 1：购物车 CRUD API 缺失

| 项目 | 详情 |
|------|------|
| **现状** | `CartItem` entity + `CartItemMapper` 已存在，前端 `Cart.vue` 已开发，但**后端完全没有 CartController 和 CartService** |
| **影响** | 购物车→下单链路断裂，无法完整演示核心业务流程 |
| **修复方案** | 创建 `CartController`，实现 5 个 RESTful 端点 |
| **工作量** | 1 天 |

需实现的 API 端点：

```
GET    /api/cart                    -- 获取当前用户购物车列表
POST   /api/cart                    -- 添加商品到购物车
PUT    /api/cart/{id}               -- 更新购物车项数量
DELETE /api/cart/{id}               -- 删除购物车项
DELETE /api/cart/clear/{storeId}    -- 清空指定门店购物车
```

#### 缺口 2：定位推荐门店

| 项目 | 详情 |
|------|------|
| **现状** | 后端 `StoreController.nearby()` 已支持经纬度参数 + Haversine 距离排序，但前端未调用浏览器 Geolocation API |
| **影响** | "定位推荐最近门店"功能不可用 |
| **修复方案** | 前端 `Home.vue` / `StoreList.vue` 调用 `navigator.geolocation.getCurrentPosition()`，传入经纬度 |
| **工作量** | 0.5 天 |

#### 缺口 3：商品图片上传

| 项目 | 详情 |
|------|------|
| **现状** | 所有图片字段均为 URL 字符串，无文件上传接口和存储服务 |
| **影响** | 创建商品时只能手填 URL，无法上传本地图片 |
| **修复方案** | 后端新增 `FileUploadController`，先用本地存储，后续可迁移至 OSS |
| **工作量** | 1 天 |

### 2.2 P1 重要功能缺口（建议补齐）

| # | 功能 | 缺失内容 | 修复工作量 |
|---|------|---------|-----------|
| 1 | 库存预警 | `StoreSku` 无 `lowStockThreshold` 字段，无预警任务 | 1 天 |
| 2 | 门店业绩对比 | 无 store-comparison API，AdminReportController 仅有 dashboard/salesReport | 1 天 |
| 3 | 商品销售分析 | 无商品维度销售排行接口 | 0.5 天 |
| 4 | 核销记录查询 | 无独立核销历史查询接口（仅可通过订单状态间接查） | 0.5 天 |
| 5 | 地址管理 | 无 `UserAddress` 实体和 CRUD（到店自提模式需求较弱，但完善度需要） | 1 天 |
| 6 | 业务参数配置 | 超时时间等参数硬编码，无动态配置接口 | 0.5 天 |

### 2.3 前端功能缺口

| # | 问题 | 严重度 | 位置 | 修复方案 |
|---|------|--------|------|---------|
| 1 | **Dashboard 图表使用 mock 假数据** | 🔴 | `admin/Dashboard.vue:323`（Math.random）、`store/Dashboard.vue:331-337` | 接入后端 Report API 真实数据 |
| 2 | **修改密码是空壳功能** | 🔴 | `Profile.vue:264-266` API 调用被注释，点击后登出但密码未改 | 后端实现密码修改 API + 取消注释 |
| 3 | **goToReport 跳转死路由** | 🟡 | `admin/Dashboard.vue:294` 跳 `/admin/report`，路由不存在 | 注册路由或移除按钮 |
| 4 | **request.js baseURL 硬编码** | 🟡 | `api/request.js:8` 写死 `'/api'`，`.env` 配置不生效 | 改为 `import.meta.env.VITE_API_BASE_URL \|\| '/api'` |
| 5 | **无全局商品搜索** | 🟡 | Home.vue 无搜索框 | 新增搜索组件 + 搜索结果页 |
| 6 | **无门店详情页** | 🟡 | 消费者无法查看门店营业时间、联系方式 | 新增 `StoreDetail.vue` |

---

## 3. 代码质量优化

### 3.1 后端代码优化

#### 问题 1：N+1 查询（高优先级）

**位置**：`OrderServiceImpl.fillOrderDetails()`

```java
// ❌ 当前：循环中逐个查询（N+1）
for (Order order : orders) {
    List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
    order.setItems(items);
}

// ✅ 优化：批量查询 + Map 分组
List<Long> orderIds = orders.stream()
    .map(Order::getId)
    .collect(Collectors.toList());
Map<Long, List<OrderItem>> itemsMap = orderItemMapper
    .selectByOrderIds(orderIds)  // 新增批量查询方法
    .stream()
    .collect(Collectors.groupingBy(OrderItem::getOrderId));
orders.forEach(o -> o.setItems(
    itemsMap.getOrDefault(o.getId(), Collections.emptyList())
));
```

#### 问题 2：JWT 重复解析 4 次（中优先级）

**位置**：`JwtAuthenticationFilter.doFilterInternal()`

```java
// ❌ 当前：每次调用都重新解析 token
Long userId = jwtUtil.getUserId(token);       // 第 1 次解析
String username = jwtUtil.getUsername(token);   // 第 2 次解析
String role = jwtUtil.getRole(token);          // 第 3 次解析
Long storeId = jwtUtil.getStoreId(token);      // 第 4 次解析

// ✅ 优化：解析一次取全部 Claims
Claims claims = jwtUtil.parseToken(token);     // 仅解析 1 次
Long userId = claims.get("userId", Long.class);
String username = claims.getSubject();
String role = claims.get("role", String.class);
Long storeId = claims.get("storeId", Long.class);
```

#### 问题 3：报表内存聚合 OOM 风险（中优先级）

**位置**：`ReportServiceImpl`

```java
// ❌ 当前：selectList 全量加载到内存做 stream 聚合
List<Order> allOrders = orderMapper.selectList(wrapper);
// 然后用 Java stream 做 groupBy/sum...

// ✅ 优化：SQL 层面 GROUP BY 聚合
// OrderMapper 新增：
@Select("""
    SELECT DATE(create_time) AS date,
           COUNT(*) AS orderCount,
           SUM(CASE WHEN status >= 1 AND status != 5
               THEN total_amount ELSE 0 END) AS salesAmount
    FROM orders
    WHERE store_id = #{storeId}
      AND create_time BETWEEN #{startDate} AND #{endDate}
    GROUP BY DATE(create_time)
    ORDER BY date
    """)
List<DailySalesVO> selectDailySales(@Param("storeId") Long storeId,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);
```

#### 问题 4：其他后端优化项

| 问题 | 位置 | 优化方案 |
|------|------|---------|
| 魔法数字 | `OrderServiceImpl` 中 `status != 1` | 使用枚举常量替换 |
| 拒单原因混入 remark | `OrderServiceImpl.reject()` | 新增独立 `rejectReason` 字段 |
| 注册并发竞态 | `AuthServiceImpl.register()` | 数据库 `UNIQUE KEY uk_username` 已兜底，catch `DuplicateKeyException` |
| updateProfile 缺校验 | `AuthController` | 为 `@RequestBody UpdateProfileDTO dto` 添加 `@Valid` |
| 缺少常见异常处理 | `GlobalExceptionHandler` | 补充 `HttpRequestMethodNotSupportedException`、`MissingServletRequestParameterException` |

### 3.2 前端代码优化

#### 问题 1：公共代码零提取（最高优先级）

**现状**：无 `components/` 和 `utils/` 目录。工具函数在多文件中大量重复。

**优化方案**：

```
src/
├── utils/                          # 新建工具函数目录
│   ├── format.js                   # formatTime / formatDateTime / formatMoney
│   ├── constants.js                # 订单状态映射、角色映射等
│   └── storage.js                  # localStorage 安全读写封装
├── components/                     # 新建公共组件目录
│   ├── StatusTag.vue               # 订单状态标签（替代 5 处重复的 getStatusType/Text）
│   ├── EmptyState.vue              # 统一空状态组件
│   ├── PageHeader.vue              # 页面标题 + 面包屑
│   └── SearchBar.vue               # 全局搜索组件
```

**重复函数消除清单**：

| 函数 | 当前重复次数 | 优化后 |
|------|------------|--------|
| `formatTime` / `formatDateTime` | **7 处** | → `utils/format.js` 统一导出 |
| `getStatusType` | **4 处** | → `utils/constants.js` 或 `StatusTag.vue` |
| `getStatusText` | **5 处** | → `utils/constants.js` 或 `StatusTag.vue` |
| `getRoleText` | **2 处** | → `utils/constants.js` |

#### 问题 2：request.js 环境变量修复

```javascript
// ❌ 当前：硬编码（api/request.js:8）
baseURL: '/api'

// ✅ 修复：读取环境变量
baseURL: import.meta.env.VITE_API_BASE_URL || '/api'
```

#### 问题 3：全量拉取订单做客户端统计

```javascript
// ❌ 当前：Profile.vue:278
const res = await getMyOrders({ size: 1000 })
// 然后前端 filter 计数...

// ✅ 优化：后端提供统计 API
// GET /api/orders/stats → { total, pending, completed, cancelled }
const res = await getOrderStats()
```

#### 问题 4：ECharts 按需加载

```javascript
// ❌ 当前：全量引入（约 1MB）
import * as echarts from 'echarts'

// ✅ 优化：按需引入
import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([BarChart, LineChart, PieChart, GridComponent,
             TooltipComponent, LegendComponent, CanvasRenderer])
```

#### 问题 5：localStorage 安全封装

```javascript
// ❌ 当前：stores/user.js 无异常保护
const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')

// ✅ 优化：utils/storage.js
export function getStorageItem(key, defaultValue = null) {
  try {
    const item = localStorage.getItem(key)
    return item ? JSON.parse(item) : defaultValue
  } catch (e) {
    console.warn(`Failed to parse localStorage key "${key}":`, e)
    localStorage.removeItem(key)
    return defaultValue
  }
}
```

---

## 4. 安全性增强方案

### 4.1 JWT Token 黑名单（Redis）

**问题**：用户被禁用或修改密码后，旧 Token 仍有效至过期。

**方案**：基于 Redis 实现 Token 黑名单机制。

```java
@Service
public class TokenBlacklistService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 将 Token 加入黑名单
     * @param token JWT Token
     * @param expiration Token 剩余有效期（毫秒）
     */
    public void blacklist(String token, long expiration) {
        String key = "jwt:blacklist:" + token;
        redisTemplate.opsForValue().set(key, "1", expiration, TimeUnit.MILLISECONDS);
    }

    /**
     * 检查 Token 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("jwt:blacklist:" + token));
    }
}
```

在 `JwtAuthenticationFilter` 中增加黑名单检查：
```java
if (tokenBlacklistService.isBlacklisted(token)) {
    response.setStatus(401);
    return;
}
```

### 4.2 登录暴力破解防护

**方案**：基于 Redis 滑动窗口限流。

```java
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION = 15; // 分钟

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void recordFailedAttempt(String username) {
        String key = "login:failed:" + username;
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts == 1) {
            redisTemplate.expire(key, LOCK_DURATION, TimeUnit.MINUTES);
        }
    }

    public boolean isLocked(String username) {
        String key = "login:failed:" + username;
        String attempts = redisTemplate.opsForValue().get(key);
        return attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPTS;
    }

    public void clearAttempts(String username) {
        redisTemplate.delete("login:failed:" + username);
    }
}
```

### 4.3 接口权限细化

**问题**：`/orders/**` 路径仅要求 `authenticated`，门店管理员理论上可访问消费者订单接口。

**优化方案**：

```java
// SecurityConfig.java 增加细粒度控制
.requestMatchers("/api/orders/**").hasRole("CONSUMER")
.requestMatchers("/api/store/**").hasRole("STORE_ADMIN")
.requestMatchers("/api/admin/**").hasRole("SYSTEM_ADMIN")
```

### 4.4 安全优化清单

| # | 优化项 | 当前状态 | 方案 | 优先级 |
|---|--------|---------|------|--------|
| 1 | Token 黑名单 | ❌ 缺失 | Redis 存储已失效 Token | 高 |
| 2 | 暴力破解防护 | ❌ 缺失 | Redis 登录失败计数 + 锁定 | 高 |
| 3 | Refresh Token | ❌ 缺失 | 双 Token 机制（Access 短期 + Refresh 长期） | 中 |
| 4 | 接口权限细化 | ⚠️ 不够细 | SecurityConfig 按角色限制路径 | 中 |
| 5 | 密码修改功能 | ❌ 空壳 | 后端实现 `/api/auth/change-password` | 中 |
| 6 | 密码强度校验 | ❌ 缺失 | 前后端同时校验（≥8位，含字母+数字） | 低 |

---

## 5. 性能优化方案

### 5.1 数据库索引优化

```sql
-- 订单表复合索引（高频查询场景）
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_orders_store_status ON orders(store_id, status);
CREATE INDEX idx_orders_create_time_status ON orders(create_time, status);
CREATE INDEX idx_orders_pickup_store ON orders(pickup_code, store_id);

-- 门店商品表索引
CREATE INDEX idx_store_sku_store_status ON store_skus(store_id, status);

-- 购物车表索引
CREATE INDEX idx_cart_user_store ON cart_item(user_id, store_id);
```

### 5.2 Redis 缓存策略

| 缓存对象 | Key 格式 | TTL | 失效策略 | 理由 |
|----------|---------|-----|---------|------|
| 商品分类列表 | `cache:categories:all` | 10 min | 分类变更时 `@CacheEvict` | 极少变化，高频读取 |
| 门店列表 | `cache:stores:list` | 5 min | 门店信息变更时清除 | 较少变化，消费者频繁浏览 |
| 门店商品列表 | `cache:store:{storeId}:skus` | 5 min | 库存/价格变更时清除 | 读多写少 |
| 商品详情 | `cache:product:{id}` | 10 min | 商品编辑时清除 | 读多写少 |
| JWT 黑名单 | `jwt:blacklist:{token}` | Token 剩余有效期 | 自动过期 | 安全需求 |

**Spring Cache 配置示例**：

```java
@Service
public class CategoryServiceImpl implements CategoryService {

    @Override
    @Cacheable(value = "categories", key = "'all'")
    public List<Category> listAll() {
        return categoryMapper.selectList(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort)
        );
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public void update(CategoryDTO dto) {
        // 更新分类逻辑...
    }
}
```

### 5.3 前端性能优化

| 优化项 | 当前问题 | 优化方案 | 预期效果 |
|--------|---------|---------|---------|
| ECharts 按需加载 | 全量引入 ~1MB | 按需引入所需图表类型 | 减少 ~600KB |
| 路由懒加载 | ✅ 已实现 | 无需改动 | — |
| 客户端统计改后端 API | `size: 1000` 全量拉取 | 新增 `/api/orders/stats` 接口 | 减少 95% 数据传输 |
| 图片懒加载 | 商品列表图片一次性加载 | 使用 `loading="lazy"` 或 `v-lazy` | 首屏加载提速 |
| 骨架屏 | 仅用 `v-loading` | 使用 `el-skeleton` 组件 | 感知性能提升 |

---

## 6. 新增模块推荐

### 6.1 推荐模块总览

| 优先级 | 模块名称 | 难度 | 答辩加分 | 工作量 |
|--------|---------|------|---------|--------|
| **P0** | Swagger API 文档集成 | ⭐ | ★★★★ | 0.5 天 |
| **P0** | Docker 容器化部署 | ⭐ | ★★★★★ | 1 天 |
| **P1** | 数据可视化大屏 | ⭐⭐⭐ | ★★★★★ | 3-5 天 |
| **P1** | Redis 缓存层 | ⭐⭐ | ★★★★ | 1-2 天 |
| **P2** | WebSocket 实时通知 | ⭐⭐ | ★★★★ | 2-3 天 |
| **P2** | Excel 报表导出 | ⭐⭐ | ★★★ | 1 天 |

### 6.2 Swagger API 文档集成

**技术方案**：SpringDoc OpenAPI 3

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

```yaml
# application.yaml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
```

```java
// Controller 示例
@RestController
@RequestMapping("/api/orders")
@Tag(name = "订单管理", description = "消费者端订单相关接口")
public class OrderController {

    @Operation(summary = "创建订单", description = "从购物车创建订单，自动扣减库存")
    @ApiResponse(responseCode = "200", description = "创建成功")
    @PostMapping
    public Result<Order> create(@Valid @RequestBody CreateOrderDTO dto) {
        // ...
    }
}
```

**产出**：访问 `http://localhost:8080/api/swagger-ui.html` 即可看到完整的交互式 API 文档。

### 6.3 Docker 容器化部署

**后端 Dockerfile**：

```dockerfile
# 多阶段构建
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml**：

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD:-root}
      MYSQL_DATABASE: snack_platform
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./docs/04-DB/schema.sql:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  backend:
    build: ./snack-platform-backend
    ports:
      - "8080:8080"
    environment:
      DB_URL: jdbc:mysql://mysql:3306/snack_platform?useSSL=false&allowPublicKeyRetrieval=true
      DB_USERNAME: root
      DB_PASSWORD: ${DB_PASSWORD:-root}
      REDIS_HOST: redis
    depends_on:
      - mysql
      - redis

  frontend:
    build: ./snack-platform-frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql-data:
```

### 6.4 数据可视化大屏

**功能规划**：

```
┌──────────────────────────────────────────────────────────────┐
│                    全平台运营数据大屏                          │
├──────────┬──────────┬──────────┬──────────────────────────────┤
│ 今日订单  │ 今日营收  │ 活跃门店  │      订单趋势（折线图）     │
│   128    │ ¥12,580  │   3/3    │   ~~~~~~~~~~~~~~~~~~~~~~~~  │
│  ↑12%    │  ↑8.5%   │  100%    │   近 7 天每日订单量+营收     │
├──────────┴──────────┴──────────┤                              │
│                                │                              │
│  门店销售排行（柱状图）          │                              │
│  ████████████ 北京店 ¥5,200    │                              │
│  █████████ 上海店 ¥4,180       ├──────────────────────────────┤
│  ███████ 广州店 ¥3,200         │   品类销售占比（饼图）        │
│                                │      🥜 坚果 32%             │
├────────────────────────────────┤      🍪 饼干 25%             │
│   实时订单流水（滚动列表）       │      🍫 糖果 20%             │
│   10:32 北京店 ¥46.30 已完成   │      🥤 饮料 15%             │
│   10:28 上海店 ¥57.80 待接单   │      🍿 膨化 8%              │
│   10:15 广州店 ¥33.00 已取消   │                              │
└────────────────────────────────┴──────────────────────────────┘
```

**技术实现要点**：
- ECharts 图表组件化封装（LineChart、BarChart、PieChart）
- 后端补充 `/api/admin/stats/realtime` 实时数据接口
- 定时轮询（30s）或 WebSocket 推送刷新数据
- 全屏模式 + 深色主题 + 数字滚动动画

### 6.5 WebSocket 实时通知

**技术方案**：Spring WebSocket + STOMP

**推送场景**：

| 事件 | 发送方 | 接收方 | 消息内容 |
|------|--------|--------|---------|
| 新订单 | 消费者支付成功 | 门店管理员 | "收到新订单 #ORD2025xxx，金额 ¥xx.xx" |
| 门店接单 | 门店管理员接单 | 消费者 | "您的订单已被门店接受，正在备货中" |
| 备货完成 | 门店管理员标记 | 消费者 | "您的订单已备好，请凭自提码 xxxxxx 到店取货" |
| 订单取消 | 门店拒单 | 消费者 | "很抱歉，门店因 xxx 原因无法接受您的订单" |

**答辩演示效果**：双屏同时打开（消费者端 + 门店管理端），消费者付款后门店端实时弹出通知（含提示音），直观展示"多门店协同"核心卖点。

### 6.6 Excel 报表导出

**技术方案**：EasyExcel（阿里开源，流式写入防 OOM）

```java
@GetMapping("/export")
@Operation(summary = "导出销售报表")
public void exportSalesReport(HttpServletResponse response,
                               @RequestParam String startDate,
                               @RequestParam String endDate) {
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", "attachment;filename=sales-report.xlsx");

    List<SalesReportVO> data = reportService.getSalesData(startDate, endDate);
    EasyExcel.write(response.getOutputStream(), SalesReportVO.class)
             .sheet("销售报表")
             .doWrite(data);
}
```

---

## 7. 前端工程化优化

### 7.1 目录结构优化

```
src/
├── api/                    # API 层（已有）
├── assets/                 # 静态资源（建议新增）
│   ├── images/
│   └── styles/
├── components/             # 🆕 公共组件
│   ├── StatusTag.vue       # 订单状态标签
│   ├── EmptyState.vue      # 统一空状态
│   ├── SearchBar.vue       # 全局搜索
│   ├── ImageUpload.vue     # 图片上传
│   └── DataCard.vue        # 统计卡片
├── composables/            # 🆕 组合式函数
│   ├── useLoading.js       # loading 状态
│   ├── usePagination.js    # 分页逻辑
│   └── usePermission.js    # 权限检查
├── utils/                  # 🆕 工具函数
│   ├── format.js           # 格式化（时间/金额）
│   ├── constants.js        # 常量（状态映射）
│   ├── storage.js          # localStorage 安全封装
│   └── validate.js         # 表单校验规则
├── layouts/                # 布局（已有）
│   ├── AdminLayout.vue
│   ├── ConsumerLayout.vue
│   └── StoreLayout.vue     # 🆕 门店独立布局
├── router/                 # 路由（已有）
├── stores/                 # 状态管理（已有）
└── views/                  # 页面（已有）
```

### 7.2 响应式设计待补齐页面

| 页面 | 当前移动端适配 | 优化工作量 |
|------|--------------|-----------|
| consumer/OrderDetail | ⭐ 无适配 | 0.5 天 |
| consumer/Profile | ⭐ 无适配 | 0.5 天 |
| admin/Dashboard | ⭐ 无适配 | 0.5 天 |
| store/Dashboard | ⭐ 无适配 | 0.5 天 |
| admin 列表页（5个） | ⭐ 无适配 | 1 天 |

### 7.3 错误处理统一化

建议实现统一的错误边界组件和请求 loading 管理：

```javascript
// composables/useLoading.js
export function useLoading() {
  const loading = ref(false)
  const error = ref(null)

  async function execute(fn) {
    loading.value = true
    error.value = null
    try {
      return await fn()
    } catch (e) {
      error.value = e.message || '操作失败'
      ElMessage.error(error.value)
      throw e
    } finally {
      loading.value = false
    }
  }

  return { loading, error, execute }
}
```

---

## 8. 执行路线图

### 8.1 阶段规划

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Phase 1: 紧急修复（2 天）                    │
│                                                                     │
│  Day 1  ├── 购物车 CartController + CartService                     │
│         ├── 修改密码 API 实现 + 前端取消注释                          │
│         └── request.js baseURL 修复 + 死路由修复                     │
│                                                                     │
│  Day 2  ├── Dashboard 图表接入真实 Report API 数据                   │
│         ├── Swagger 文档集成（springdoc-openapi）                    │
│         └── 前端 utils/ + components/ 提取                           │
├─────────────────────────────────────────────────────────────────────┤
│                      Phase 2: 工程化增强（2 天）                      │
│                                                                     │
│  Day 3  ├── Docker 容器化（Dockerfile + docker-compose）             │
│         └── 数据库索引优化 + N+1 查询修复                             │
│                                                                     │
│  Day 4  ├── Redis 缓存层（商品/分类/门店 + JWT 黑名单）               │
│         └── 登录暴力破解防护                                          │
├─────────────────────────────────────────────────────────────────────┤
│                      Phase 3: 亮点功能（5-7 天）                      │
│                                                                     │
│  Day 5-8  ├── 数据可视化大屏（4-5 个 ECharts 图表）                  │
│           └── 后端补充门店对比 API + 商品销量排行 API                  │
│                                                                     │
│  Day 9-11 ├── WebSocket 实时订单通知                                 │
│           └── 前端通知组件 + 提示音                                   │
├─────────────────────────────────────────────────────────────────────┤
│                      Phase 4: 收尾打磨（2-3 天）                      │
│                                                                     │
│  Day 12  ├── Excel 报表导出（EasyExcel）                             │
│          └── 文件上传服务（本地存储）                                  │
│                                                                     │
│  Day 13  ├── 响应式适配（OrderDetail / Profile / Dashboard）          │
│          └── ECharts 按需加载 + 图片懒加载                            │
│                                                                     │
│  Day 14  ├── JaCoCo 测试覆盖率配置 + 补充 Controller 测试            │
│          └── 全量回归测试 + Bug 修复                                  │
└─────────────────────────────────────────────────────────────────────┘
```

### 8.2 预期成果

| 指标 | 优化前 | 优化后 |
|------|--------|--------|
| MVP 功能完成度 | ~85% | **≥ 95%** |
| User Story 完成率 | 67%（18/27） | **≥ 85%（23/27）** |
| AC 验收通过率 | ~70% | **≥ 85%** |
| 后端代码质量评分 | 7/10 | **8.5/10** |
| 前端代码质量评分 | 6.5/10 | **8/10** |
| 安全性评分 | 6.5/10 | **8/10** |
| 答辩技术亮点 | 3 项 | **8+ 项** |

### 8.3 答辩可展示的技术亮点

完成全部优化后，可在答辩中展示：

1. **Spring Boot + Vue.js 前后端分离架构**
2. **Spring Security + JWT 认证鉴权（含 Token 黑名单）**
3. **Redis 缓存 + 性能优化（含对比数据）**
4. **WebSocket 实时通知（双屏演示）**
5. **ECharts 数据可视化大屏（全屏演示）**
6. **Docker 容器化一键部署**
7. **Swagger 交互式 API 文档**
8. **MyBatis-Plus 乐观锁防超卖**
9. **AOP 操作日志切面**
10. **EasyExcel 报表导出**

---

## 附录 A：关键文件问题索引

| 文件路径 | 问题 | 行号 |
|----------|------|------|
| `backend/.../OrderServiceImpl.java` | N+1 查询（fillOrderDetails） | — |
| `backend/.../JwtAuthenticationFilter.java` | JWT 重复解析 4 次 | — |
| `backend/.../ReportServiceImpl.java` | 全量加载内存聚合 OOM 风险 | — |
| `backend/.../SecurityConfig.java` | `/orders/**` 角色控制不够细 | — |
| `frontend/src/api/request.js` | baseURL 硬编码不读环境变量 | L8 |
| `frontend/src/views/consumer/Profile.vue` | 修改密码 API 被注释 | L264-266 |
| `frontend/src/views/consumer/Profile.vue` | 全量拉取订单做客户端统计 | L278 |
| `frontend/src/views/admin/Dashboard.vue` | 销售趋势 Math.random() mock | L323 |
| `frontend/src/views/admin/Dashboard.vue` | 分类饼图硬编码数据 | L450-457 |
| `frontend/src/views/admin/Dashboard.vue` | goToReport 跳转死路由 | L294 |
| `frontend/src/views/store/Dashboard.vue` | 订单状态饼图硬编码 mock | L331-337 |
| `frontend/src/views/store/Dashboard.vue` | 热销商品硬编码 mock | L391-398 |
| `frontend/src/stores/user.js` | localStorage 无 try-catch 保护 | L8 |

---

## 附录 B：文档修订记录

| 版本 | 日期 | 修订内容 | 修订人 |
|------|------|---------|--------|
| v1.0 | 2026-02-15 | 初始版本：三维并行分析报告整合 | AI Analysis Team |

---

> **文档说明**：本文档基于后端代码审计、前端代码审计、PRD 差距对比三维并行深度分析的结论整合而成。建议开发团队按照执行路线图的阶段顺序推进优化，确保答辩前完成 Phase 1 和 Phase 2。
