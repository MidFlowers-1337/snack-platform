# 🍿 Snack Platform

> 面向多门店协同的连锁零食电商平台

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.5-brightgreen.svg)](https://vuejs.org/)

---

## 📖 项目简介

Snack Platform 是一个面向连锁零食门店的电商平台，支持多门店独立运营、统一商品管理、订单履约和自提核销等核心业务场景。

### ✨ 核心特性

- 🏪 **多门店管理** - 支持多门店独立运营，各门店独立定价和库存
- 📦 **商品管理** - 平台统一管理商品（SPU），门店独立上架（SKU）
- 🛒 **订单履约** - 完整的下单、支付、接单、备货、自提流程
- 🎫 **自提核销** - 支持扫码和手动输入自提码核销
- 👥 **权限隔离** - 消费者、门店管理员、系统管理员三级权限
- 📊 **数据统计** - 门店和平台级别的销售数据统计

### 🎯 适用场景

- 连锁零食店
- 社区团购
- 便利店联盟
- 其他需要多门店协同的零售场景

---

## 🛠️ 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **后端** | Spring Boot | 3.5.8 |
| | Spring Security + JWT | 6.x |
| | MyBatis-Plus | 3.5.5 |
| | MySQL | 8.0 |
| **前端** | Vue.js | 3.5 |
| | Element Plus | 2.12 |
| | Pinia | 3.0 |
| | Vite | 7.2 |

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+

### 一键启动

```bash
# 1. 克隆项目
git clone https://github.com/your-org/snack-platform.git
cd snack-platform

# 2. 创建数据库
mysql -u root -p -e "CREATE DATABASE snack_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p snack_platform < docs/04-DB/schema.sql

# 3. 启动后端
cd snack-platform-backend
cp .env.example .env
# 编辑 .env 配置数据库连接
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

> 📚 详细安装指南请参考 [快速开始文档](docs/QUICKSTART.md)

---

## 📁 项目结构

```
snack-platform/
├── snack-platform-backend/     # 后端服务 (Spring Boot)
│   ├── src/main/java/          # Java 源码
│   ├── src/main/resources/     # 配置文件
│   └── src/test/               # 测试代码
├── snack-platform-frontend/    # 前端应用 (Vue.js)
│   ├── src/api/                # API 接口
│   ├── src/views/              # 页面组件
│   ├── src/stores/             # 状态管理
│   └── src/router/             # 路由配置
├── docs/                       # 项目文档
│   ├── 01-Requirements/        # 需求文档
│   ├── 02-Architecture/        # 架构文档
│   ├── 03-API/                 # API 文档
│   ├── 04-DB/                  # 数据库文档
│   ├── 05-Test/                # 测试文档
│   └── 06-Plan/                # 项目计划
└── README.md                   # 项目说明
```

---

## 📚 文档导航

| 文档 | 说明 |
|------|------|
| [📖 文档中心](docs/README.md) | 所有文档的索引入口 |
| [🚀 快速开始](docs/QUICKSTART.md) | 开发环境搭建指南 |
| [🤝 贡献指南](docs/CONTRIBUTING.md) | 如何参与项目开发 |
| [📋 产品需求](docs/01-Requirements/PRD.md) | 产品背景和功能规划 |
| [🏗️ 系统架构](docs/02-Architecture/Architecture.md) | 技术方案和设计 |
| [🔌 API 文档](docs/03-API/openapi.md) | REST API 接口定义 |
| [🗄️ 数据库设计](docs/04-DB/schema.md) | 表结构和关系设计 |
| [🧪 测试计划](docs/05-Test/TestPlan.md) | 测试策略和用例 |

---

## 🔐 角色权限

| 角色 | 说明 | 主要功能 |
|------|------|----------|
| **消费者** | 普通用户 | 浏览商品、下单购买、查看订单、自提取货 |
| **门店管理员** | 门店运营人员 | 库存管理、订单处理、自提核销、门店统计 |
| **系统管理员** | 平台运营人员 | 商品管理、门店管理、用户管理、平台统计 |

---

## 📊 业务流程

### 订单状态流转

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  待支付     │────▶│  待接单     │────▶│  已接单     │
│ PENDING_PAY │     │PENDING_ACCEPT│    │  ACCEPTED   │
└─────────────┘     └─────────────┘     └─────────────┘
       │                   │                   │
       ▼                   ▼                   ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  已取消     │     │  已取消     │     │  待自提     │
│ CANCELLED   │     │ CANCELLED   │     │READY_PICKUP │
└─────────────┘     └─────────────┘     └─────────────┘
                                               │
                                               ▼
                                        ┌─────────────┐
                                        │  已完成     │
                                        │ COMPLETED   │
                                        └─────────────┘
```

---

## 🧪 测试

```bash
# 后端测试
cd snack-platform-backend
./mvnw test

# 前端测试（如有）
cd snack-platform-frontend
npm run test
```

---

## 🚢 部署

### Docker 部署

```bash
# 构建后端镜像
cd snack-platform-backend
docker build -t snack-platform-backend .

# 构建前端镜像
cd snack-platform-frontend
docker build -t snack-platform-frontend .

# 使用 docker-compose 启动
docker-compose up -d
```

> 📚 详细部署指南请参考各子项目的 README 文件

---

## 🤝 参与贡献

我们欢迎任何形式的贡献！

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: add some feature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

> 📚 详细贡献指南请参考 [CONTRIBUTING.md](docs/CONTRIBUTING.md)

---

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

---

## 📞 联系我们

- 提交 Issue：[GitHub Issues](https://github.com/your-org/snack-platform/issues)
- 项目文档：[文档中心](docs/README.md)

---

<p align="center">
  Made with ❤️ by Snack Platform Team
</p>