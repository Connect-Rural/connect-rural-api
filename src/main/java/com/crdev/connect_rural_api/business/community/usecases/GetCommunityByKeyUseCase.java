package com.crdev.connect_rural_api.business.community.usecases;

import com.crdev.connect_rural_api.app.community.dto.response.CommunityAdminResponseDto;
import com.crdev.connect_rural_api.business.community.CommunityService;
import com.crdev.connect_rural_api.business.community.mapper.CommunityAppMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCommunityByKeyUseCase {

    private final CommunityService service;
    private final CommunityAppMapper mapper;

    public CommunityAdminResponseDto execute(String key) {
        var entity = service.getBykey(key);
        return mapper.toAdminResponse(entity);
    }
}