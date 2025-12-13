# 🚀 快速开始指南

> 本指南帮助你在 10 分钟内搭建好 Snack Platform 开发环境

---

## 📋 目录

1. [环境要求](#环境要求)
2. [获取代码](#获取代码)
3. [数据库配置](#数据库配置)
4. [启动后端服务](#启动后端服务)
5. [启动前端服务](#启动前端服务)
6. [验证安装](#验证安装)
7. [测试账号](#测试账号)
8. [常见问题](#常见问题)

---

## 环境要求

### 必需软件

| 软件 | 版本要求 | 下载地址 |
|------|----------|----------|
| JDK | 17+ | [Eclipse Temurin](https://adoptium.net/) |
| Node.js | 18+ | [Node.js 官网](https://nodejs.org/) |
| MySQL | 8.0+ | [MySQL 官网](https://dev.mysql.com/downloads/) |
| Git | 最新版 | [Git 官网](https://git-scm.com/) |

### 可选软件

| 软件 | 用途 |
|------|------|
| VS Code | 前端开发 IDE |
| IntelliJ IDEA | 后端开发 IDE |
| MySQL Workbench | 数据库管理工具 |
| Postman | API 测试工具 |

### 验证环境

```bash
# 检查 Java 版本
java -version
# 期望输出: openjdk version "17.x.x" 或更高

# 检查 Node.js 版本
node -v
# 期望输出: v18.x.x 或更高

# 检查 npm 版本
npm -v
# 期望输出: 9.x.x 或更高

# 检查 MySQL 版本
mysql --version
# 期望输出: mysql Ver 8.x.x
```

---

## 获取代码

```bash
# 克隆仓库
git clone https://github.com/your-org/snack-platform.git

# 进入项目目录
cd snack-platform
```

项目结构：
```
snack-platform/
├── snack-platform-backend/    # 后端项目 (Spring Boot)
├── snack-platform-frontend/   # 前端项目 (Vue.js)
├── docs/                      # 项目文档
└── README.md                  # 项目说明
```

---

## 数据库配置

### 1. 创建数据库

```sql
-- 连接 MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE snack_platform 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 验证创建成功
SHOW DATABASES;
```

### 2. 初始化表结构

```bash
# 执行建表脚本
mysql -u root -p snack_platform < docs/04-DB/schema.sql
```

或者在 MySQL 客户端中执行 [`docs/04-DB/schema.sql`](./04-DB/schema.sql) 文件内容。

### 3. 初始化测试数据（可选）

系统启动后会自动创建默认管理员账号，也可以手动插入测试数据：

```sql
-- 插入测试分类
INSERT INTO categories (name, sort_order, status) VALUES 
('膨化食品', 100, 1),
('坚果炒货', 90, 1),
('糖果巧克力', 80, 1),
('饼干糕点', 70, 1);

-- 插入测试门店
INSERT INTO stores (name, address, longitude, latitude, phone, business_hours, status) VALUES 
('旗舰店', '北京市朝阳区xxx路1号', 116.4074, 39.9042, '010-12345678', '09:00-22:00', 1),
('社区店', '北京市海淀区xxx路2号', 116.3074, 39.9842, '010-87654321', '08:00-21:00', 1);
```

---

## 启动后端服务

### 1. 进入后端目录

```bash
cd snack-platform-backend
```

### 2. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件
```

编辑 `.env` 文件，配置数据库连接：

```bash
# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/snack_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=your_password

# JWT 配置（生产环境请使用更复杂的密钥）
JWT_SECRET=your-super-secure-jwt-secret-key-at-least-256-bits-long-for-hs256
JWT_EXPIRATION=86400000

# 服务器配置
SERVER_PORT=8080

# CORS 配置
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

### 3. 启动服务

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### 4. 验证启动

服务启动成功后，访问 http://localhost:8080/api/health 应返回：

```json
{
  "status": "UP"
}
```

---

## 启动前端服务

### 1. 进入前端目录

```bash
# 从项目根目录
cd snack-platform-frontend

# 或从后端目录
cd ../snack-platform-frontend
```

### 2. 安装依赖

```bash
npm install
```

### 3. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env.development
```

编辑 `.env.development` 文件：

```bash
VITE_API_BASE_URL=http://localhost:8080/api
```

### 4. 启动开发服务器

```bash
npm run dev
```

### 5. 访问应用

打开浏览器访问 http://localhost:3000

---

## 验证安装

### 检查清单

- [ ] 后端服务运行在 http://localhost:8080
- [ ] 前端服务运行在 http://localhost:3000
- [ ] 可以访问登录页面
- [ ] 可以使用测试账号登录

### 快速测试

1. 打开 http://localhost:3000
2. 点击"注册"创建新账号
3. 使用新账号登录
4. 浏览门店和商品列表

---

## 测试账号

系统预置以下测试账号（首次启动时自动创建）：

| 账号 | 密码 | 角色 | 说明 |
|------|------|------|------|
| admin | admin123 | SYSTEM_ADMIN | 系统管理员 |
| store1 | store123 | STORE_ADMIN | 门店管理员（绑定门店1） |
| consumer | consumer123 | CONSUMER | 普通消费者 |

> ⚠️ **注意**：以上为开发测试账号，生产环境请务必修改密码！

### 各角色功能入口

| 角色 | 登录后跳转 | 主要功能 |
|------|------------|----------|
| CONSUMER | 首页 | 浏览商品、下单、查看订单 |
| STORE_ADMIN | 门店后台 | 库存管理、订单处理、核销 |
| SYSTEM_ADMIN | 管理后台 | 商品管理、门店管理、数据统计 |

---

## 常见问题

### Q1: 后端启动失败，提示数据库连接错误

**可能原因**：
- MySQL 服务未启动
- 数据库名称或密码错误
- 数据库未创建

**解决方案**：
```bash
# 检查 MySQL 服务状态
# Windows
net start mysql

# Linux
sudo systemctl status mysql

# 验证数据库连接
mysql -u root -p -e "SHOW DATABASES;"
```

### Q2: 前端启动失败，提示端口被占用

**解决方案**：
```bash
# 修改 vite.config.js 中的端口
server: {
  port: 3001  // 改为其他端口
}

# 或者关闭占用端口的进程
# Windows
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :3000
kill -9 <PID>
```

### Q3: API 请求返回 CORS 错误

**解决方案**：
1. 确认后端 `.env` 中 `CORS_ALLOWED_ORIGINS` 包含前端地址
2. 确认前端 `.env.development` 中 API 地址正确
3. 重启后端服务

### Q4: 登录后页面空白或报错

**可能原因**：
- Token 过期或无效
- 后端服务未启动

**解决方案**：
1. 清除浏览器 localStorage
2. 确认后端服务正常运行
3. 重新登录

### Q5: 如何重置数据库？

```bash
# 删除并重建数据库
mysql -u root -p -e "DROP DATABASE snack_platform; CREATE DATABASE snack_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 重新执行建表脚本
mysql -u root -p snack_platform < docs/04-DB/schema.sql
```

---

## 下一步

- 📖 阅读 [系统架构设计](./02-Architecture/Architecture.md) 了解技术方案
- 📖 阅读 [业务规则](./01-Requirements/BusinessRules.md) 了解业务逻辑
- 📖 阅读 [贡献指南](./CONTRIBUTING.md) 了解开发规范
- 📖 查看 [REST API 文档](./03-API/openapi.md) 了解接口定义

---

## 获取帮助

如果遇到问题：
1. 查看 [常见问题](#常见问题) 部分
2. 搜索项目 Issues
3. 提交新的 Issue 描述问题

---

> 💡 **提示**：建议使用 VS Code 打开项目，安装推荐的扩展以获得更好的开发体验。