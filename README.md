# TalkHelper - AI驱动的播客生成平台

## 项目结构

```
talkhelper/                          # 父项目
├── talkhelper-common/               # 公共模块 - 常量、枚举、异常、工具类
├── talkhelper-text-preprocess/      # 文本预处理模块
├── talkhelper-script-gen/          # 播客脚本生成模块
├── talkhelper-audio/               # 多模态音频合成模块
├── talkhelper-async-task/          # 异步任务模块
├── talkhelper-resource/            # 资源管理模块 (MinIO)
├── talkhelper-user/                # 用户权限管理模块 (Spring Security + JWT)
├── talkhelper-scheduler/           # 调度模块
└── talkhelper-web/                 # Web启动模块 - 应用入口
```

## 技术栈

- **后端框架**: Spring Boot 3.2.3
- **AI集成**: Spring AI Alibaba 1.0.0-M3.2
- **数据库**: MySQL + MyBatis Plus 3.5.5
- **对象存储**: MinIO 8.5.7
- **安全认证**: Spring Security + JWT 0.12.3
- **构建工具**: Maven

## 模块说明

### talkhelper-common
- 系统常量 (`ThConstants`)
- 枚举类 (`ThTaskStatus`)
- 统一返回结果 (`ThResult`)
- 业务异常 (`ThBusinessException`)

### talkhelper-text-preprocess
- 文本清洗和预处理
- 依赖: common

### talkhelper-script-gen
- AI生成播客脚本
- 依赖: common, text-preprocess, Spring AI Alibaba

### talkhelper-audio
- 多模态音频合成
- 依赖: common, script-gen

### talkhelper-async-task
- 异步任务处理
- 依赖: common

### talkhelper-resource
- MinIO文件存储管理
- 依赖: common, MinIO

### talkhelper-user
- 用户管理和权限控制
- 依赖: common, Spring Security, JWT

### talkhelper-scheduler
- 定时任务调度
- 依赖: common, async-task

### talkhelper-web
- 应用启动入口
- 整合所有业务模块
- 配置文件: application.yml

## 快速开始

### 前置要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- MinIO (可选)

### 构建项目
```bash
mvn clean install
```

### 运行应用
```bash
cd talkhelper-web
mvn spring-boot:run
```

### 配置说明
在 `talkhelper-web/src/main/resources/application.yml` 中配置:
- 数据库连接信息
- AI API密钥
- MinIO配置
- JWT密钥

## 开发规范

1. **命名规范**: 所有类统一使用 `Th` 前缀
2. **分层架构**: Controller → Service → Mapper → Entity
3. **模块依赖**: 遵循依赖关系,避免循环依赖
4. **统一返回**: 使用 `ThResult` 封装返回结果

## License

MIT
