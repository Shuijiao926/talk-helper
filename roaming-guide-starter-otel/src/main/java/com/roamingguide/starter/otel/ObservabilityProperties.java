package com.roamingguide.starter.otel;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 可观测性配置属性
 */
@Data
@ConfigurationProperties(prefix = "roaming-guide.observability")
public class ObservabilityProperties {

    /** 是否启用链路追踪 */
    private boolean enabled = true;

    /** 是否记录 LLM Prompt 内容到 Span (生产环境建议关闭以避免敏感信息泄漏) */
    private boolean logPromptContent = false;

    /** 是否记录 LLM 完整输出到 Span */
    private boolean logCompletionContent = false;

    /** Prompt 内容最大记录长度 (超出截断) */
    private int promptMaxLength = 500;

    /** Completion 内容最大记录长度 (超出截断) */
    private int completionMaxLength = 500;
}
