package com.crdev.connect_rural_api.data.file;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileJpaRepository extends JpaRepository<FileObjectEntity, UUID> {

    List<FileObjectEntity> findAllByStatusNotOrderByCreatedAtDesc(FileStatus status);
}
