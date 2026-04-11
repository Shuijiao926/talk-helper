package com.talkhelper.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM响应统一DTO
 * 屏蔽不同厂商SDK的差异
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThLlmResponse {

    /**
     * 生成的文本内容
     */
    private String content;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 输入Token数
     */
    private Integer promptTokens;

    /**
     * 输出Token数
     */
    private Integer completionTokens;

    /**
     * 总Token数
     */
    private Integer totalTokens;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息（失败时）
     */
    private String errorMessage;

    /**
     * 创建成功响应
     */
    public static ThLlmResponse success(String content, String model, 
                                        Integer promptTokens, Integer completionTokens) {
        return ThLlmResponse.builder()
                .content(content)
                .model(model)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(promptTokens + completionTokens)
                .success(true)
                .build();
    }

    /**
     * 创建失败响应
     */
    public static ThLlmResponse failure(String errorMessage) {
        return ThLlmResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
