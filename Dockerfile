# ========================================
# TalkHelper 多阶段构建 Dockerfile
# ========================================

# ── Build Stage ──
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# 1. 先复制所有 pom.xml（利用 Docker 层缓存加速依赖下载）
COPY pom.xml .
COPY talkhelper-common/pom.xml         talkhelper-common/
COPY talkhelper-text-preprocess/pom.xml talkhelper-text-preprocess/
COPY talkhelper-script-gen/pom.xml     talkhelper-script-gen/
COPY talkhelper-audio/pom.xml          talkhelper-audio/
COPY talkhelper-task/pom.xml           talkhelper-task/
COPY talkhelper-resource/pom.xml       talkhelper-resource/
COPY talkhelper-user/pom.xml           talkhelper-user/
COPY talkhelper-scheduler/pom.xml      talkhelper-scheduler/
COPY talkhelper-web/pom.xml            talkhelper-web/

# 2. 下载所有依赖（缓存层，源码变动时不需要重新下载）
RUN mvn dependency:go-offline -P docker -B

# 3. 复制全部源码
COPY . .

# 4. 编译打包（使用 docker profile 引入 linux64 FFmpeg）
RUN mvn clean package -P docker -DskipTests -pl talkhelper-web -am -B


# ── Run Stage ──
FROM eclipse-temurin:21-jre-alpine

# 安装 curl（healthcheck 需要）
RUN apk add --no-cache curl

# 创建非 root 用户
RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

# 复制构建产物
COPY --from=build /build/talkhelper-web/target/talkhelper-web-1.0.0.jar app.jar

# 下载 OpenTelemetry Java Agent
RUN curl -fSL https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.4.0/opentelemetry-javaagent.jar \
    -o /app/otel-agent.jar

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
