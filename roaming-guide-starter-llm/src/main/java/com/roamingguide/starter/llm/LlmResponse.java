package com.roamingguide.starter.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM 响应统一 DTO
 * 屏蔽不同厂商 SDK 的差异
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponse {

    /** 生成的文本内容 */
    private String content;

    /** 使用的模型 */
    private String model;

    /** 输入 Token 数 */
    private Integer promptTokens;

    /** 输出 Token 数 */
    private Integer completionTokens;

    /** 总 Token 数 */
    private Integer totalTokens;

    /** 是否成功 */
    private boolean success;

    /** 错误信息（失败时） */
    private String errorMessage;

    public static LlmResponse success(String content, String model,
                                      Integer promptTokens, Integer completionTokens) {
        return LlmResponse.builder()
                .content(content)
                .model(model)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(promptTokens + completionTokens)
                .success(true)
                .build();
    }

    public static LlmResponse failure(String errorMessage) {
        return LlmResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
