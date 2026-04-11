package com.talkhelper.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * LLM请求统一DTO
 * 屏蔽不同厂商SDK的差异
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThLlmRequest {

    /**
     * 提示词模板
     */
    private String promptTemplate;

    /**
     * 模板变量
     */
    private Map<String, String> variables;

    /**
     * 模型名称（可选，覆盖默认配置）
     */
    private String model;

    /**
     * 温度参数 (0-1)
     */
    private Double temperature;

    /**
     * 最大输出长度
     */
    private Integer maxTokens;

    /**
     * 最终渲染后的Prompt（由服务层生成）
     */
    private String finalPrompt;
}
