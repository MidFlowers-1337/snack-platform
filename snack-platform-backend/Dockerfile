# ============================================
# 多阶段构建 - 零食电商平台后端
# ============================================

# 阶段 1: Maven 构建
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 先复制 pom.xml 利用 Docker 缓存层加速依赖下载
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并打包
COPY src ./src
RUN mvn package -DskipTests -B

# 阶段 2: 运行时镜像（精简）
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 创建非 root 用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 创建上传目录
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app

# 从构建阶段复制 jar
COPY --from=builder /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

# JVM 优化参数
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
