package com.crdev.connect_rural_api.business.file.usecases;

import com.crdev.connect_rural_api.app.file.dto.response.FileResponseDto;
import com.crdev.connect_rural_api.business.file.FileService;
import com.crdev.connect_rural_api.business.file.mapper.FileAppMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListFilesUseCase {
    private final FileService service;
    private final FileAppMapper mapper;

    public List<FileResponseDto> execute() {
        return mapper.toResponseList(service.listActive());
    }
}
