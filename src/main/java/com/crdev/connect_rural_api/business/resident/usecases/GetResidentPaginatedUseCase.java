package com.crdev.connect_rural_api.business.resident.usecases;


import com.crdev.connect_rural_api.app.community.dto.request.CommunityFilterDto;
import com.crdev.connect_rural_api.app.community.dto.response.CommunityPaginatedResponseDto;
import com.crdev.connect_rural_api.app.resident.dto.request.ResidentFilterDto;
import com.crdev.connect_rural_api.app.resident.dto.response.ResidentPaginatedResponseDto;
import com.crdev.connect_rural_api.business.community.CommunityService;
import com.crdev.connect_rural_api.business.community.mapper.CommunityAppMapper;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.business.resident.mapper.ResidentAppMapper;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GetResidentPaginatedUseCase {
    private final ResidentService service;
    private final ResidentAppMapper mapper;


    public ResidentPaginatedResponseDto execute(String communityKey, ResidentFilterDto filter) {

        Page<ResidentEntity> result = service.getAllPaginatedAndFiltered(
                communityKey,
                filter.getKeyword(),
                filter.getPage(),
                filter.getSize());

        return new ResidentPaginatedResponseDto(
                mapper.toResponseList(result.getContent()),
                filter.getPage(),
                filter.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}
