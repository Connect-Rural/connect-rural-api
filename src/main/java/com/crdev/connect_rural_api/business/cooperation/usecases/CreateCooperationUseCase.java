package com.crdev.connect_rural_api.business.cooperation.usecases;

import com.crdev.connect_rural_api.app.cooperation.dto.request.CreateCooperationRequestDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationSummaryResponseDto;
import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.enums.CooperationAssignmentType;
import com.crdev.connect_rural_api.business.cooperation.mapper.CooperationAppMapper;
import com.crdev.connect_rural_api.business.cooperationResident.CooperationResidentService;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;

import static java.util.UUID.fromString;

@Component
@AllArgsConstructor
public class CreateCooperationUseCase {
    private final CooperationService service;
    private final CooperationResidentService cooperationResidentService;
    private final ResidentService residentService;
    private final CooperationAppMapper mapper;

    @Transactional
    public CooperationSummaryResponseDto execute(String communityKey, CreateCooperationRequestDto dto){
        var cooperationEntity = new CooperationEntity(
                null,
                fromString(communityKey),
                dto.getName(),
                dto.getDescription(),
                dto.getBaseAmount(),
                dto.getStartDate(),
                dto.getDueDate(),
                dto.getHasLateFee(),
                dto.getLateFeeAmount(),
                dto.getLateFeePeriodicity(),
                dto.getAssignmentType().toString(),
                dto.getStatus(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        CooperationEntity cooperationCreated = service.create(cooperationEntity);

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
                cooperationCreated.getKey().toString(),
                assignedResidents
        );


        return mapper.toResponseSummaryDto(cooperationCreated);
    }
}
