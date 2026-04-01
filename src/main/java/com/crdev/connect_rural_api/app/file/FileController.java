package com.crdev.connect_rural_api.app.file;

import com.crdev.connect_rural_api.app.file.dto.response.FileResponseDto;
import com.crdev.connect_rural_api.business.file.dto.FileDownloadPayload;
import com.crdev.connect_rural_api.business.file.usecases.DeleteFileUseCase;
import com.crdev.connect_rural_api.business.file.usecases.DownloadFileUseCase;
import com.crdev.connect_rural_api.business.file.usecases.GetFileMetadataUseCase;
import com.crdev.connect_rural_api.business.file.usecases.ListFilesUseCase;
import com.crdev.connect_rural_api.business.file.usecases.UploadFileUseCase;
import com.crdev.connect_rural_api.data.file.FileObjectEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    private final UploadFileUseCase uploadFileUseCase;
    private final ListFilesUseCase listFilesUseCase;
    private final GetFileMetadataUseCase getFileMetadataUseCase;
    private final DownloadFileUseCase downloadFileUseCase;
    private final DeleteFileUseCase deleteFileUseCase;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponseDto> upload(@RequestParam("file") MultipartFile file) {
        log.info("File upload requested: name={}, size={}", file.getOriginalFilename(), file.getSize());
        return ResponseEntity.status(201).body(uploadFileUseCase.execute(file));
    }

    @GetMapping
    public ResponseEntity<List<FileResponseDto>> list() {
        return ResponseEntity.ok(listFilesUseCase.execute());
    }

    @GetMapping("/{key}")
    public ResponseEntity<FileResponseDto> getMetadata(@PathVariable String key) {
        return ResponseEntity.ok(getFileMetadataUseCase.execute(key));
    }

    @GetMapping("/{key}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable String key) {
        FileDownloadPayload payload = downloadFileUseCase.execute(key);
        FileObjectEntity file = payload.file();

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.getOriginalName(), StandardCharsets.UTF_8)
                .build();

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString());

        if (file.getSize() != null) {
            builder.contentLength(file.getSize());
        }

        return builder.body(new InputStreamResource(payload.stream()));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        deleteFileUseCase.execute(key);
        return ResponseEntity.noContent().build();
    }
}
