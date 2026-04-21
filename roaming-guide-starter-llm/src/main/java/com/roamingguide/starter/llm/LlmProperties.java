package com.roamingguide.starter.llm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LLM 配置属性
 */
@Data
@ConfigurationProperties(prefix = "roaming-guide.llm")
public class LlmProperties {

    /** LLM 提供商（dashscope / openai / claude） */
    private String provider = "dashscope";
}
