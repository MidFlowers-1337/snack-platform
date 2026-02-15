# ============================================
# 多阶段构建 - 零食电商平台前端
# ============================================

# 阶段 1: Node.js 构建
FROM node:20-alpine AS builder
WORKDIR /app

# 先复制 package.json 利用缓存
COPY package.json package-lock.json* ./
RUN npm install --registry=https://registry.npmmirror.com

# 复制源码并构建
COPY . .
RUN npm run build

# 阶段 2: Nginx 运行
FROM nginx:alpine

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
