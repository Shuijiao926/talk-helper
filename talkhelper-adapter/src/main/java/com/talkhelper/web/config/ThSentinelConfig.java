package com.talkhelper.web.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel配置 - 限流熔断
 */
@Slf4j
@Configuration
public class ThSentinelConfig {

    /**
     * 启用Sentinel注解支持
     */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    /**
     * 初始化限流规则
     */
    @PostConstruct
    public void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 规则1: 文件上传接口限流 - 每秒最多50个请求
        FlowRule uploadRule = new FlowRule();
        uploadRule.setResource("upload");
        uploadRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        uploadRule.setCount(50); // QPS限制
        uploadRule.setLimitApp("default");
        rules.add(uploadRule);

        // 规则2: 任务查询接口限流 - 每秒最多200个请求
        FlowRule queryRule = new FlowRule();
        queryRule.setResource("queryTask");
        queryRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        queryRule.setCount(200);
        queryRule.setLimitApp("default");
        rules.add(queryRule);

        // 规则3: 文本预处理服务限流 - 每秒最多30个任务
        FlowRule preprocessRule = new FlowRule();
        preprocessRule.setResource("textPreprocess");
        preprocessRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        preprocessRule.setCount(30);
        preprocessRule.setLimitApp("default");
        rules.add(preprocessRule);

        FlowRuleManager.loadRules(rules);
        log.info("Sentinel限流规则初始化完成: {} 条规则", rules.size());
    }
}
