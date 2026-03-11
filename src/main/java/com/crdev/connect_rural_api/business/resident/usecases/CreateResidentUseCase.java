package com.crdev.connect_rural_api.business.resident.usecases;

import com.crdev.connect_rural_api.app.resident.dto.request.CreateResidentDto;
import com.crdev.connect_rural_api.app.resident.dto.response.ResidentResponseDto;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.business.resident.mapper.ResidentAppMapper;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
public class CreateResidentUseCase {
    private final ResidentService service;
    private final ResidentAppMapper mapper;

    public ResidentResponseDto execute(String communityKey, CreateResidentDto dto){
        log.info("Creating resident: communityKey={}", communityKey);
        var residentEntity = new ResidentEntity(
                null,
                UUID.fromString(communityKey),
                dto.getFirstName(),
                dto.getLastName(),
                dto.getBirthDate(),
                dto.getPhoneNumber(),
                dto.getEmail(),
                dto.getAddress(),
                dto.getAddressReference(),
                dto.getJoinedAt(),
                true,
                null,
                null
        );

        ResidentEntity residentCreated = service.create(residentEntity);
        return mapper.toResponse(residentCreated);
    }
}
