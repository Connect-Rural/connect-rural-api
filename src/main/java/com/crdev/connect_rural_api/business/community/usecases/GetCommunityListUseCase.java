package com.crdev.connect_rural_api.business.community.usecases;

import com.crdev.connect_rural_api.app.community.dto.response.CommunityAdminResponseDto;
import com.crdev.connect_rural_api.app.community.dto.response.CommunityResponseDto;
import com.crdev.connect_rural_api.business.community.CommunityService;
import com.crdev.connect_rural_api.business.community.mapper.CommunityAppMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class GetCommunityListUseCase {
    private final CommunityService service;
    private final CommunityAppMapper mapper;

    public List<CommunityAdminResponseDto> execute() {
        return service.getAll()
                .stream()
                .map(mapper::toAdminResponse)
                .collect(Collectors.toList());
    }
}
