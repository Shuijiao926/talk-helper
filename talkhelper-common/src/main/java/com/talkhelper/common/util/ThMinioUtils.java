package com.talkhelper.common.util;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * MinIO对象存储工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThMinioUtils {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket-name}")
    private String bucketName;

    private MinioClient minioClient;

    /**
     * 初始化MinIO客户端
     */
    private MinioClient getClient() {
        if (minioClient == null) {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
        }
        return minioClient;
    }

    /**
     * 上传文件到MinIO
     *
     * @param file 文件
     * @param objectName 对象名称（路径）
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String objectName) {
        try {
            MinioClient client = getClient();
            
            // 确保bucket存在
            boolean found = client.bucketExists(io.minio.BucketExistsArgs.builder()
                    .bucket(bucketName).build());
            if (!found) {
                client.makeBucket(io.minio.MakeBucketArgs.builder()
                        .bucket(bucketName).build());
            }

            // 上传文件
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            String fileUrl = endpoint + "/" + bucketName + "/" + objectName;
            log.info("文件上传到MinIO成功: {}", fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("文件上传到MinIO失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从MinIO下载文件
     *
     * @param objectName 对象名称
     * @return 文件流
     */
    public InputStream downloadFile(String objectName) {
        try {
            MinioClient client = getClient();
            return client.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("从MinIO下载文件失败: {}", objectName, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除MinIO中的文件
     *
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) {
        try {
            MinioClient client = getClient();
            client.removeObject(io.minio.RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            log.info("文件已从MinIO删除: {}", objectName);
        } catch (Exception e) {
            log.error("从MinIO删除文件失败: {}", objectName, e);
        }
    }
}

