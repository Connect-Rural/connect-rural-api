package com.crdev.connect_rural_api.business.cooperation.usecases;

import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationDetailResponseDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.ResidentAssigned;
import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.mapper.CooperationAppMapper;
import com.crdev.connect_rural_api.business.cooperationResident.CooperationResidentService;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.data.cooperationResident.CooperationResidentEntity;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class GetCooperationDetailByKeyUseCase {
    private final CooperationService cooperationService;
    private final CooperationResidentService cooperationResidentService;
    private final ResidentService residentService;
    private final CooperationAppMapper mapper;

    @Transactional
    public CooperationDetailResponseDto execute(String communityKey, String cooperationKey) {

        var cooperationEntity = cooperationService.getByKey(communityKey, cooperationKey);

        List<CooperationResidentEntity> cooperationResidents =
                cooperationResidentService.listByCooperation(cooperationKey);

        Map<UUID, Boolean> paymentStatusMap = cooperationResidents.stream()
                .collect(Collectors.toMap(
                        CooperationResidentEntity::getResidentKey,
                        CooperationResidentEntity::getIsPaid
                ));

        List<UUID> assignedResidentKeys = cooperationResidents.stream()
                .map(CooperationResidentEntity::getResidentKey)
                .toList();

        List<ResidentAssigned> assignments = new ArrayList<>();

        if (!assignedResidentKeys.isEmpty()) {
            List<ResidentEntity> assignedResidents = assignedResidentKeys.stream()
                    .map(key -> {
                        try {
                            return residentService.getByKey(communityKey, key.toString());
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            assignments = assignedResidents.stream()
                    .map(resident -> new ResidentAssigned(
                            resident.getKey().toString(),
                            resident.getFirstName(),
                            resident.getLastName(),
                            paymentStatusMap.getOrDefault(resident.getKey(), false)
                    ))
                    .collect(Collectors.toList());
        }

        // Calcular estadísticas
        int totalAssignedResidents = assignments.size();
        int paidResidents = (int) assignments.stream()
                .filter(ResidentAssigned::getIsPaid)
                .count();
        int pendingResidents = totalAssignedResidents - paidResidents;

        // Calcular porcentaje de progreso
        double progressPercentage = totalAssignedResidents > 0
                ? (paidResidents * 100.0) / totalAssignedResidents
                : 0.0;

        return mapper.toDetailResponseDto(
                cooperationEntity,
                assignments,
                progressPercentage,
                totalAssignedResidents,
                paidResidents,
                pendingResidents
        );
    }
}
