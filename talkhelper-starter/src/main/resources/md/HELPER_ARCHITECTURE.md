# Helper Architecture

`talkhelper` 后续会逐步向统一 Helper 结构收敛，优先采用 `COLA-lite` 风格，而不是继续扩展纯 MVC。

推荐分层如下：

- `adapter`：Controller、MQ consumer、WebSocket handler
- `application`：用例编排、Facade、任务调度入口
- `domain`：核心模型、业务规则、网关接口
- `infrastructure`：对象存储、监控、缓存、第三方 SDK、数据库实现

## 当前阶段

第一阶段先不大规模移动包结构，而是先把最耦合的基础设施依赖抽成网关接口：

- 存储：统一通过 `ThObjectStorageGateway`
- 任务进度发布：统一通过 `ThTaskProgressNotifier`
- 任务监控：统一通过 `ThTaskMonitorGateway`

这样可以在不打断当前业务流程的前提下，逐步把实现从本地 Starter 切换到独立的 `store-helper` 和 `monitorhelper`。

## 演进顺序

1. 业务代码只依赖 domain gateway 接口
2. 先用当前本地实现做 infrastructure adapter
3. 补齐 `store-helper` / `monitorhelper` HTTP 或 SDK 适配器
4. 最后再做 package 迁移和服务边界收敛
