package com.crdev.connect_rural_api.app.file;

import com.crdev.connect_rural_api.app.file.dto.FileResponse;
import com.crdev.connect_rural_api.business.file.FileService;
import com.crdev.connect_rural_api.business.file.dto.FileDownloadPayload;
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

    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> upload(@RequestParam("file") MultipartFile file) {
        log.info("File upload requested: name={}, size={}", file.getOriginalFilename(), file.getSize());
        return ResponseEntity.status(201).body(fileService.upload(file));
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> list() {
        return ResponseEntity.ok(fileService.list());
    }

    @GetMapping("/{key}")
    public ResponseEntity<FileResponse> getMetadata(@PathVariable String key) {
        return ResponseEntity.ok(fileService.getMetadata(key));
    }

    @GetMapping("/{key}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable String key) {
        FileDownloadPayload payload = fileService.download(key);
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

        if (file.getSize() != null) builder.contentLength(file.getSize());
        return builder.body(new InputStreamResource(payload.stream()));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        fileService.delete(key);
        return ResponseEntity.noContent().build();
    }
}
