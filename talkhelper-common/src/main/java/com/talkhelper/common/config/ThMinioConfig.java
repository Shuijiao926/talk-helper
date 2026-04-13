package com.talkhelper.common.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO存储配置
 * 注意:此配置类专门用于MinIO策略,与其他存储策略隔离
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "talkhelper.storage.minio")
public class ThMinioConfig {

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

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
