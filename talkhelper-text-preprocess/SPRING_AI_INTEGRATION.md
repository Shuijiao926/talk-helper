# Spring AI Alibaba 集成指南

## 📋 概述

本项目已集成 **Spring AI Alibaba**，使用阿里云通义千问（DashScope）大模型，实现文本到播客脚本的智能转换。

---

## 🔧 技术栈

- **框架**: Spring AI Alibaba 1.0.0-M3.2
- **模型**: 通义千问 qwen-plus
- **功能**: 文本 → 播客脚本生成

---

## ⚙️ 配置说明

### 1. API Key 配置

在 `application.yml` 中配置：

```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY:sk-5355539b05fc43c598e03cf05fc47117}
      chat:
        options:
          model: qwen-plus      # 模型选择
          temperature: 0.7       # 创造性 (0-1)
          top-p: 0.8             # 采样参数
          max-tokens: 2000       # 最大输出长度
```

**环境变量方式（推荐生产环境）：**
```bash
export DASHSCOPE_API_KEY=your-api-key-here
```

### 2. 模型选择

| 模型 | 特点 | 适用场景 | 价格 |
|------|------|---------|------|
| qwen-turbo | 速度快，成本低 | 简单任务 | 💰 |
| qwen-plus | 平衡性能和成本 | **通用场景（推荐）** | 💰💰 |
| qwen-max | 最强性能 | 复杂推理 | 💰💰💰 |

---

## 🎯 核心组件

### 1. ThAiProcessHandler（AI处理处理器）

**职责**：
- 合并文本分块
- 加载提示词模板
- 调用大模型生成播客脚本
- 处理异常和日志

**工作流程**：
```
文本分块 → 合并全文 → 选择模板 → 渲染Prompt → 调用LLM → 返回脚本
```

### 2. ThPodcastPromptTemplateService（提示词模板服务）

**职责**：
- 加载5种播客脚本模板
- 缓存模板避免重复读取
- 渲染模板变量
- 提供默认模板兜底

**支持的模板**：
- `template_standard.md` - 标准通用（5分钟）
- `template_deep_dive.md` - 深度解析（10-15分钟）
- `template_quick_tips.md` - 快速技巧（3分钟）
- `template_storytelling.md` - 故事叙述（8分钟）
- `template_debate.md` - 观点辩论（6分钟）

---

## 💡 使用示例

### 基础用法

```java
// 1. 设置上下文属性
context.setAttribute("needAiProcess", true);
context.setAttribute("templateType", "standard"); // 选择模板类型

// 2. Pipeline自动执行
pipeline.execute(context);

// 3. 获取结果
String podcastScript = context.getAiResult();
```

### 选择不同模板

```java
// 标准通用模板（5分钟）
context.setAttribute("templateType", "standard");

// 深度解析模板（10-15分钟）
context.setAttribute("templateType", "deep_dive");

// 快速技巧模板（3分钟）
context.setAttribute("templateType", "quick_tips");

// 故事叙述模板（8分钟）
context.setAttribute("templateType", "storytelling");

// 观点辩论模板（6分钟）
context.setAttribute("templateType", "debate");
```

### 完整流程示例

```java
@PostMapping("/generate-podcast")
public ThResult<String> generatePodcast(@RequestBody ThFileUploadRequest request) {
    return ThResultHelper.execute(() -> {
        // 1. 上传并预处理文本
        var preprocessResult = textPreprocessService.uploadAndProcessWithConfig(request);
        
        // 2. 设置AI处理参数
        ThTextProcessContext context = new ThTextProcessContext();
        context.setChunks(preprocessResult.getChunks());
        context.setAttribute("needAiProcess", true);
        context.setAttribute("templateType", "standard");
        
        // 3. 执行AI处理
        aiProcessHandler.handle(context);
        
        // 4. 返回播客脚本
        return context.getAiResult();
    });
}
```

---

## 📊 性能优化

### 1. 模板缓存

```java
// ThPodcastPromptTemplateService 内部使用 Map 缓存
private final Map<String, String> templateCache = new HashMap<>();

// 首次加载后，后续直接从内存读取
templateCache.computeIfAbsent(templateName, name -> loadFromFile(name));
```

### 2. 文本分块策略

```java
// 对于长文本，建议先分块再合并
// 避免超出模型上下文窗口（qwen-plus: 32K tokens）
ThTextChunkConfig config = ThTextChunkConfig.builder()
    .chunkSize(1000)      // 每块1000字
    .overlapSize(100)     // 重叠100字保持连贯
    .build();
```

### 3. 异步处理

```java
// 大模型调用耗时较长（3-10秒），建议使用异步任务
String taskId = taskService.createTask(...);
// Worker后台调用AI，WebSocket推送进度
```

---

## 🔍 调试技巧

### 1. 查看Prompt

```java
log.info("最终Prompt长度: {}", finalPrompt.length());
log.debug("Prompt内容:\n{}", finalPrompt);
```

### 2. 监控Token使用

```java
// DashScope返回包含token使用情况
ChatResponse response = chatModel.call(prompt, ChatOptions.builder().build());
log.info("Token使用: {}", response.getMetadata().getUsage());
```

### 3. 测试API连通性

```bash
curl https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "qwen-plus",
    "input": {"messages": [{"role": "user", "content": "你好"}]}
  }'
```

---

## ⚠️ 常见问题

### Q1: API调用失败

**错误信息**：`Authentication failed`

**解决方案**：
1. 检查API Key是否正确
2. 确认账户余额充足
3. 验证网络连通性

### Q2: 输出被截断

**原因**：超过max-tokens限制

**解决方案**：
```yaml
spring:
  ai:
    dashscope:
      chat:
        options:
          max-tokens: 4000  # 增加输出长度
```

### Q3: 响应速度慢

**优化方案**：
1. 切换到 `qwen-turbo` 模型
2. 减少输入文本长度
3. 降低 `temperature` 参数

### Q4: 模板文件找不到

**检查**：
```bash
# 确认模板文件位置
ls talkhelper-text-preprocess/src/main/resources/podcast-templates/
```

---

## 🚀 最佳实践

### 1. Prompt工程

```markdown
✅ 好的Prompt：
- 明确角色设定
- 清晰的结构要求
- 具体的标注规范
- 完整的输出格式

❌ 差的Prompt：
- 模糊的指令
- 缺少示例
- 没有格式要求
```

### 2. 错误处理

```java
try {
    String result = chatModel.call(prompt);
    if (result == null || result.isEmpty()) {
        // 处理空响应
    }
} catch (Exception e) {
    log.error("AI调用失败", e);
    // 降级处理：返回默认模板或缓存结果
}
```

### 3. 成本控制

```java
// 估算Token数量（中文约1.5字/token）
int estimatedTokens = inputText.length() / 1.5;
if (estimatedTokens > 28000) {
    // 超过qwen-plus上下文窗口，需要分块处理
    splitAndProcess(inputText);
}
```

---

## 📚 参考资源

- [Spring AI Alibaba 官方文档](https://spring-ai.alibaba.com/)
- [通义千问 DashScope API](https://help.aliyun.com/zh/dashscope/)
- [Prompt Engineering 指南](https://platform.openai.com/docs/guides/prompt-engineering)

---

## 🔄 版本历史

- v1.0 (2026-04-11) - 初始集成，支持5种模板
- TODO: 添加流式输出支持
- TODO: 支持多模型切换
- TODO: 添加Prompt版本管理
