package com.crdev.connect_rural_api.business.community.usecases;


import com.crdev.connect_rural_api.app.community.dto.CommunityPaginatedResponseDto;
import com.crdev.connect_rural_api.app.community.dto.request.CommunityFilterRequestDto;
import com.crdev.connect_rural_api.app.community.mapper.CommunityAppMapper;
import com.crdev.connect_rural_api.business.community.CommunityService;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GetCommunityPaginatedUseCase {
    private final CommunityService service;
    private final CommunityAppMapper mapper;


    public CommunityPaginatedResponseDto execute(CommunityFilterRequestDto filter) {

        Page<CommunityEntity> result = service.getAllPaginatedAndFiltered(filter.getKeyword(),
                filter.getPage(),
                filter.getSize());

        return new CommunityPaginatedResponseDto(
                mapper.toResponseList(result.getContent()),
                filter.getPage(),
                filter.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}
