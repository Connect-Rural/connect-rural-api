package com.crdev.connect_rural_api.business.file.usecases;

import com.crdev.connect_rural_api.app.file.dto.response.FileResponseDto;
import com.crdev.connect_rural_api.business.file.FileService;
import com.crdev.connect_rural_api.business.file.FileStorageService;
import com.crdev.connect_rural_api.business.file.mapper.FileAppMapper;
import com.crdev.connect_rural_api.data.file.FileObjectEntity;
import com.crdev.connect_rural_api.data.file.FileStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadFileUseCase {
    private final FileService service;
    private final FileStorageService storageService;
    private final FileAppMapper mapper;

    public FileResponseDto execute(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "file";
        }

        String safeName = sanitizeFileName(originalName);
        String objectKey = UUID.randomUUID() + "_" + safeName;
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        try (InputStream inputStream = file.getInputStream()) {
            storageService.upload(objectKey, inputStream, file.getSize(), contentType);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to read upload", ex);
        }

        FileObjectEntity entity = new FileObjectEntity(
                null,
                storageService.getBucket(),
                objectKey,
                originalName,
                contentType,
                file.getSize(),
                FileStatus.READY,
                null,
                null
        );

        FileObjectEntity saved = service.create(entity);
        log.info("File uploaded: key={}, objectKey={}", saved.getKey(), objectKey);
        return mapper.toResponse(saved);
    }

    private String sanitizeFileName(String name) {
        String sanitized = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (sanitized.isBlank()) {
            return "file";
        }
        return sanitized;
    }
}
