package com.roamingguide.starter.llm.dashscope;

import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Body;
import com.dtflys.forest.annotation.ForestClient;
import com.dtflys.forest.annotation.Header;
import com.dtflys.forest.annotation.Post;

@BaseRequest(
        baseURL = "${dashscope.baseUrl}",
        contentType = "application/json",
        interceptor = DashScopeInterceptor.class
)
@ForestClient
public interface DashScopeApi {

    @Post("/chat/completions")
    DashScopeResponse chatCompletion(
            @Header("Authorization") String authorization,
            @Body DashScopeRequest request
    );
}
