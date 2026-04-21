# ========================================
# TalkHelper Dockerfile（预构建模式）
# ========================================
# 使用前需先在宿主机完成 Maven 构建:
#   mvn clean package -P docker -DskipTests
#
# 然后:
#   docker compose up -d --build
# ========================================

FROM eclipse-temurin:21-jre-alpine

# 安装 curl（healthcheck）、bash + ffmpeg（音频拼接）、时区数据
RUN apk add --no-cache curl bash ffmpeg tzdata \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && apk del tzdata

# 创建非 root 用户
RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

# 复制宿主机上已构建好的 JAR
COPY talkhelper-web/target/talkhelper-web-1.0.0.jar app.jar

# 复制 OTel Agent（宿主机已下载）
COPY otel-agent.jar /app/otel-agent.jar

# 创建日志目录并设置权限
RUN mkdir -p /app/logs && chown -R app:app /app

USER app

# OTel Agent 通过环境变量注入（docker-compose 可覆盖）
ENV JAVA_TOOL_OPTIONS="-javaagent:/app/otel-agent.jar"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=60s \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
