package com.crdev.connect_rural_api.business.cooperation.usecases;

import com.crdev.connect_rural_api.app.cooperation.dto.request.CreateCooperationRequestDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationSummaryResponseDto;
import com.crdev.connect_rural_api.app.resident.dto.request.CreateResidentDto;
import com.crdev.connect_rural_api.app.resident.dto.response.ResidentResponseDto;
import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.enums.CooperationAssignmentType;
import com.crdev.connect_rural_api.business.cooperation.mapper.CooperationAppMapper;
import com.crdev.connect_rural_api.business.cooperationResident.CooperationResidentService;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.business.resident.mapper.ResidentAppMapper;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@AllArgsConstructor
public class UpdateCooperationUseCase {
    private final CooperationService service;
    private final CooperationResidentService cooperationResidentService;
    private final ResidentService residentService;
    private final CooperationAppMapper mapper;

    @Transactional
    public CooperationSummaryResponseDto execute(String communityKey, String cooperationKey, CreateCooperationRequestDto dto) {
        CooperationEntity existing = service.getByKey(communityKey, cooperationKey);
        mapper.updateEntityFromDto(dto, existing);


        List<String> assignedResidents = List.of();

        if(dto.getAssignmentType() != null && dto.getAssignmentType().equals(CooperationAssignmentType.INDIVIDUAL)){
            assignedResidents = dto.getAssignedResidentKeys();
        } else {
            List<ResidentEntity> residentsInCommunity = residentService.listByCommunity(
                    communityKey
            );

            if (dto.getAssignmentType().equals(CooperationAssignmentType.ALL)){
                assignedResidents = residentsInCommunity.stream()
                        .map(resident -> resident.getKey().toString())
                        .toList();
            }
            else if (dto.getAssignmentType().equals(CooperationAssignmentType.ALL_EXCEPT)){
                assignedResidents = residentsInCommunity.stream()
                        .filter(resident -> !dto.getExcludedResidentKeys().contains(resident.getKey().toString()))
                        .map(resident -> resident.getKey().toString())
                        .toList();
            }

        }

        cooperationResidentService.assignResidentsToCooperation(
                cooperationKey,
                assignedResidents
        );

        var saved = service.update(existing);
        return mapper.toResponseSummaryDto(saved);
    }

}
