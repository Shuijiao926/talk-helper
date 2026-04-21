package com.roamingguide.starter.storage.minio;

import com.roamingguide.starter.storage.MinioProperties;
import com.roamingguide.starter.storage.ObjectStorageStrategy;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * MinIO对象存储实现
 */
@Slf4j
@RequiredArgsConstructor
public class MinioStorageStrategy implements ObjectStorageStrategy {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public String getType() {
        return "minio";
    }

    @Override
    public boolean isAvailable() {
        try {
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
            ensureBucketExists(bucketName);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

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
            ensureBucketExists(bucketName);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(new java.io.ByteArrayInputStream(data), data.length, -1)
                            .contentType(contentType != null ? contentType : "application/octet-stream")
                            .build()
            );

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

    private void ensureBucketExists(String bucketName) throws Exception {
        boolean found = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );
        if (!found) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            log.info("创建Bucket成功: {}", bucketName);
        }
    }

    private String generateFileUrl(String bucketName, String objectKey) {
        return String.format("%s/%s/%s",
                minioProperties.getEndpoint(),
                bucketName,
                objectKey);
    }
}
