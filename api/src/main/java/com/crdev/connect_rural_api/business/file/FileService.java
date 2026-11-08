package com.crdev.connect_rural_api.business.file;

import com.crdev.connect_rural_api.app.file.dto.FileResponse;
import com.crdev.connect_rural_api.business.file.dto.FileDownloadPayload;
import com.crdev.connect_rural_api.data.file.FileObjectEntity;
import com.crdev.connect_rural_api.data.file.FileStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final FileRepository fileRepository;
    private final FileGateway fileGateway;
    private final FileMapper mapper;

    public FileResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("File is empty");

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) originalName = "file";

        String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (safeName.isBlank()) safeName = "file";
        String objectKey = UUID.randomUUID() + "_" + safeName;

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        try (InputStream is = file.getInputStream()) {
            fileGateway.upload(objectKey, is, file.getSize(), contentType);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to read upload", ex);
        }

        FileObjectEntity entity = new FileObjectEntity(
                null, fileGateway.getBucket(), objectKey, originalName, contentType,
                file.getSize(), FileStatus.READY, null, null
        );
        FileObjectEntity saved = fileRepository.save(entity);
        log.info("File uploaded: key={}, objectKey={}", saved.getKey(), objectKey);
        return mapper.toResponse(saved);
    }

    public List<FileResponse> list() {
        return mapper.toResponseList(fileRepository.findAllByStatusNotOrderByCreatedAtDesc(FileStatus.DELETED));
    }

    public FileResponse getMetadata(String key) {
        FileObjectEntity entity = findEntity(key);
        if (entity.getStatus() == FileStatus.DELETED) throw new IllegalArgumentException("File not found");
        return mapper.toResponse(entity);
    }

    public FileDownloadPayload download(String key) {
        FileObjectEntity entity = findEntity(key);
        if (entity.getStatus() == FileStatus.DELETED) throw new IllegalArgumentException("File not found");
        return new FileDownloadPayload(entity, fileGateway.download(entity.getObjectKey()));
    }

    public void delete(String key) {
        FileObjectEntity entity = findEntity(key);
        if (entity.getStatus() == FileStatus.DELETED) return;
        fileGateway.delete(entity.getObjectKey());
        entity.setStatus(FileStatus.DELETED);
        fileRepository.save(entity);
        log.info("File deleted: key={}", key);
    }

    private FileObjectEntity findEntity(String key) {
        return fileRepository.findById(UUID.fromString(key))
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
    }
}
