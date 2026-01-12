package com.crdev.connect_rural_api.business.cooperation.usecases;


import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationResponseDto;
import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.mapper.CooperationAppMapper;
import com.crdev.connect_rural_api.business.cooperationResident.CooperationResidentService;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class GetCooperationByKeyUseCase {
    private final CooperationService service;

    private final CooperationResidentService cooperationResidentService;
    private final ResidentService residentService;
    private final CooperationAppMapper mapper;

    @Transactional
    public CooperationResponseDto execute(String communityKey, String cooperationKey){
        var cooperationEntity = service.getByKey(communityKey,cooperationKey);

        List<String> assignedResidentsKeys = new ArrayList<>();
        List<String> excludedResidentsKeys = new ArrayList<>();

        var assignedResidents = cooperationResidentService.listByCooperation(cooperationKey);

         if(cooperationEntity.getAssignmentType().equals("ALL_EXCEPT")){

             List<ResidentEntity> residentsInCommunity = residentService.listByCommunity(
                     communityKey
             );

            excludedResidentsKeys = residentsInCommunity.stream()
                     .filter(resident -> assignedResidents.stream()
                             .noneMatch(assignedResident -> assignedResident.getResidentKey().equals(resident.getKey()))
                     )
                     .map(resident -> resident.getKey().toString())
                     .toList();
        }
        else if(cooperationEntity.getAssignmentType().equals("INDIVIDUAL")){
            assignedResidentsKeys = assignedResidents.stream()
                    .map(assignedResident -> assignedResident.getResidentKey().toString())
                    .toList();
        }


        return mapper.toResponseDto(cooperationEntity,
                assignedResidentsKeys,
                 excludedResidentsKeys
                );

    }
}
