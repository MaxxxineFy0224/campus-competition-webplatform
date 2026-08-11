# ========================================
# 后端 Dockerfile —— 多阶段构建
# 阶段 1: Maven 编译打包
# 阶段 2: JRE 运行
# ========================================

# ---------- 构建阶段 ----------
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# 先复制 pom.xml，利用 Docker 缓存层加速依赖下载
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并打包（跳过测试，测试应在 CI 阶段单独运行）
COPY src ./src
RUN mvn package -DskipTests -B

# ---------- 运行阶段 ----------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 创建非 root 用户运行应用
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 复制构建好的 jar
COPY --from=builder /build/target/*.jar app.jar

# 切换到非 root 用户
USER appuser

# 默认使用 MySQL 生产配置
ENV SPRING_PROFILES_ACTIVE=mysql

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]