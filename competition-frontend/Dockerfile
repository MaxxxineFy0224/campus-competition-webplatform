# ========================================
# 前端 Dockerfile —— 多阶段构建
# 阶段 1: Node.js 构建静态资源
# 阶段 2: Nginx 托管
# ========================================

# ---------- 构建阶段 ----------
FROM node:20-alpine AS builder

WORKDIR /build

# 先复制依赖文件，利用 Docker 缓存层
COPY package*.json ./
RUN npm ci

# 复制源码并构建
COPY . .
RUN npm run build

# ---------- 运行阶段 ----------
FROM nginx:alpine

# 删除默认的 Nginx 配置
RUN rm /etc/nginx/conf.d/default.conf

# 复制构建产物
COPY --from=builder /build/dist /usr/share/nginx/html

# Nginx 配置由 docker-compose 通过 volume 挂载，这里不复制

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -qO- http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]