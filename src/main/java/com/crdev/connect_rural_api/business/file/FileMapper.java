package com.crdev.connect_rural_api.business.file;

import com.crdev.connect_rural_api.app.file.dto.FileResponse;
import com.crdev.connect_rural_api.data.file.FileObjectEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileMapper {

    public FileResponse toResponse(FileObjectEntity entity) {
        if (entity == null) return null;
        return FileResponse.builder()
                .key(entity.getKey().toString())
                .originalName(entity.getOriginalName())
                .contentType(entity.getContentType())
                .size(entity.getSize())
                .status(entity.getStatus().name())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<FileResponse> toResponseList(List<FileObjectEntity> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
