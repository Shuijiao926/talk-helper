package com.talkhelper.common.domain.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ThObjectStorageGateway {

    String getDefaultBucket();

    String uploadBytes(String objectKey, byte[] data, String contentType);

    String uploadBytes(String bucketName, String objectKey, byte[] data, String contentType);

    String uploadFile(String objectKey, MultipartFile file);

    String uploadFile(String bucketName, String objectKey, MultipartFile file);

    byte[] download(String objectKey);

    byte[] download(String bucketName, String objectKey);

    byte[] downloadByUrl(String fileUrl);
}
