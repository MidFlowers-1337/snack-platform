# 🍿 Snack Platform Backend

连锁零食电商平台后端服务

> 📚 **项目文档**：[文档中心](../docs/README.md) | [快速开始](../docs/QUICKSTART.md) | [API 文档](../docs/03-API/openapi.md) | [数据库设计](../docs/04-DB/schema.md)

## 📋 技术栈

- **框架**: Spring Boot 3.5.8
- **安全**: Spring Security 6.x + JWT
- **ORM**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0
- **构建**: Maven

## 🚀 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Maven 3.8+

### 配置

1. **复制环境变量模板**

```bash
cp .env.example .env
```

2. **编辑 `.env` 文件**

```bash
# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/snack_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=your_password

# JWT 配置
JWT_SECRET=your-super-secure-jwt-secret-key-at-least-256-bits-long-for-hs256
JWT_EXPIRATION=86400000

# 服务器配置
SERVER_PORT=8080

# CORS 配置
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

3. **创建数据库**

```sql
CREATE DATABASE snack_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 启动

```bash
# 开发模式
./mvnw spring-boot:run

# 或者先构建再运行
./mvnw clean package -DskipTests
java -jar target/snack-platform-backend-0.0.1-SNAPSHOT.jar
```

服务将在 `http://localhost:8080` 启动，API 基础路径为 `/api`

## 📁 项目结构

```
src/main/java/com/snackchain/snack_platform_backend/
├── SnackPlatformBackendApplication.java  # 启动类
├── common/                               # 公共模块
│   ├── config/                           # 配置类
│   │   ├── MyBatisPlusConfig.java        # MyBatis-Plus 配置
│   │   └── SecurityConfig.java           # Spring Security 配置
│   ├── exception/                        # 异常处理
│   │   ├── BusinessException.java        # 业务异常
│   │   └── GlobalExceptionHandler.java   # 全局异常处理器
│   ├── result/                           # 统一响应
│   │   ├── Result.java                   # 响应封装
│   │   └── ResultCode.java               # 响应码枚举
│   └── util/                             # 工具类
│       └── JwtUtil.java                  # JWT 工具
├── entity/                               # 实体类
│   ├── Category.java                     # 分类
│   ├── Order.java                        # 订单
│   ├── OrderItem.java                    # 订单项
│   ├── Product.java                      # 商品
│   ├── Store.java                        # 门店
│   ├── StoreSku.java                     # 门店库存
│   └── User.java                         # 用户
├── enums/                                # 枚举类
│   ├── OrderStatus.java                  # 订单状态
│   └── UserRole.java                     # 用户角色
├── mapper/                               # MyBatis Mapper
├── module/                               # 业务模块
│   ├── auth/                             # 认证模块
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── service/
│   │   └── vo/
│   ├── category/                         # 分类模块
│   ├── order/                            # 订单模块
│   ├── product/                          # 商品模块
│   ├── report/                           # 报表模块
│   ├── sku/                              # 库存模块
│   └── store/                            # 门店模块
└── security/                             # 安全模块
    ├── annotation/                       # 自定义注解
    │   └── RequireRole.java              # 角色校验注解
    ├── context/                          # 用户上下文
    │   ├── UserContext.java
    │   └── UserContextHolder.java
    └── filter/                           # 过滤器
        └── JwtAuthenticationFilter.java  # JWT 认证过滤器
```

## 🔐 安全配置

### 角色权限

| 角色 | 说明 | 权限 |
|------|------|------|
| CONSUMER | 消费者 | 浏览商品、下单、查看订单 |
| STORE_ADMIN | 门店管理员 | 管理库存、处理订单、核销 |
| SYSTEM_ADMIN | 系统管理员 | 管理商品、门店、查看报表 |

### JWT 认证

- Token 有效期：24 小时（可配置）
- Token 格式：`Bearer <token>`
- 请求头：`Authorization`

### 密码加密

使用 BCrypt 算法加密存储密码

## 📚 API 接口

### 公开接口（无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |
| GET | `/api/stores` | 获取门店列表 |
| GET | `/api/stores/{id}` | 获取门店详情 |
| GET | `/api/categories` | 获取分类列表 |

