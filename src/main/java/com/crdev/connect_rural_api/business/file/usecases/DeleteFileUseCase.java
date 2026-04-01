package com.crdev.connect_rural_api.business.file.usecases;

import com.crdev.connect_rural_api.business.file.FileService;
import com.crdev.connect_rural_api.business.file.FileStorageService;
import com.crdev.connect_rural_api.data.file.FileObjectEntity;
import com.crdev.connect_rural_api.data.file.FileStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteFileUseCase {
    private final FileService service;
    private final FileStorageService storageService;

    public void execute(String key) {
        FileObjectEntity entity = service.getByKey(key);
        if (entity.getStatus() == FileStatus.DELETED) {
            return;
        }
        storageService.delete(entity.getObjectKey());
        entity.setStatus(FileStatus.DELETED);
        service.update(entity);
        log.info("File deleted: key={}", key);
    }
}
