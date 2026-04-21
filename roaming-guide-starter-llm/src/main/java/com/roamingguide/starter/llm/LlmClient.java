package com.roamingguide.starter.llm;

/**
 * LLM 客户端接口（策略模式）
 * 支持多种大模型提供商：DashScope、OpenAI、Claude 等
 */
public interface LlmClient {

    /** 获取客户端类型名称 */
    String getType();

    /** 调用大模型 */
    LlmResponse chat(LlmRequest request);

    /** 检查客户端是否可用 */
    boolean isAvailable();

    /** 关闭客户端资源 */
    void shutdown();
}
