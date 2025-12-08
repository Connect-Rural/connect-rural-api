package com.crdev.connect_rural_api.business.resident.usecases;

import com.crdev.connect_rural_api.app.resident.dto.response.ResidentDetailResponseDto;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.business.resident.mapper.ResidentAppMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GetResidentByKeyUseCase {
    private final ResidentService service;
    private final ResidentAppMapper mapper;

    public ResidentDetailResponseDto execute(String communityKey, String residentKey){
        var residentEntity = service.getByKey(communityKey,residentKey);
        return mapper.toResponseDetail(residentEntity);

    }
}
