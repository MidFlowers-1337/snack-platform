# 🍿 连锁零食电商平台 (Snack Platform)

面向多门店协同的连锁零食电商平台 - 毕业设计项目

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.5-4FC08D.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [API 文档](#api-文档)
- [部署指南](#部署指南)
- [开发指南](#开发指南)
- [常见问题](#常见问题)

## 📖 项目简介

本项目是一个面向连锁零食店的电商平台，支持多门店协同运营。系统包含三种用户角色：

- **消费者 (CONSUMER)**: 浏览商品、下单购买、到店自提
- **门店管理员 (STORE_ADMIN)**: 管理门店库存、处理订单、核销自提
- **系统管理员 (SYSTEM_ADMIN)**: 管理商品、门店、用户、查看统计报表

## ✨ 功能特性

### 消费者端
- 🏪 门店选择（支持附近门店排序）
- 🛒 商品浏览与购物车
- 📦 在线下单与订单管理
- 📱 自提码核销

### 门店管理端
- 📊 门店数据仪表盘
- 📦 库存管理（SKU 管理）
- 📋 订单处理（接单/拒单/备货完成）
- ✅ 自提核销

### 系统管理端
- 📊 全平台数据统计
- 🏷️ 商品分类管理
- 🍿 商品信息管理
- 🏪 门店管理

## 🛠️ 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.8 | 基础框架 |
| Spring Security | 6.x | 安全框架 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| MySQL | 8.0 | 数据库 |
| JWT | 0.12.3 | 身份认证 |
| Lombok | - | 代码简化 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue.js | 3.5 | 前端框架 |
| Element Plus | 2.12 | UI 组件库 |
| Vue Router | 4.6 | 路由管理 |
| Pinia | 3.0 | 状态管理 |
| Axios | 1.13 | HTTP 客户端 |
| Vite | 7.2 | 构建工具 |

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端 (Vue.js 3)                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  消费者端   │  │ 门店管理端  │  │    系统管理端       │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   后端 (Spring Boot 3)                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │   Auth   │  │  Store   │  │  Order   │  │  Report  │    │
│  │  Module  │  │  Module  │  │  Module  │  │  Module  │    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                  │
│  │ Product  │  │   SKU    │  │ Category │                  │
│  │  Module  │  │  Module  │  │  Module  │                  │
│  └──────────┘  └──────────┘  └──────────┘                  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      MySQL 8.0 数据库                        │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 快速开始

### 环境要求

- **JDK**: 17+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Maven**: 3.8+

### 1. 克隆项目

```bash
git clone https://github.com/MidFlowers-1337/snack-platform.git
cd snack-platform
```

### 2. 数据库配置

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE snack_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据库脚本（如果有）
USE snack_platform;
SOURCE docs/schema.sql;
```

### 3. 后端配置与启动

```bash
cd snack-platform-backend

# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，配置数据库连接信息
# DB_USERNAME=your_username
# DB_PASSWORD=your_password

# 启动后端服务
./mvnw spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动，API 基础路径为 `/api`

### 4. 前端配置与启动

```bash
cd snack-platform-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 `http://localhost:3000` 启动

### 5. 访问系统

打开浏览器访问 `http://localhost:3000`

**测试账号**:
| 角色 | 用户名 | 密码 |
|------|--------|------|
| 系统管理员 | admin | 123456 |
| 门店管理员 | store1 | 123456 |
| 消费者 | user1 | 123456 |

## 📁 项目结构

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
│   │       ├── entity/              # 实体类
│   │       ├── enums/               # 枚举类
│   │       ├── mapper/              # MyBatis Mapper
│   │       ├── module/              # 业务模块
│   │       │   ├── auth/            # 认证模块
│   │       │   ├── category/        # 分类模块
│   │       │   ├── order/           # 订单模块
│   │       │   ├── product/         # 商品模块
│   │       │   ├── report/          # 报表模块
│   │       │   ├── sku/             # 库存模块
│   │       │   └── store/           # 门店模块
│   │       └── security/            # 安全模块
│   └── src/main/resources/
│       └── application.yaml         # 配置文件
│
├── snack-platform-frontend/         # 前端项目
│   ├── src/
│   │   ├── api/                     # API 接口
│   │   ├── components/              # 公共组件
│   │   ├── layouts/                 # 布局组件
│   │   ├── router/                  # 路由配置
│   │   ├── stores/                  # Pinia 状态
│   │   └── views/                   # 页面组件
│   │       ├── admin/               # 系统管理端
│   │       ├── consumer/            # 消费者端
│   │       └── store/               # 门店管理端
│   └── vite.config.js               # Vite 配置
│
└── docs/                            # 项目文档
```

## 📚 API 文档

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |
| GET | `/api/auth/me` | 获取当前用户信息 |
| PUT | `/api/auth/profile` | 更新用户信息 |

### 门店接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/stores` | 获取门店列表 |
| GET | `/api/stores/{id}` | 获取门店详情 |
| GET | `/api/stores/nearby` | 获取附近门店 |

### 商品接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/products` | 获取商品列表 |
| POST | `/api/admin/products` | 创建商品 |
| PUT | `/api/admin/products/{id}` | 更新商品 |
| DELETE | `/api/admin/products/{id}` | 删除商品 |

### 订单接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/orders` | 创建订单 |
| GET | `/api/orders` | 获取订单列表 |
| GET | `/api/orders/{id}` | 获取订单详情 |
| POST | `/api/orders/{id}/cancel` | 取消订单 |
| POST | `/api/orders/{id}/verify` | 核销订单 |

### 库存接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/stores/{storeId}/skus` | 获取门店商品列表 |
| PUT | `/api/store-admin/skus/{id}` | 更新库存 |

完整 API 文档请参考 [`docs/openapi.md`](docs/openapi.md)

## 🚢 部署指南

### 生产环境配置

#### 1. 环境变量配置

创建 `.env` 文件或设置系统环境变量：

```bash
# 数据库配置
DB_URL=jdbc:mysql://your-db-host:3306/snack_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=true
DB_USERNAME=your_db_username
DB_PASSWORD=your_secure_password

# JWT 配置 - 必须使用安全的密钥
JWT_SECRET=your-super-secure-jwt-secret-key-at-least-256-bits-long
JWT_EXPIRATION=86400000

# 服务器配置
SERVER_PORT=8080

# 日志级别
LOG_LEVEL=info
SECURITY_LOG_LEVEL=warn

# CORS 配置 - 设置为实际前端域名
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
```

#### 2. 构建后端

```bash
cd snack-platform-backend
./mvnw clean package -DskipTests
```

生成的 JAR 文件位于 `target/snack-platform-backend-0.0.1-SNAPSHOT.jar`

#### 3. 构建前端

```bash
cd snack-platform-frontend
npm run build
```

生成的静态文件位于 `dist/` 目录

#### 4. 运行后端

```bash
java -jar target/snack-platform-backend-0.0.1-SNAPSHOT.jar
```

#### 5. 部署前端

将 `dist/` 目录部署到 Nginx 或其他 Web 服务器

**Nginx 配置示例**:

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /var/www/snack-platform/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### Docker 部署（可选）

```dockerfile
# Dockerfile for backend
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/snack-platform-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  backend:
    build: ./snack-platform-backend
    ports:
      - "8080:8080"
    environment:
      - DB_URL=jdbc:mysql://db:3306/snack_platform
      - DB_USERNAME=root
      - DB_PASSWORD=your_password
      - JWT_SECRET=your_jwt_secret
    depends_on:
      - db

  db:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=your_password
      - MYSQL_DATABASE=snack_platform
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

## 💻 开发指南

### 代码规范

- **后端**: 遵循阿里巴巴 Java 开发手册
- **前端**: 遵循 Vue.js 官方风格指南

### 分支管理

- `main`: 主分支，用于生产环境
- `dev`: 开发分支，用于开发环境
- `feature/*`: 功能分支
- `fix/*`: 修复分支

### 提交规范

```
feat: 新功能
fix: 修复 bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建/工具相关
```

### 本地开发

1. 后端热重载已启用（spring-boot-devtools）
2. 前端使用 Vite 热更新
3. API 代理配置在 `vite.config.js`

## ❓ 常见问题

### Q: 后端启动失败，提示数据库连接错误？

A: 请检查：
1. MySQL 服务是否启动
2. 数据库 `snack_platform` 是否已创建
3. `.env` 文件中的数据库配置是否正确

### Q: 前端启动后无法访问 API？

A: 请检查：
1. 后端服务是否正常运行在 8080 端口
2. `vite.config.js` 中的代理配置是否正确

### Q: 登录后提示 Token 无效？

A: 请检查：
1. JWT 密钥配置是否正确
2. Token 是否过期（默认 24 小时）

### Q: 如何重置测试数据？

A: 重新执行数据库初始化脚本：
```bash
mysql -u root -p snack_platform < docs/schema.sql
```

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 👥 作者

- **开发者**: MidFlowers-1337

## 🙏 致谢

感谢以下开源项目：
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [MyBatis-Plus](https://baomidou.com/)