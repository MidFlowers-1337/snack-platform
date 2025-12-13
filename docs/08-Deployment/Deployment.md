# 部署文档

> 本文档详细说明 Snack Platform 零食电商平台的部署流程，包括开发环境、生产环境及 Docker 容器化部署方案。

---

## 目录

1. [环境要求](#1-环境要求)
2. [开发环境部署](#2-开发环境部署)
3. [生产环境部署](#3-生产环境部署)
4. [Docker 部署（可选）](#4-docker-部署可选)
5. [环境变量说明](#5-环境变量说明)
6. [常见问题排查](#6-常见问题排查)

---

## 1. 环境要求

### 1.1 硬件要求

#### 开发环境

| 资源 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 2 核 | 4 核+ |
| 内存 | 4 GB | 8 GB+ |
| 磁盘 | 10 GB | 20 GB+ |

#### 生产环境

| 资源 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 2 核 | 4 核+ |
| 内存 | 4 GB | 8 GB+ |
| 磁盘 | 50 GB SSD | 100 GB+ SSD |
| 网络 | 1 Mbps | 10 Mbps+ |

### 1.2 软件要求

| 软件 | 版本要求 | 用途 | 下载地址 |
|------|----------|------|----------|
| JDK | 17+ | 后端运行环境 | [Eclipse Temurin](https://adoptium.net/) |
| Node.js | 18+ | 前端构建工具 | [Node.js 官网](https://nodejs.org/) |
| MySQL | 8.0+ | 数据库 | [MySQL 官网](https://dev.mysql.com/downloads/) |
| Nginx | 1.20+ | 反向代理（可选） | [Nginx 官网](https://nginx.org/) |
| Docker | 20.10+ | 容器化部署（可选） | [Docker 官网](https://www.docker.com/) |
| Docker Compose | 2.0+ | 容器编排（可选） | 随 Docker 安装 |

#### 验证软件版本

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

# 检查 Nginx 版本（如已安装）
nginx -v
# 期望输出: nginx version: nginx/1.20.x 或更高

# 检查 Docker 版本（如已安装）
docker --version
# 期望输出: Docker version 20.10.x 或更高
```

---

## 2. 开发环境部署

### 2.1 后端启动

#### 步骤 1：进入后端目录

```bash
cd snack-platform-backend
```

#### 步骤 2：配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env
```

编辑 `.env` 文件，配置开发环境参数：

```bash
# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/snack_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=root

# JWT 配置（开发环境可使用简单密钥）
JWT_SECRET=dev-only-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm-dev
JWT_EXPIRATION=86400000

# 服务器配置
SERVER_PORT=8080

# CORS 配置
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

#### 步骤 3：启动后端服务

**方式一：使用 Maven Wrapper（推荐）**

```bash
# Windows
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

# Linux/Mac
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**方式二：使用本地 Maven**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**方式三：使用 IDE**

在 IntelliJ IDEA 或 Eclipse 中：
1. 导入项目为 Maven 项目
2. 找到 [`SnackPlatformBackendApplication.java`](../snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/SnackPlatformBackendApplication.java:1)
3. 右键运行，添加 VM 参数：`-Dspring.profiles.active=dev`

#### 步骤 4：验证启动

```bash
# 检查健康状态
curl http://localhost:8080/api/actuator/health

# 期望返回
{"status":"UP"}
```

### 2.2 前端启动

#### 步骤 1：进入前端目录

```bash
cd snack-platform-frontend
```

#### 步骤 2：安装依赖

```bash
npm install
```

#### 步骤 3：配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env.development
```

编辑 `.env.development` 文件：

```bash
# API 基础地址
VITE_API_BASE_URL=http://localhost:8080/api

# 是否显示测试账号（开发环境设为 true）
VITE_SHOW_TEST_ACCOUNTS=true

# 应用标题
VITE_APP_TITLE=零食平台（开发环境）
```

#### 步骤 4：启动开发服务器

```bash
npm run dev
```

#### 步骤 5：访问应用

打开浏览器访问 http://localhost:5173

### 2.3 数据库初始化

#### 步骤 1：创建数据库

```sql
-- 连接 MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE IF NOT EXISTS snack_platform 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;

-- 验证创建成功
SHOW DATABASES;
```

#### 步骤 2：执行初始化脚本

```bash
# 执行建表脚本（包含测试数据）
mysql -u root -p snack_platform < docs/04-DB/schema.sql
```

或在 MySQL 客户端中执行 [`docs/04-DB/schema.sql`](../04-DB/schema.sql:1) 文件内容。

#### 步骤 3：验证数据

```sql
USE snack_platform;

-- 检查表结构
SHOW TABLES;

-- 检查测试数据
SELECT * FROM users;
SELECT * FROM stores;
SELECT * FROM categories;
```

#### 测试账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | SYSTEM_ADMIN | 系统管理员 |
| store1 | 123456 | STORE_ADMIN | 门店管理员（绑定北京店） |
| user1 | 123456 | CONSUMER | 普通消费者 |

---

## 3. 生产环境部署

### 3.1 后端打包与部署

#### Maven 打包

```bash
cd snack-platform-backend

# 清理并打包（跳过测试）
./mvnw clean package -DskipTests

# 或使用本地 Maven
mvn clean package -DskipTests
```

打包完成后，JAR 文件位于 `target/snack-platform-backend-0.0.1-SNAPSHOT.jar`

#### JAR 运行

**方式一：直接运行**

```bash
java -jar target/snack-platform-backend-0.0.1-SNAPSHOT.jar
```

**方式二：使用环境变量运行**

```bash
# 设置环境变量并运行
export DB_URL="jdbc:mysql://your-db-host:3306/snack_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=true"
export DB_USERNAME="your_username"
export DB_PASSWORD="your_secure_password"
export JWT_SECRET="your-super-secure-jwt-secret-key-at-least-256-bits-long-for-hs256"
export CORS_ALLOWED_ORIGINS="https://your-domain.com"

java -jar target/snack-platform-backend-0.0.1-SNAPSHOT.jar
```

**方式三：使用启动脚本**

创建 `start.sh` 脚本：

```bash
#!/bin/bash

# 应用配置
APP_NAME="snack-platform-backend"
JAR_FILE="snack-platform-backend-0.0.1-SNAPSHOT.jar"
LOG_FILE="app.log"

# 环境变量
export DB_URL="jdbc:mysql://localhost:3306/snack_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=true"
export DB_USERNAME="snack_user"
export DB_PASSWORD="your_secure_password"
export JWT_SECRET="your-super-secure-jwt-secret-key-at-least-256-bits-long-for-hs256"
export JWT_EXPIRATION="86400000"
export SERVER_PORT="8080"
export MANAGEMENT_PORT="8081"
export CORS_ALLOWED_ORIGINS="https://your-domain.com"
export LOG_LEVEL="info"

# JVM 参数
JVM_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# 启动应用
nohup java $JVM_OPTS -jar $JAR_FILE > $LOG_FILE 2>&1 &

echo "$APP_NAME started with PID $!"
```

赋予执行权限并运行：

```bash
chmod +x start.sh
./start.sh
```

#### 环境变量配置

生产环境必须配置以下环境变量：

| 变量名 | 必需 | 说明 | 示例值 |
|--------|------|------|--------|
| DB_URL | ✅ | 数据库连接 URL | `jdbc:mysql://localhost:3306/snack_platform?...` |
| DB_USERNAME | ✅ | 数据库用户名 | `snack_user` |
| DB_PASSWORD | ✅ | 数据库密码 | `your_secure_password` |
| JWT_SECRET | ✅ | JWT 签名密钥（至少 32 字符） | `your-super-secure-jwt-secret...` |
| JWT_EXPIRATION | ❌ | Token 过期时间（毫秒） | `86400000`（24小时） |
| SERVER_PORT | ❌ | 服务端口 | `8080` |
| MANAGEMENT_PORT | ❌ | Actuator 管理端口 | `8081` |
| CORS_ALLOWED_ORIGINS | ❌ | 允许的跨域来源 | `https://your-domain.com` |
| LOG_LEVEL | ❌ | 日志级别 | `info` |

#### 使用 systemd 管理服务（Linux）

创建服务文件 `/etc/systemd/system/snack-platform.service`：

```ini
[Unit]
Description=Snack Platform Backend Service
After=network.target mysql.service

[Service]
Type=simple
User=snack
Group=snack
WorkingDirectory=/opt/snack-platform
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar snack-platform-backend-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

# 环境变量
Environment="DB_URL=jdbc:mysql://localhost:3306/snack_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=true"
Environment="DB_USERNAME=snack_user"
Environment="DB_PASSWORD=your_secure_password"
Environment="JWT_SECRET=your-super-secure-jwt-secret-key-at-least-256-bits-long-for-hs256"
Environment="CORS_ALLOWED_ORIGINS=https://your-domain.com"

[Install]
WantedBy=multi-user.target
```

管理服务：

```bash
# 重新加载 systemd 配置
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start snack-platform

# 设置开机自启
sudo systemctl enable snack-platform

# 查看服务状态
sudo systemctl status snack-platform

# 查看日志
sudo journalctl -u snack-platform -f
```

### 3.2 前端打包与部署

#### npm build

```bash
cd snack-platform-frontend

# 配置生产环境变量
cp .env.example .env.production
```

编辑 `.env.production` 文件：

```bash
# API 基础地址（生产环境）
VITE_API_BASE_URL=https://api.your-domain.com/api

# 禁用测试账号显示
VITE_SHOW_TEST_ACCOUNTS=false

# 应用标题
VITE_APP_TITLE=零食平台
```

执行构建：

```bash
# 构建生产版本
npm run build
```

构建完成后，静态文件位于 `dist/` 目录。

#### 静态文件部署

**方式一：直接部署到 Nginx**

```bash
# 复制构建产物到 Nginx 目录
sudo cp -r dist/* /var/www/snack-platform/
```

**方式二：使用 rsync 同步**

```bash
rsync -avz --delete dist/ user@server:/var/www/snack-platform/
```

**方式三：使用 CI/CD 自动部署**

参考 GitHub Actions 或 GitLab CI 配置。

### 3.3 Nginx 配置

#### 反向代理配置

创建 Nginx 配置文件 `/etc/nginx/sites-available/snack-platform`：

```nginx
# 上游服务器配置
upstream snack_backend {
    server 127.0.0.1:8080;
    keepalive 32;
}

# HTTP 重定向到 HTTPS
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    return 301 https://$server_name$request_uri;
}

# HTTPS 主配置
server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;

    # SSL 证书配置
    ssl_certificate /etc/nginx/ssl/your-domain.com.crt;
    ssl_certificate_key /etc/nginx/ssl/your-domain.com.key;
    
    # SSL 安全配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 1d;

    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;

    # 静态资源根目录
    root /var/www/snack-platform;
    index index.html;

    # Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied any;
    gzip_types text/plain text/css text/xml text/javascript application/javascript application/json application/xml;

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        access_log off;
    }

    # API 反向代理
    location /api/ {
        proxy_pass http://snack_backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Connection "";
        
        # 超时配置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # 缓冲配置
        proxy_buffering on;
        proxy_buffer_size 4k;
        proxy_buffers 8 4k;
    }

    # Actuator 端点（仅内网访问）
    location /api/actuator/ {
        allow 127.0.0.1;
        allow 10.0.0.0/8;
        allow 172.16.0.0/12;
        allow 192.168.0.0/16;
        deny all;
        
        proxy_pass http://127.0.0.1:8081/actuator/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
    }

    # Vue Router 历史模式支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 错误页面
    error_page 500 502 503 504 /50x.html;
    location = /50x.html {
        root /usr/share/nginx/html;
    }

    # 访问日志
    access_log /var/log/nginx/snack-platform.access.log;
    error_log /var/log/nginx/snack-platform.error.log;
}
```

#### 启用配置

```bash
# 创建软链接
sudo ln -s /etc/nginx/sites-available/snack-platform /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重新加载 Nginx
sudo systemctl reload nginx
```

#### HTTPS 配置（可选）

**使用 Let's Encrypt 免费证书：**

```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d your-domain.com -d www.your-domain.com

# 自动续期测试
sudo certbot renew --dry-run
```

**使用自签名证书（仅测试环境）：**

```bash
# 创建证书目录
sudo mkdir -p /etc/nginx/ssl

# 生成自签名证书
sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout /etc/nginx/ssl/your-domain.com.key \
    -out /etc/nginx/ssl/your-domain.com.crt
```

---

## 4. Docker 部署（可选）

### 4.1 Dockerfile 编写

#### 后端 Dockerfile

创建 `snack-platform-backend/Dockerfile`：

```dockerfile
# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制 pom.xml 并下载依赖（利用缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 创建非 root 用户
RUN addgroup -g 1001 -S snack && \
    adduser -u 1001 -S snack -G snack

# 复制构建产物
COPY --from=builder /app/target/*.jar app.jar

# 设置权限
RUN chown -R snack:snack /app

USER snack

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

# 暴露端口
EXPOSE 8080 8081

# 启动命令
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "app.jar"]
```

#### 前端 Dockerfile

创建 `snack-platform-frontend/Dockerfile`：

```dockerfile
# 构建阶段
FROM node:18-alpine AS builder

WORKDIR /app

# 复制 package.json 并安装依赖
COPY package*.json ./
RUN npm ci --only=production

# 复制源码并构建
COPY . .
RUN npm run build

# 运行阶段
FROM nginx:alpine

# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/nginx.conf

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:80/ || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

#### 前端 Nginx 配置

创建 `snack-platform-frontend/nginx.conf`：

```nginx
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    keepalive_timeout 65;
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;

    server {
        listen 80;
        server_name localhost;
        root /usr/share/nginx/html;
        index index.html;

        location / {
            try_files $uri $uri/ /index.html;
        }

        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
}
```

### 4.2 docker-compose 配置

创建项目根目录下的 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  # MySQL 数据库
  mysql:
    image: mysql:8.0
    container_name: snack-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root123456}
      MYSQL_DATABASE: snack_platform
      MYSQL_USER: snack_user
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-snack123456}
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./docs/04-DB/schema.sql:/docker-entrypoint-initdb.d/init.sql:ro
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_general_ci
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - snack-network

  # 后端服务
  backend:
    build:
      context: ./snack-platform-backend
      dockerfile: Dockerfile
    container_name: snack-backend
    restart: unless-stopped
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      DB_URL: jdbc:mysql://mysql:3306/snack_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
      DB_USERNAME: snack_user
      DB_PASSWORD: ${MYSQL_PASSWORD:-snack123456}
      JWT_SECRET: ${JWT_SECRET:-your-super-secure-jwt-secret-key-at-least-256-bits-long-for-hs256}
      JWT_EXPIRATION: 86400000
      SERVER_PORT: 8080
      CORS_ALLOWED_ORIGINS: http://localhost,http://localhost:80
    ports:
      - "8080:8080"
      - "8081:8081"
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/api/actuator/health"]
      interval: 30s
      timeout: 10s
      start_period: 60s
      retries: 3
    networks:
      - snack-network

  # 前端服务
  frontend:
    build:
      context: ./snack-platform-frontend
      dockerfile: Dockerfile
      args:
        VITE_API_BASE_URL: http://localhost:8080/api
    container_name: snack-frontend
    restart: unless-stopped
    depends_on:
      - backend
    ports:
      - "80:80"
    networks:
      - snack-network

volumes:
  mysql_data:
    driver: local

networks:
  snack-network:
    driver: bridge
```

#### 生产环境 docker-compose

创建 `docker-compose.prod.yml`：

```yaml
version: '3.8'

services:
  backend:
    image: your-registry/snack-backend:latest
    environment:
      DB_URL: ${DB_URL}
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS}
    deploy:
      replicas: 2
      resources:
        limits:
          cpus: '1'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M

  frontend:
    image: your-registry/snack-frontend:latest
    deploy:
      replicas: 2
      resources:
        limits:
          cpus: '0.5'
          memory: 256M
```

### 4.3 容器启动

#### 开发环境启动

```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
```

#### 常用命令

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v

# 重启特定服务
docker-compose restart backend

# 进入容器
docker-compose exec backend sh

# 查看容器资源使用
docker stats
```

#### 生产环境启动

```bash
# 使用生产配置启动
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# 或使用环境变量文件
docker-compose --env-file .env.production up -d
```

---

## 5. 环境变量说明

### 后端环境变量

参考 [`snack-platform-backend/.env.example`](../snack-platform-backend/.env.example:1)

| 变量名 | 必需 | 默认值 | 说明 |
|--------|------|--------|------|
| `DB_URL` | ✅ | - | MySQL 数据库连接 URL |
| `DB_USERNAME` | ✅ | - | 数据库用户名 |
| `DB_PASSWORD` | ✅ | - | 数据库密码 |
| `JWT_SECRET` | ✅ | - | JWT 签名密钥（至少 32 字符） |
| `JWT_EXPIRATION` | ❌ | `86400000` | Token 过期时间（毫秒，默认 24 小时） |
| `SERVER_PORT` | ❌ | `8080` | 应用服务端口 |
| `MANAGEMENT_PORT` | ❌ | `8081` | Actuator 管理端口 |
| `LOG_LEVEL` | ❌ | `info` | 应用日志级别 |
| `SECURITY_LOG_LEVEL` | ❌ | `warn` | 安全模块日志级别 |
| `MYBATIS_LOG_IMPL` | ❌ | `Slf4jImpl` | MyBatis 日志实现 |
| `CORS_ALLOWED_ORIGINS` | ❌ | `localhost:3000,5173` | 允许的跨域来源 |

### 前端环境变量

参考 [`snack-platform-frontend/.env.example`](../snack-platform-frontend/.env.example:1)

| 变量名 | 必需 | 默认值 | 说明 |
|--------|------|--------|------|
| `VITE_API_BASE_URL` | ✅ | - | 后端 API 基础地址 |
| `VITE_SHOW_TEST_ACCOUNTS` | ❌ | `false` | 是否显示测试账号（生产环境应为 false） |
| `VITE_APP_TITLE` | ❌ | `零食平台` | 应用标题 |

### 环境变量配置示例

#### 开发环境

**后端 `.env`：**
```bash
DB_URL=jdbc:mysql://localhost:3306/snack_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=root
JWT_SECRET=dev-only-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm-dev
JWT_EXPIRATION=86400000
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
LOG_LEVEL=debug
```

**前端 `.env.development`：**
```bash
VITE_API_BASE_URL=http://localhost:8080/api
VITE_SHOW_TEST_ACCOUNTS=true
VITE_APP_TITLE=零食平台（开发环境）
```

#### 生产环境

**后端环境变量：**
```bash
DB_URL=jdbc:mysql://prod-db-host:3306/snack_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=true
DB_USERNAME=snack_prod_user
DB_PASSWORD=<strong-password>
JWT_SECRET=<random-256-bit-secret>
JWT_EXPIRATION=86400000
SERVER_PORT=8080
MANAGEMENT_PORT=8081
CORS_ALLOWED_ORIGINS=https://your-domain.com
LOG_LEVEL=info
```

**前端 `.env.production`：**
```bash
VITE_API_BASE_URL=https://api.your-domain.com/api
VITE_SHOW_TEST_ACCOUNTS=false
VITE_APP_TITLE=零食平台
```

---

## 6. 常见问题排查

### 6.1 后端启动失败

#### 问题：数据库连接失败

**错误信息：**
```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
```

**排查步骤：**

1. **检查 MySQL 服务状态**
   ```bash
   # Windows
   net start mysql
   
   # Linux
   sudo systemctl status mysql
   ```

2. **验证数据库连接**
   ```bash
   mysql -h localhost -u root -p -e "SELECT 1"
   ```

3. **检查连接 URL 格式**
   ```bash
   # 正确格式
   jdbc:mysql://localhost:3306/snack_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
   ```

4. **检查防火墙设置**
   ```bash
   # Linux
   sudo ufw status
   sudo ufw allow 3306
   ```

#### 问题：端口被占用

**错误信息：**
```
Web server failed to start. Port 8080 was already in use.
```

**解决方案：**

```bash
# Windows - 查找占用端口的进程
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>

# 或修改端口
export SERVER_PORT=8081
```

#### 问题：JWT 密钥配置错误

**错误信息：**
```
io.jsonwebtoken.security.WeakKeyException: The specified key byte array is 128 bits
```

**解决方案：**

确保 JWT_SECRET 至少 32 字符（256 位）：

```bash
# 生成安全密钥
openssl rand -base64 32

# 配置示例
JWT_SECRET=your-super-secure-jwt-secret-key-at-least-256-bits-long-for-hs256
```

#### 问题：内存不足

**错误信息：**
```
java.lang.OutOfMemoryError: Java heap space
```

**解决方案：**

```bash
# 增加 JVM 内存
java -Xms512m -Xmx1024m -jar app.jar

# 或设置环境变量
export JAVA_OPTS="-Xms512m -Xmx1024m"
```

### 6.2 前端构建失败

#### 问题：依赖安装失败

**错误信息：**
```
npm ERR! code ERESOLVE
npm ERR! ERESOLVE unable to resolve dependency tree
```

**解决方案：**

```bash
# 清除缓存
npm cache clean --force

# 删除 node_modules 和 lock 文件
rm -rf node_modules package-lock.json

# 重新安装
npm install

# 或使用 legacy-peer-deps
npm install --legacy-peer-deps
```

#### 问题：构建内存不足

**错误信息：**
```
FATAL ERROR: Ineffective mark-compacts near heap limit Allocation failed
```

**解决方案：**

```bash
# 增加 Node.js 内存限制
export NODE_OPTIONS="--max-old-space-size=4096"
npm run build
```

#### 问题：环境变量未生效

**排查步骤：**

1. 确认环境变量文件名正确：
   - 开发环境：`.env.development`
   - 生产环境：`.env.production`

2. 确认变量前缀为 `VITE_`：
   ```bash
   # 正确
   VITE_API_BASE_URL=http://localhost:8080/api
   
   # 错误（不会被 Vite 识别）
   API_BASE_URL=http://localhost:8080/api
   ```

3. 重启开发服务器：
   ```bash
   npm run dev
   ```

### 6.3 数据库连接问题

#### 问题：字符集错误

**错误信息：**
```
Incorrect string value: '\xF0\x9F\x98\x80...' for column 'name'
```

**解决方案：**

```sql
-- 修改数据库字符集
ALTER DATABASE snack_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 修改表字符集
ALTER TABLE products CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

#### 问题：连接数过多

**错误信息：**
```
Too many connections
```

**解决方案：**

```sql
-- 查看当前连接数
SHOW STATUS LIKE 'Threads_connected';

-- 查看最大连接数
SHOW VARIABLES LIKE 'max_connections';

-- 增加最大连接数
SET GLOBAL max_connections = 500;
```

#### 问题：慢查询

**排查步骤：**

```sql
-- 开启慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;

-- 查看慢查询日志位置
SHOW VARIABLES LIKE 'slow_query_log_file';
```

### 6.4 跨域问题

#### 问题：CORS 错误

**错误信息：**
```
Access to XMLHttpRequest at 'http://localhost:8080/api/...' from origin 'http://localhost:5173' has been blocked by CORS policy
```

**解决方案：**

1. **检查后端 CORS 配置**

   确认 `CORS_ALLOWED_ORIGINS` 包含前端地址：
   ```bash
   CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
   ```

2. **检查 Nginx 配置**

   确保 Nginx 正确传递请求头：
   ```nginx
   location /api/ {
       proxy_pass http://backend;
       proxy_set_header Host $host;
       proxy_set_header X-Real-IP $remote_addr;
       proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
       proxy_set_header X-Forwarded-Proto $scheme;
   }
   ```

3. **重启后端服务**
   ```bash
   # 修改配置后需要重启
   sudo systemctl restart snack-platform
   ```

#### 问题：预检请求失败

**错误信息：**
```
Response to preflight request doesn't pass access control check
```

**解决方案：**

确保后端正确处理 OPTIONS 请求，检查 [`SecurityConfig.java`](../snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/common/config/SecurityConfig.java:1) 中的 CORS 配置。

---

## 附录

### A. 部署检查清单

#### 生产环境上线前检查

- [ ] 数据库已创建并初始化
- [ ] 所有必需环境变量已配置
- [ ] JWT_SECRET 使用安全的随机密钥
- [ ] 数据库密码已更改为强密码
- [ ] CORS 配置仅允许生产域名
- [ ] HTTPS 证书已配置
- [ ] 日志级别设置为 info
- [ ] Actuator 端点已限制访问
- [ ] 防火墙规则已配置
- [ ] 备份策略已制定

#### 部署后验证

- [ ] 后端健康检查通过：`/api/actuator/health`
- [ ] 前端页面正常加载
- [ ] 用户可以正常登录
- [ ] API 请求正常响应
- [ ] 日志正常输出

### B. 常用命令速查

```bash
# 后端
./mvnw spring-boot:run                    # 开发模式启动
./mvnw clean package -DskipTests          # 打包
java -jar target/*.jar                    # 运行 JAR

# 前端
npm install                               # 安装依赖
npm run dev                               # 开发模式
npm run build                             # 生产构建

# Docker
docker-compose up -d                      # 启动所有服务
docker-compose down                       # 停止所有服务
docker-compose logs -f                    # 查看日志

# 数据库
mysql -u root -p snack_platform < schema.sql  # 初始化数据库

# Nginx
sudo nginx -t                             # 测试配置
sudo systemctl reload nginx               # 重载配置
```

### C. 相关文档

- [快速开始指南](../QUICKSTART.md)
- [系统架构设计](../02-Architecture/Architecture.md)
- [API 文档](../03-API/openapi.md)
- [数据库设计](../04-DB/schema.md)
- [贡献指南](../CONTRIBUTING.md)

---

> 📝 **文档版本**：1.0.0
> 📅 **最后更新**：2025-12-13
> 👥 **维护团队**：Snack Platform Team