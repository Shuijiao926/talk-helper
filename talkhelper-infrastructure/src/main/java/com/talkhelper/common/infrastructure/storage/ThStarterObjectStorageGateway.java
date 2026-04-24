package com.talkhelper.common.infrastructure.storage;

import com.roamingguide.starter.storage.ObjectStorageFactory;
import com.roamingguide.starter.storage.ObjectStorageStrategy;
import com.roamingguide.starter.storage.StorageProperties;
import com.talkhelper.common.domain.storage.ThObjectStorageGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;

@Component
@RequiredArgsConstructor
public class ThStarterObjectStorageGateway implements ThObjectStorageGateway {

    private final ObjectStorageFactory storageFactory;
    private final StorageProperties storageProperties;

    @Override
    public String getDefaultBucket() {
        return storageProperties.getDefaultBucket();
    }

    @Override
    public String uploadBytes(String objectKey, byte[] data, String contentType) {
        return uploadBytes(getDefaultBucket(), objectKey, data, contentType);
    }

    @Override
    public String uploadBytes(String bucketName, String objectKey, byte[] data, String contentType) {
        return storage().uploadBytes(data, bucketName, objectKey, contentType);
    }

    @Override
    public String uploadFile(String objectKey, MultipartFile file) {
        return uploadFile(getDefaultBucket(), objectKey, file);
    }

    @Override
    public String uploadFile(String bucketName, String objectKey, MultipartFile file) {
        return storage().uploadFile(file, bucketName, objectKey);
    }

    @Override
    public byte[] download(String objectKey) {
        return download(getDefaultBucket(), objectKey);
    }

    @Override
    public byte[] download(String bucketName, String objectKey) {
        return storage().downloadFile(bucketName, objectKey);
    }

    @Override
    public byte[] downloadByUrl(String fileUrl) {
        String[] bucketAndKey = parseBucketAndKey(fileUrl);
        return download(bucketAndKey[0], bucketAndKey[1]);
    }

    private ObjectStorageStrategy storage() {
        return storageFactory.getActiveStorage();
    }

    private String[] parseBucketAndKey(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            int firstSlash = path.indexOf('/');
            if (firstSlash == -1) {
                throw new IllegalArgumentException("Invalid object storage URL: " + fileUrl);
            }

            return new String[]{
                    path.substring(0, firstSlash),
                    path.substring(firstSlash + 1)
            };
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse object storage URL: " + fileUrl, e);
        }
    }
}
