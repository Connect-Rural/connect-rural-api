package com.crdev.connect_rural_api.business.cooperation.usecases;

import com.crdev.connect_rural_api.app.cooperation.dto.request.CreateCooperationRequestDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationSummaryResponseDto;
import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.enums.CooperationAssignmentType;
import com.crdev.connect_rural_api.business.cooperation.mapper.CooperationAppMapper;
import com.crdev.connect_rural_api.business.financialObligation.FinancialObligationService;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.fromString;

@Component
@AllArgsConstructor
@Slf4j
public class CreateCooperationUseCase {
    private final CooperationService service;
    private final FinancialObligationService financialObligationService;
    private final ResidentService residentService;
    private final CooperationAppMapper mapper;

    @Transactional
    public CooperationSummaryResponseDto execute(String communityKey, CreateCooperationRequestDto dto) {
        log.info("Creating cooperation: communityKey={}, name={}", communityKey, dto.getName());

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
                null,
                null,
                null
        );

        CooperationEntity cooperationCreated = service.create(cooperationEntity);

        List<UUID> assignedResidentKeys = resolveAssignedResidents(communityKey, dto);

        if (!assignedResidentKeys.isEmpty()) {
            financialObligationService.createForCooperation(
                    cooperationCreated.getKey(),
                    fromString(communityKey),
                    dto.getDueDate(),
                    dto.getBaseAmount(),
                    assignedResidentKeys
            );
        }

        return mapper.toResponseSummaryDto(cooperationCreated);
    }

    private List<UUID> resolveAssignedResidents(String communityKey, CreateCooperationRequestDto dto) {
        CooperationAssignmentType type = dto.getAssignmentType();

        if (type == CooperationAssignmentType.INDIVIDUAL) {
            return dto.getAssignedResidentKeys().stream().map(UUID::fromString).toList();
        }

        List<ResidentEntity> allResidents = residentService.listByCommunity(communityKey);

        if (type == CooperationAssignmentType.ALL) {
            return allResidents.stream().map(ResidentEntity::getKey).toList();
        }

        if (type == CooperationAssignmentType.ALL_EXCEPT) {
            return allResidents.stream()
                    .filter(r -> !dto.getExcludedResidentKeys().contains(r.getKey().toString()))
                    .map(ResidentEntity::getKey)
                    .toList();
        }

        return List.of();
    }
}
