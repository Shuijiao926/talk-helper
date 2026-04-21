package com.roamingguide.starter.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO存储配置属性
 */
@Data
@ConfigurationProperties(prefix = "roaming-guide.storage.minio")
public class MinioProperties {

    /**
     * MinIO端点地址
     */
    private String endpoint = "http://127.0.0.1:9000";

    /**
     * MinIO Access Key
     */
    private String accessKey = "minioadmin";

    /**
     * MinIO Secret Key
     */
    private String secretKey = "minioadmin";

    /**
     * 默认Bucket名称(可选)
     */
    private String bucketName = "Talkhelper";
}
