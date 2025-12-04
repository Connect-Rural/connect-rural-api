package com.crdev.connect_rural_api.business.community.usecases;

import com.crdev.connect_rural_api.app.community.dto.request.CreateCommunityDto;
import com.crdev.connect_rural_api.app.community.dto.response.CommunityAdminResponseDto;
import com.crdev.connect_rural_api.business.community.CommunityService;
import com.crdev.connect_rural_api.business.community.mapper.CommunityAppMapper;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class UpdateCommunityUseCase {
    private final CommunityService service;
    private final CommunityAppMapper mapper;

    @Transactional
    public CommunityAdminResponseDto execute(String key, CreateCommunityDto dto) {
        CommunityEntity existing = service.getBykey(key);
        mapper.updateFromDto(dto, existing);
        var saved = service.update(existing);
        return mapper.toAdminResponse(saved);
    }

}
