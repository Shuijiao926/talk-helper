package com.talkhelper;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * TalkHelper 主启动类
 * AI驱动的播客生成平台
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class TalkHelperApplication {

    public static void main(String[] args) {
        SpringApplication.run(TalkHelperApplication.class, args);
    }
}
