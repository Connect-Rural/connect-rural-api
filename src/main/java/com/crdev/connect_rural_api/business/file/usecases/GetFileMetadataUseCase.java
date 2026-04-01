package com.crdev.connect_rural_api.business.file.usecases;

import com.crdev.connect_rural_api.app.file.dto.response.FileResponseDto;
import com.crdev.connect_rural_api.business.file.FileService;
import com.crdev.connect_rural_api.business.file.mapper.FileAppMapper;
import com.crdev.connect_rural_api.data.file.FileObjectEntity;
import com.crdev.connect_rural_api.data.file.FileStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetFileMetadataUseCase {
    private final FileService service;
    private final FileAppMapper mapper;

    public FileResponseDto execute(String key) {
        FileObjectEntity entity = service.getByKey(key);
        if (entity.getStatus() == FileStatus.DELETED) {
            throw new IllegalArgumentException("File not found");
        }
        return mapper.toResponse(entity);
    }
}
