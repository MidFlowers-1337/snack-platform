# Snack Platform

> 面向多门店协同的连锁零食电商平台

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.5-brightgreen.svg)](https://vuejs.org/)

---

## 项目简介

Snack Platform 是一个面向连锁零食门店的电商平台，支持多门店独立运营、统一商品管理、订单履约和自提核销等核心业务场景。

### 核心特性

- **多门店管理** - 支持多门店独立运营，各门店独立定价和库存
- **商品管理** - 平台统一管理商品（SPU），门店独立上架（SKU）
- **订单履约** - 完整的下单、支付、接单、备货、自提流程
- **自提核销** - 支持扫码和手动输入自提码核销
- **权限隔离** - 消费者、门店管理员、系统管理员三级权限
- **数据统计** - 门店和平台级别的销售数据统计与实时看板
- **促销活动** - 限时折扣、满减优惠等营销工具
- **会员积分** - 下单积累积分，积分兑换优惠
- **购物车** - 服务端持久化购物车，跨设备同步

### 适用场景

- 连锁零食店
- 社区团购
- 便利店联盟
- 其他需要多门店协同的零售场景

---

## 技术栈

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **后端** | Spring Boot | 3.5.8 | 应用框架 |
| | Spring Security + JWT | 6.x | 认证授权 |
| | MyBatis-Plus | 3.5.5 | ORM 框架 |
| | MySQL | 8.0 | 关系型数据库 |
| | Redis | 7.x | 缓存与会话 |
| **前端** | Vue.js | 3.5 | 渐进式框架 |
| | Element Plus | 2.12 | UI 组件库 |
| | Pinia | 3.0 | 状态管理 |
| | Vite | 7.2 | 构建工具 |
| **部署** | Docker + Docker Compose | - | 容器化部署 |
| | Nginx | - | 前端静态服务与反向代理 |

---

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 7.x（可选，用于缓存加速）

### 方式一：Docker Compose 部署（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/your-org/snack-platform.git
cd snack-platform

# 2. 复制环境变量
cp .env.example .env
# 编辑 .env 配置数据库密码等

# 3. 一键启动
docker-compose up -d
```

### 方式二：本地开发

```bash
# 1. 克隆项目
git clone https://github.com/your-org/snack-platform.git
cd snack-platform

# 2. 创建数据库
mysql -u root -p -e "CREATE DATABASE snack_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p snack_platform < docs/04-DB/schema.sql

# 3. 启动后端
cd snack-platform-backend
./mvnw spring-boot:run

# 4. 启动前端（新终端）
cd snack-platform-frontend
npm install
npm run dev
```

### 访问应用

- 前端应用：http://localhost:3000
- 后端 API：http://localhost:8080/api

### 测试账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | admin123 | 系统管理员 |
| store1 | store123 | 门店管理员 |
| consumer | consumer123 | 消费者 |

---

## 项目结构

```
snack-platform/
├── snack-platform-backend/     # 后端服务 (Spring Boot)
│   ├── src/main/java/
│   │   └── com/snackchain/snack_platform_backend/
│   │       ├── common/         # 公共模块（配置、异常、工具）
│   │       ├── entity/         # 实体类
│   │       ├── mapper/         # 数据访问层
│   │       ├── module/         # 业务模块
│   │       │   ├── auth/       # 认证与用户管理
│   │       │   ├── cart/       # 购物车
│   │       │   ├── category/   # 分类管理
│   │       │   ├── file/       # 文件上传
│   │       │   ├── log/        # 操作日志
│   │       │   ├── order/      # 订单管理
│   │       │   ├── points/     # 积分系统
│   │       │   ├── product/    # 商品管理
│   │       │   ├── promotion/  # 促销活动
│   │       │   ├── report/     # 数据报表
│   │       │   ├── sku/        # 门店库存
│   │       │   └── store/      # 门店管理
│   │       └── security/       # 安全模块（JWT 过滤器）
│   └── src/main/resources/     # 配置文件
├── snack-platform-frontend/    # 前端应用 (Vue.js)
│   ├── src/
│   │   ├── api/                # API 接口层
│   │   ├── components/         # 公共组件
│   │   ├── composables/        # 组合式函数
│   │   ├── layouts/            # 布局组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # Pinia 状态管理
│   │   ├── utils/              # 工具函数
│   │   └── views/              # 页面组件
│   │       ├── admin/          # 系统管理端
│   │       ├── consumer/       # 消费者端
│   │       └── store/          # 门店管理端
│   └── nginx.conf              # Nginx 部署配置
├── docs/                       # 项目文档
│   ├── 01-Requirements/        # 需求文档
│   ├── 02-Architecture/        # 架构文档
│   ├── 03-API/                 # API 文档
│   ├── 04-DB/                  # 数据库文档与脚本
│   ├── 05-Test/                # 测试文档
│   ├── 06-Plan/                # 项目计划
│   └── 10-Optimization/        # 优化方案
├── docker-compose.yml          # Docker Compose 编排
├── .env.example                # 环境变量模板
└── README.md                   # 项目说明
```

---

## 文档导航

| 文档 | 说明 |
|------|------|
| [文档中心](docs/README.md) | 所有文档的索引入口 |
| [快速开始](docs/QUICKSTART.md) | 开发环境搭建指南 |
| [贡献指南](docs/CONTRIBUTING.md) | 如何参与项目开发 |
| [产品需求](docs/01-Requirements/PRD.md) | 产品背景和功能规划 |
| [系统架构](docs/02-Architecture/Architecture.md) | 技术方案和设计 |
| [API 文档](docs/03-API/openapi.md) | REST API 接口定义 |
| [数据库设计](docs/04-DB/schema.md) | 表结构和关系设计 |
| [测试计划](docs/05-Test/TestPlan.md) | 测试策略和用例 |
| [优化方案](docs/10-Optimization/OptimizationPlan.md) | 后端增强与前端重构方案 |

---

## 角色权限

| 角色 | 说明 | 主要功能 |
|------|------|----------|
| **消费者** | 普通用户 | 浏览商品、下单购买、查看订单、自提取货、积分兑换 |
| **门店管理员** | 门店运营人员 | 库存管理、订单处理、自提核销、门店统计、促销管理 |
| **系统管理员** | 平台运营人员 | 商品管理、门店管理、用户管理、平台统计、数据看板 |

---

## 业务流程

### 订单状态流转

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  待支付     │────>│  待接单     │────>│  已接单     │
│ PENDING_PAY │     │PENDING_ACCEPT│    │  ACCEPTED   │
└─────────────┘     └─────────────┘     └─────────────┘
       │                   │                   │
       v                   v                   v
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  已取消     │     │  已拒绝     │     │  待自提     │
│ CANCELLED   │     │ REJECTED    │     │READY_PICKUP │
└─────────────┘     └─────────────┘     └─────────────┘
                                               │
                                               v
                                        ┌─────────────┐
                                        │  已完成     │
                                        │ COMPLETED   │
                                        └─────────────┘
```

---

## 测试

```bash
# 后端测试
cd snack-platform-backend
./mvnw test

# 前端测试
cd snack-platform-frontend
npm run test
```

---

## 部署

### Docker Compose（推荐）

```bash
cp .env.example .env
# 编辑 .env 配置
docker-compose up -d
```

服务启动后：
- 前端：http://localhost:3000
- 后端 API：http://localhost:8080/api
- MySQL：localhost:3306
- Redis：localhost:6379

### 手动部署

```bash
# 后端
cd snack-platform-backend
./mvnw clean package -DskipTests
java -jar target/snack-platform-backend-*.jar

# 前端
cd snack-platform-frontend
npm run build
# 将 dist/ 部署到 Nginx
```

---

## 参与贡献

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: add some feature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

---

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情
