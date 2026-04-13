package com.talkhelper.common.storage;

import com.talkhelper.common.config.ThMinioConfig;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * MinIO对象存储实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThMinioStorageStrategy implements ThObjectStorageStrategy {

    private final MinioClient minioClient;
    private final ThMinioConfig minioConfig;

    @Override
    public String getType() {
        return "minio";
    }

    @Override
    public boolean isAvailable() {
        try {
            // 尝试列出bucket来检查连接
            minioClient.listBuckets();
            return true;
        } catch (Exception e) {
            log.debug("MinIO不可用: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String bucketName, String objectKey) {
        try {
            // 确保bucket存在
            ensureBucketExists(bucketName);

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 生成访问URL
            String url = generateFileUrl(bucketName, objectKey);
            log.info("文件上传成功: bucket={}, key={}, url={}", bucketName, objectKey, url);
            return url;

        } catch (Exception e) {
            log.error("文件上传失败: bucket={}, key={}", bucketName, objectKey, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadBytes(byte[] data, String bucketName, String objectKey, String contentType) {
        try {
            // 确保bucket存在
            ensureBucketExists(bucketName);

            // 上传字节数组
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(new java.io.ByteArrayInputStream(data), data.length, -1)
                            .contentType(contentType != null ? contentType : "application/octet-stream")
                            .build()
            );

            // 生成访问URL
            String url = generateFileUrl(bucketName, objectKey);
            log.info("字节数据上传成功: bucket={}, key={}, url={}", bucketName, objectKey, url);
            return url;

        } catch (Exception e) {
            log.error("字节数据上传失败: bucket={}, key={}", bucketName, objectKey, e);
            throw new RuntimeException("字节数据上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadFile(String bucketName, String objectKey) {
        try {
            var stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );

            byte[] data = stream.readAllBytes();
            stream.close();

            log.debug("文件下载成功: bucket={}, key={}, size={}", bucketName, objectKey, data.length);
            return data;

        } catch (Exception e) {
            log.error("文件下载失败: bucket={}, key={}", bucketName, objectKey, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String bucketName, String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
            log.info("文件删除成功: bucket={}, key={}", bucketName, objectKey);

        } catch (Exception e) {
            log.error("文件删除失败: bucket={}, key={}", bucketName, objectKey, e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedUrl(String bucketName, String objectKey, long expireSeconds) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry((int) expireSeconds)
                            .build()
            );
            log.debug("生成预签名URL: bucket={}, key={}, expire={}s", bucketName, objectKey, expireSeconds);
            return url;

        } catch (Exception e) {
            log.error("生成预签名URL失败: bucket={}, key={}", bucketName, objectKey, e);
            throw new RuntimeException("生成预签名URL失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean fileExists(String bucketName, String objectKey) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 确保Bucket存在
     */
    private void ensureBucketExists(String bucketName) throws Exception {
        boolean found = minioClient.bucketExists(
                io.minio.BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );
        if (!found) {
            minioClient.makeBucket(
                    io.minio.MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            log.info("创建Bucket成功: {}", bucketName);
        }
    }

    /**
     * 生成文件访问URL
     */
    private String generateFileUrl(String bucketName, String objectKey) {
        // 这里假设MinIO配置了公开访问,直接返回URL
        // 如果需要预签名URL,调用generatePresignedUrl方法
        return String.format("%s/%s/%s", 
                getMinioEndpoint(), 
                bucketName, 
                objectKey);
    }

    /**
     * 获取MinIO端点(从配置中读取)
     */
    private String getMinioEndpoint() {
        return minioConfig.getEndpoint();
    }
}