### 消费者接口（需要 CONSUMER 角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/auth/me` | 获取当前用户信息 |
| PUT | `/api/auth/profile` | 更新用户信息 |
| GET | `/api/stores/{storeId}/skus` | 获取门店商品 |
| POST | `/api/orders` | 创建订单 |
| GET | `/api/orders` | 获取我的订单 |
| GET | `/api/orders/{id}` | 获取订单详情 |
| POST | `/api/orders/{id}/cancel` | 取消订单 |

### 门店管理员接口（需要 STORE_ADMIN 角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/store-admin/skus` | 获取门店库存 |
| PUT | `/api/store-admin/skus/{id}` | 更新库存 |
| GET | `/api/store-admin/orders` | 获取门店订单 |
| POST | `/api/store-admin/orders/{id}/confirm` | 确认订单 |
| POST | `/api/store-admin/orders/{id}/reject` | 拒绝订单 |
| POST | `/api/store-admin/orders/{id}/ready` | 备货完成 |
| POST | `/api/store-admin/orders/verify` | 核销订单 |
| GET | `/api/store-admin/reports/dashboard` | 门店仪表盘 |

### 系统管理员接口（需要 SYSTEM_ADMIN 角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/categories` | 获取分类列表 |
| POST | `/api/admin/categories` | 创建分类 |
| PUT | `/api/admin/categories/{id}` | 更新分类 |
| DELETE | `/api/admin/categories/{id}` | 删除分类 |
| GET | `/api/admin/products` | 获取商品列表 |
| POST | `/api/admin/products` | 创建商品 |
| PUT | `/api/admin/products/{id}` | 更新商品 |
| DELETE | `/api/admin/products/{id}` | 删除商品 |
| GET | `/api/admin/stores` | 获取门店列表 |
| POST | `/api/admin/stores` | 创建门店 |
| PUT | `/api/admin/stores/{id}` | 更新门店 |
| DELETE | `/api/admin/stores/{id}` | 删除门店 |
| GET | `/api/admin/reports/dashboard` | 系统仪表盘 |
| GET | `/api/admin/reports/sales` | 销售报表 |

## 🗄️ 数据库设计

### 核心表

| 表名 | 说明 |
|------|------|
| `user` | 用户表 |
| `store` | 门店表 |
| `category` | 分类表 |
| `product` | 商品表 |
| `store_sku` | 门店库存表 |
| `order` | 订单表 |
| `order_item` | 订单项表 |

### 订单状态流转

```
PENDING_PAYMENT → PENDING_CONFIRM → CONFIRMED → READY → COMPLETED
       ↓                ↓              ↓
    CANCELLED       REJECTED       CANCELLED
```

## 🔧 配置说明

### application.yaml

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:86400000}

server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /api

cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS}
```

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DB_URL` | 数据库连接 URL | - |
| `DB_USERNAME` | 数据库用户名 | root |
| `DB_PASSWORD` | 数据库密码 | root |
| `JWT_SECRET` | JWT 密钥 | - |
| `JWT_EXPIRATION` | JWT 过期时间（毫秒） | 86400000 |
| `SERVER_PORT` | 服务端口 | 8080 |
| `CORS_ALLOWED_ORIGINS` | CORS 允许的源 | localhost |
| `LOG_LEVEL` | 日志级别 | info |

## 🧪 测试

```bash
# 运行所有测试
./mvnw test

# 运行特定测试类
./mvnw test -Dtest=AuthServiceTest
```

## 📦 构建

```bash
# 构建 JAR 包
./mvnw clean package -DskipTests

# 构建并运行
./mvnw clean package -DskipTests && java -jar target/snack-platform-backend-0.0.1-SNAPSHOT.jar
```

## 🐳 Docker

```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/snack-platform-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# 构建镜像
docker build -t snack-platform-backend .

# 运行容器
docker run -d -p 8080:8080 \
  -e DB_URL=jdbc:mysql://host:3306/snack_platform \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=password \
  -e JWT_SECRET=your-secret \
  snack-platform-backend
```

## 📝 开发规范

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化代码
- 统一使用 `Result<T>` 封装响应

### 异常处理

- 业务异常使用 `BusinessException`
- 全局异常由 `GlobalExceptionHandler` 处理

### 日志规范

- 使用 SLF4J 日志框架
- 生产环境使用 INFO 级别
- 敏感信息不记录日志

## 📄 许可证

MIT License

---

📚 返回 [项目主页](../README.md) | [文档中心](../docs/README.md)