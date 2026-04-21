package com.roamingguide.starter.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 对象存储通用配置属性
 */
@Data
@ConfigurationProperties(prefix = "roaming-guide.storage")
public class StorageProperties {

    /**
     * 默认Bucket名称
     */
    private String defaultBucket = "talkhelper";

    /**
     * 激活的存储类型(minio/local/s3)
     */
    private String activeType = "minio";
}
