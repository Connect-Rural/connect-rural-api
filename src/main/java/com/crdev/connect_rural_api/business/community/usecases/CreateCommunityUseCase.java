package com.crdev.connect_rural_api.business.community.usecases;

import com.crdev.connect_rural_api.app.community.dto.request.CreateCommunityDto;
import com.crdev.connect_rural_api.app.community.dto.response.CommunityResponseDto;
import com.crdev.connect_rural_api.business.community.CommunityService;
import com.crdev.connect_rural_api.business.community.mapper.CommunityAppMapper;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CreateCommunityUseCase {
    private final CommunityService service;
    private final CommunityAppMapper mapper;

    public CommunityResponseDto execute(CreateCommunityDto dto) {
        var communityEntity = new CommunityEntity(
                null,
                dto.getName(),
                dto.getDescription(),
                dto.getLogoUrl(),
                null,
                null,
                null,
                null,
                dto.getSubscriptionPlan(),
                false,
                null,
                true,
                null,
                null
        );
        CommunityEntity communitySaved = service.create(communityEntity);
        return mapper.toResponse(communitySaved);
    }
}
