package com.crdev.connect_rural_api.business.file;

import com.crdev.connect_rural_api.data.file.FileObjectEntity;
import com.crdev.connect_rural_api.data.file.FileStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepository {
    FileObjectEntity save(FileObjectEntity entity);
    Optional<FileObjectEntity> findById(UUID key);
    List<FileObjectEntity> findAllByStatusNotOrderByCreatedAtDesc(FileStatus status);
}
