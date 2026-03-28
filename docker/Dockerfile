# =====================================================
# 客户服务系统 Dockerfile（多阶段构建）
# =====================================================
# 阶段 1：编译后端
# 阶段 2：运行时镜像
# =====================================================

# ========== 阶段 1：Maven 编译 ==========
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# 安装 Maven
RUN apk add --no-cache maven

# 复制 pom.xml 先下载依赖（利用 Docker 缓存）
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并编译
COPY backend/src ./src
RUN mvn package -DskipTests -B

# ========== 阶段 2：运行时 ==========
FROM eclipse-temurin:21-jre-alpine

# 设置工作目录
WORKDIR /app

# 安装必要的工具
RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 创建非 root 用户（安全最佳实践）
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -D -s /bin/sh appuser

# 创建文件存储目录并设置权限
RUN mkdir -p /data/client-service/files && \
    chown -R appuser:appgroup /data/client-service && \
    chmod 755 /data/client-service/files

# 创建日志目录并设置权限
RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app/logs

# 从编译阶段复制 JAR
COPY --from=builder /build/target/client-service-*.jar app.jar

# 设置应用文件权限
RUN chown appuser:appgroup app.jar

# 切换到非 root 用户
USER appuser

# 暴露端口
EXPOSE 8081

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/api/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "exec java \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} \
    -jar app.jar"]
