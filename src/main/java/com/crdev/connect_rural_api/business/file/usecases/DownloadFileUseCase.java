package com.crdev.connect_rural_api.business.file.usecases;

import com.crdev.connect_rural_api.business.file.FileService;
import com.crdev.connect_rural_api.business.file.FileStorageService;
import com.crdev.connect_rural_api.business.file.dto.FileDownloadPayload;
import com.crdev.connect_rural_api.data.file.FileObjectEntity;
import com.crdev.connect_rural_api.data.file.FileStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DownloadFileUseCase {
    private final FileService service;
    private final FileStorageService storageService;

    public FileDownloadPayload execute(String key) {
        FileObjectEntity entity = service.getByKey(key);
        if (entity.getStatus() == FileStatus.DELETED) {
            throw new IllegalArgumentException("File not found");
        }
        return new FileDownloadPayload(entity, storageService.download(entity.getObjectKey()));
    }
}
