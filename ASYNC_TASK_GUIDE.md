# TalkHelper 异步任务系统 - 启动指南

## 📋 前置要求

1. Docker Desktop 已安装
2. JDK 21
3. Maven 3.8+

## 🚀 快速启动

### 1. 启动中间件（Redis + MySQL）

```bash
# 在项目根目录执行
docker-compose up -d

# 验证服务
docker ps
```

预期输出：
```
CONTAINER ID   IMAGE            STATUS         PORTS                    NAMES
xxx            redis:7-alpine   Up (healthy)   0.0.0.0:6379->6379/tcp   talkhelper-redis
xxx            mysql:8.0        Up (healthy)   0.0.0.0:3306->3306/tcp   talkhelper-mysql
```

### 2. 初始化数据库

```bash
# 连接到MySQL
docker exec -it talkhelper-mysql mysql -uroot -proot123

# 执行建表SQL
source /path/to/talkhelper-task/src/main/resources/db/migration/V1.0.0
```

或者使用Navicat/DataGrip等工具执行SQL文件。

### 3. 编译项目

```bash
mvn clean install -DskipTests
```

### 4. 启动应用

```bash
cd talkhelper-web
mvn spring-boot:run
```

## 🧪 测试流程

### 1. 上传文件（创建任务）

```bash
curl -X POST http://localhost:8080/api/text-preprocess/file/upload \
  -F "file=@test.pdf"
```

响应示例：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": "TASK_1712834567890_1234",
    "message": "任务已创建，请通过WebSocket订阅进度"
  }
}
```

### 2. WebSocket订阅进度

使用wscat或浏览器JavaScript：

```javascript
const ws = new WebSocket('ws://localhost:8080/ws/task-progress');

ws.onopen = () => {
  console.log('WebSocket连接成功');
  
  // 订阅任务进度
  ws.send(JSON.stringify({
    action: 'subscribe',
    taskId: 'TASK_1712834567890_1234'
  }));
};

ws.onmessage = (event) => {
  const progress = JSON.parse(event.data);
  console.log('进度更新:', progress);
  
  // 示例输出：
  // {taskId: "xxx", status: "processing", progress: 10, currentStage: "正在解析文档"}
  // {taskId: "xxx", status: "processing", progress: 50, currentStage: "正在清洗文本"}
  // {taskId: "xxx", status: "completed", progress: 100, currentStage: "处理完成", resultData: {...}}
};
```

### 3. 查询任务状态（HTTP轮询备用方案）

```bash
curl http://localhost:8080/api/text-preprocess/task/TASK_1712834567890_1234
```

### 4. 取消任务

```bash
curl -X POST http://localhost:8080/api/text-preprocess/task/TASK_1712834567890_1234/cancel
```

## 📊 架构说明

### 数据流

```
用户请求 → Controller（<100ms返回taskId）
              ↓
         Redis Queue（任务队列）
              ↓
         Worker消费（多并发）
              ↓
         Pipeline处理（解析→清洗→分块）
              ↓
         结果存入DB + Redis
              ↓
         Redis Pub/Sub推送进度
              ↓
         WebSocket推送给前端
```

### 关键组件

| 组件 | 作用 |
|------|------|
| ThAsyncTaskService | 任务管理服务（创建/更新/查询） |
| ThTaskWorker | 任务消费者（CPU核数个线程） |
| ThTaskProgressWebSocketHandler | WebSocket进度推送 |
| TaskProgressRedisListener | Redis消息监听器 |

### 性能指标

| 指标 | 优化前 | 优化后 |
|------|--------|--------|
| HTTP响应时间 | 5-60s | <100ms |
| QPS | 10-20 | 200-500 |
| 并发任务数 | 200 | 10000+ |

## 🔧 配置调优

### application.yml

```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 50  # 根据并发调整
          max-idle: 20
          min-idle: 5
```

### Worker线程数

默认 = CPU核数，可在 `ThTaskWorker.java` 中调整：

```java
int workerCount = Runtime.getRuntime().availableProcessors();
// 或固定值：int workerCount = 8;
```

## 🐛 常见问题

### 1. Redis连接失败

```bash
# 检查Redis是否运行
docker ps | grep redis

# 查看日志
docker logs talkhelper-redis
```

### 2. 任务不消费

检查Worker是否启动：
```
日志应包含：========== 任务Worker启动 ==========
```

### 3. WebSocket连接失败

确保端口8080未被占用，检查防火墙设置。

## 📝 下一步优化

1. ✅ 异步任务框架（已完成）
2. ⏳ 线程池隔离（待实施）
3. ⏳ 缓存层（Caffeine + Redis）
4. ⏳ 限流熔断（Sentinel）
5. ⏳ MinIO对象存储

---

**技术支持**: 查看日志文件 `logs/talkhelper.log`
