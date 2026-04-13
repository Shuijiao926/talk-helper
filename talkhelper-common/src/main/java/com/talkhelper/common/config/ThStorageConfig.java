package com.talkhelper.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 对象存储通用配置
 * 注意:具体存储策略的配置由各自的Strategy管理
 */
@Data
@Component
@ConfigurationProperties(prefix = "talkhelper.storage")
public class ThStorageConfig {

    /**
     * 默认Bucket名称
     */
    private String defaultBucket = "talkhelper";

    /**
     * 激活的存储类型(minio/local/s3)
     */
    private String activeType = "minio";
}
