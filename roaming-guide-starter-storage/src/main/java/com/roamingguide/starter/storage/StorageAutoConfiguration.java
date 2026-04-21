package com.roamingguide.starter.storage;

import com.roamingguide.starter.storage.minio.MinioStorageStrategy;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 对象存储自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties({StorageProperties.class, MinioProperties.class})
public class StorageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(MinioClient.class)
    @ConditionalOnProperty(prefix = "roaming-guide.storage.minio", name = "endpoint")
    public MinioClient minioClient(MinioProperties minioProperties) {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(MinioStorageStrategy.class)
    @ConditionalOnClass(MinioClient.class)
    @ConditionalOnProperty(prefix = "roaming-guide.storage.minio", name = "endpoint")
    public MinioStorageStrategy minioStorageStrategy(MinioClient minioClient, MinioProperties minioProperties) {
        return new MinioStorageStrategy(minioClient, minioProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectStorageFactory objectStorageFactory(List<ObjectStorageStrategy> strategies,
                                                     StorageProperties storageProperties) {
        return new ObjectStorageFactory(strategies, storageProperties);
    }
}
