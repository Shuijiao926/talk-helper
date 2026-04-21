package com.roamingguide.starter.llm.dashscope;

import com.dtflys.forest.exceptions.ForestRuntimeException;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.dtflys.forest.interceptor.Interceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DashScopeInterceptor implements Interceptor<DashScopeResponse> {

    @Override
    public boolean beforeExecute(ForestRequest request) {
        log.debug("DashScope请求: {} {}", request.getMethod(), request.getUrl());
        return true;
    }

    @Override
    public void onSuccess(DashScopeResponse data, ForestRequest request, ForestResponse response) {
        if (data != null && data.getUsage() != null) {
            log.info("DashScope调用成功, model={}, promptTokens={}, completionTokens={}, totalTokens={}",
                    data.getModel(),
                    data.getUsage().getPromptTokens(),
                    data.getUsage().getCompletionTokens(),
                    data.getUsage().getTotalTokens());
        } else {
            log.info("DashScope调用成功");
        }
    }

    @Override
    public void onError(ForestRuntimeException ex, ForestRequest request, ForestResponse response) {
        log.error("DashScope调用失败: status={}, body={}",
                response != null ? response.getStatusCode() : "N/A",
                response != null ? response.getContent() : "N/A",
                ex);
    }
}
