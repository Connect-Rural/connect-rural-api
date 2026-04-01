package com.crdev.connect_rural_api.business.file;

import com.crdev.connect_rural_api.data.file.FileObjectEntity;
import com.crdev.connect_rural_api.data.file.FileObjectRepository;
import com.crdev.connect_rural_api.data.file.FileStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileObjectRepository repository;

    public FileObjectEntity create(FileObjectEntity entity) {
        return repository.save(entity);
    }

    public FileObjectEntity update(FileObjectEntity entity) {
        return repository.save(entity);
    }

    public FileObjectEntity getByKey(String key) {
        return repository.findById(UUID.fromString(key))
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
    }

    public List<FileObjectEntity> listActive() {
        return repository.findAllByStatusNotOrderByCreatedAtDesc(FileStatus.DELETED);
    }
}
