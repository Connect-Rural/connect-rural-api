package com.crdev.connect_rural_api.business.resident.usecases;

import com.crdev.connect_rural_api.app.resident.dto.request.CreateResidentDto;
import com.crdev.connect_rural_api.app.resident.dto.response.ResidentResponseDto;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.business.resident.mapper.ResidentAppMapper;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
@Slf4j
public class UpdateResidentUseCase {
    private final ResidentService service;
    private final ResidentAppMapper mapper;

    @Transactional
    public ResidentResponseDto execute(String communityKey, String residentKey, CreateResidentDto dto) {
        log.info("Updating resident: communityKey={}, residentKey={}", communityKey, residentKey);
        ResidentEntity existing = service.getByKey(communityKey, residentKey);
        mapper.updateEntityFromDto(dto, existing);
        var saved = service.update(existing);
        return mapper.toResponse(saved);
    }

}
