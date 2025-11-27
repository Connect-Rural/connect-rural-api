package com.crdev.connect_rural_api.business.community.usecases;

import com.crdev.connect_rural_api.app.community.dto.CommunityResponseDto;
import com.crdev.connect_rural_api.app.community.mapper.CommunityAppMapper;
import com.crdev.connect_rural_api.business.community.CommunityService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class GetCommunityListUseCase {
    private final CommunityService service;
    private final CommunityAppMapper mapper;

    public List<CommunityResponseDto> execute() {
        return service.getAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}
