package com.crdev.connect_rural_api.business.community.usecases;


import com.crdev.connect_rural_api.app.community.dto.response.CommunityPaginatedResponseDto;
import com.crdev.connect_rural_api.app.community.dto.request.CommunityFilterDto;
import com.crdev.connect_rural_api.business.community.CommunityService;
import com.crdev.connect_rural_api.business.community.mapper.CommunityAppMapper;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GetCommunityPaginatedUseCase {
    private final CommunityService service;
    private final CommunityAppMapper mapper;


    public CommunityPaginatedResponseDto execute(CommunityFilterDto filter) {

        Page<CommunityEntity> result = service.getAllPaginatedAndFiltered(filter.getKeyword(),
                filter.getPage(),
                filter.getSize());

        return new CommunityPaginatedResponseDto(
                mapper.toAdminResponseList(result.getContent()),
                filter.getPage(),
                filter.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}
