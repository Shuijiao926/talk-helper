

## 新建 SDK/Queue 原则
- api 创建 job 时，只传 task 就行，不要传一堆参数比如 inputs 等，这些参数 sdk 内部会从 task 中获取
- 生成结果单元要有 error 字段，每个单元都要有，比如 audios 里每个 audio 都要有 error 字段
- 回调结果放另一个表，避免【回调队列】和【生成队列】同时写表，减少锁冲突
- 队列的基本结构
  - 生成队列
    - ErrorHandler
        - doJobAdapter: 生成功能，要可重复，要独立
        - Adapter 结构（Adapter 的意思，是支持多渠道）
            - 处理 SDK 错误
        - 调用 doJobWith* 函数
            - 调用 SDK
            - 保存 outId
            - 保存 outInputs 和 outOutputs (调用 updateExtra)
            - catch SDK 异常
        - 次数检查
        - 延迟方案
  - 查询队列
    - 超时检查
    - 重新生成方案
    - 延迟方案
    - 结果统一处理（不要放 succeeded 队列）