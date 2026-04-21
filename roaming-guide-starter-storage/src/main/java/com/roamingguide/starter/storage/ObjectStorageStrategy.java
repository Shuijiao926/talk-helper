package com.roamingguide.starter.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 对象存储策略接口
 * 支持多种OSS提供商(MinIO、阿里云OSS、AWS S3等)
 */
public interface ObjectStorageStrategy {

    /**
     * 获取存储类型名称
     */
    String getType();

    /**
     * 检查存储服务是否可用
     */
    boolean isAvailable();

    /**
     * 上传文件
     *
     * @param file       文件
     * @param bucketName 存储桶名称
     * @param objectKey  对象键(路径)
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String bucketName, String objectKey);

    /**
     * 上传字节数组
     *
     * @param data        文件数据
     * @param bucketName  存储桶名称
     * @param objectKey   对象键(路径)
     * @param contentType 内容类型
     * @return 文件访问URL
     */
    String uploadBytes(byte[] data, String bucketName, String objectKey, String contentType);

    /**
     * 下载文件
     *
     * @param bucketName 存储桶名称
     * @param objectKey  对象键(路径)
     * @return 文件字节数组
     */
    byte[] downloadFile(String bucketName, String objectKey);

    /**
     * 删除文件
     *
     * @param bucketName 存储桶名称
     * @param objectKey  对象键(路径)
     */
    void deleteFile(String bucketName, String objectKey);

    /**
     * 生成预签名URL(临时访问链接)
     *
     * @param bucketName    存储桶名称
     * @param objectKey     对象键(路径)
     * @param expireSeconds 过期时间(秒)
     * @return 预签名URL
     */
    String generatePresignedUrl(String bucketName, String objectKey, long expireSeconds);

    /**
     * 检查文件是否存在
     *
     * @param bucketName 存储桶名称
     * @param objectKey  对象键(路径)
     * @return 是否存在
     */
    boolean fileExists(String bucketName, String objectKey);
}
