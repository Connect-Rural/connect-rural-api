package com.crdev.connect_rural_api.data.file;

import com.crdev.connect_rural_api.business.file.FileGateway;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileGatewayImpl implements FileGateway {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("MinIO bucket created: {}", bucket);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to initialize MinIO bucket: " + bucket, ex);
        }
    }

    @Override
    public String getBucket() {
        return bucket;
    }

    @Override
    public void upload(String objectKey, InputStream inputStream, long size, String contentType) {
        try {
            ensureBucketExists();
            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(inputStream, size, -1);
            if (contentType != null && !contentType.isBlank()) {
                builder.contentType(contentType);
            }
            minioClient.putObject(builder.build());
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to upload file to storage", ex);
        }
    }

    @Override
    public InputStream download(String objectKey) {
        try {
            ensureBucketExists();
            return minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucket).object(objectKey).build()
            );
        } catch (Exception ex) {
            throw new IllegalArgumentException("File not found in storage", ex);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            ensureBucketExists();
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build()
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete file from storage", ex);
        }
    }
}
