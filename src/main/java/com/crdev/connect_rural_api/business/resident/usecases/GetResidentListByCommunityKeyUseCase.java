package com.crdev.connect_rural_api.business.resident.usecases;


import com.crdev.connect_rural_api.app.resident.dto.response.ResidentResponseDto;
import com.crdev.connect_rural_api.business.community.CommunityService;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.business.resident.mapper.ResidentAppMapper;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class GetResidentListByCommunityKeyUseCase {
    private final ResidentService residentService;
    private final CommunityService communityService;
    private final ResidentAppMapper mapper;

    @Transactional
    public List<ResidentResponseDto> execute(String communityKey) {
        CommunityEntity community = communityService.getBykey(communityKey);
        return residentService.listByCommunity(community.getKey().toString())
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}
