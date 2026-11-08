package com.crdev.connect_rural_api.data.file;

import com.crdev.connect_rural_api.business.file.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FileRepositoryImpl implements FileRepository {

    private final FileJpaRepository jpaRepository;

    @Override
    public FileObjectEntity save(FileObjectEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<FileObjectEntity> findById(UUID key) {
        return jpaRepository.findById(key);
    }

    @Override
    public List<FileObjectEntity> findAllByStatusNotOrderByCreatedAtDesc(FileStatus status) {
        return jpaRepository.findAllByStatusNotOrderByCreatedAtDesc(status);
    }
}
