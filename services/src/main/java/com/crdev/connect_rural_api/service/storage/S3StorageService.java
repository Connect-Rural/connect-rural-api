package com.crdev.connect_rural_api.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final String bucket;

    public S3StorageService(
            @Value("${app.storage.s3.endpoint:}") String endpoint,
            @Value("${app.storage.s3.region:us-east-1}") String region,
            @Value("${app.storage.s3.access-key}") String accessKey,
            @Value("${app.storage.s3.secret-key}") String secretKey,
            @Value( "${app.storage.s3.bucket}") String bucket,
            @Value("${app.storage.s3.path-style-access:true}") boolean pathStyleAccess) {

        this.bucket = bucket;

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
        Region awsRegion = Region.of(region);
        boolean hasEndpoint = endpoint != null && !endpoint.isBlank();
        URI endpointUri = hasEndpoint ? URI.create(endpoint) : null;

        // --- Construcción del S3Client ---
        S3ClientBuilder clientBuilder = S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(credentialsProvider);

        if (hasEndpoint) {
            clientBuilder.endpointOverride(endpointUri);
        }
        if (pathStyleAccess) {
            clientBuilder.forcePathStyle(true);
        }
        this.s3Client = clientBuilder.build();

        // --- Construcción del S3Presigner (para URLs prefirmadas) ---
        S3Presigner.Builder presignerBuilder = S3Presigner.builder()
                .region(awsRegion)
                .credentialsProvider(credentialsProvider);

        if (hasEndpoint) {
            presignerBuilder.endpointOverride(endpointUri);
        }
        // El presigner no soporta forcePathStyle directamente, pero al usar
        // el mismo endpoint compatible con S3, las URLs salen correctamente.
        this.presigner = presignerBuilder.build();
    }

    @Override
    public String getBucket() {
        return bucket;
    }

    @Override
    public String upload(MultipartFile file, String key) throws IOException {
        try (InputStream is = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(is, file.getSize()));
            return key;
        } catch (IOException e) {
            throw e;
        }
    }

    @Override
    public InputStream download(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return s3Client.getObject(request);
    }

    @Override
    public void delete(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.deleteObject(request);
    }

    @Override
    public String getUrl(String key) {
        // 1. Creas la petición base (esto ya lo tenías)
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // La URL expira en 10 min (ajústalo a tu gusto)
                .getObjectRequest(getObjectRequest)
                .build();

        // 3. Ahora sí, llamas a presignGetObject con el tipo correcto
        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toString();
    }
    @Bean
    public S3Client s3Client() {
        return s3Client;
    }
}