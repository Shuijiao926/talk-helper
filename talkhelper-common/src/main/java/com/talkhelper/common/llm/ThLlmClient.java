package com.talkhelper.common.llm;

import com.talkhelper.common.dto.ThLlmRequest;
import com.talkhelper.common.dto.ThLlmResponse;

/**
 * LLM客户端接口（策略模式）
 * 支持多种大模型提供商：DashScope、OpenAI、Claude等
 */
public interface ThLlmClient {

    /**
     * 获取客户端类型名称
     */
    String getType();

    /**
     * 调用大模型
     *
     * @param request 请求参数
     * @return 响应结果
     */
    ThLlmResponse chat(ThLlmRequest request);

    /**
     * 检查客户端是否可用
     */
    boolean isAvailable();

    /**
     * 关闭客户端资源
     */
    void shutdown();
}
