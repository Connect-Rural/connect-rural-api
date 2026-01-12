package com.crdev.connect_rural_api.business.cooperation.usecases;


import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationResponseDto;
import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.mapper.CooperationAppMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GetCooperationByKeyUseCase {
    private final CooperationService service;
    private final CooperationAppMapper mapper;

    public CooperationResponseDto execute(String communityKey, String residentKey){
        var cooperationEntity = service.getByKey(communityKey,residentKey);
        return mapper.toResponseDto(cooperationEntity);

    }
}
