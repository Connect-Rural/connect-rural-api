package com.crdev.connect_rural_api.business.file.mapper;

import com.crdev.connect_rural_api.app.file.dto.response.FileResponseDto;
import com.crdev.connect_rural_api.data.file.FileObjectEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileAppMapper {

    public FileResponseDto toResponse(FileObjectEntity entity) {
        if (entity == null) return null;
        return new FileResponseDto(
                entity.getKey().toString(),
                entity.getOriginalName(),
                entity.getContentType(),
                entity.getSize(),
                entity.getStatus().name(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public List<FileResponseDto> toResponseList(List<FileObjectEntity> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
