package com.talkhelper.common.llm.dashscope;

import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Body;
import com.dtflys.forest.annotation.ForestClient;
import com.dtflys.forest.annotation.Header;
import com.dtflys.forest.annotation.Post;

@BaseRequest(
        baseURL = "${dashscope.base-url}",
        contentType = "application/json",
        interceptor = ThDashScopeInterceptor.class
)
@ForestClient
public interface ThDashScopeApi {

    @Post("/chat/completions")
    ThDashScopeResponse chatCompletion(
            @Header("Authorization") String authorization,
            @Body ThDashScopeRequest request
    );
}
