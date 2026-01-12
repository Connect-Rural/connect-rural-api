package com.crdev.connect_rural_api.business.cooperation.mapper;

import com.crdev.connect_rural_api.app.cooperation.dto.request.CreateCooperationRequestDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationResponseDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationSummaryResponseDto;
import com.crdev.connect_rural_api.app.resident.dto.request.CreateResidentDto;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CooperationAppMapper {

    public CooperationSummaryResponseDto toResponseSummaryDto(CooperationEntity entity) {
        return toResponseSummaryDto(entity, 80, 0, 0, 0);
    }

    public CooperationSummaryResponseDto toResponseSummaryDto(CooperationEntity entity, double progress , int totalAssigned,
                                                              int totalPending ,
                                                              int totalPaid ) {
        if (entity == null) return null;
        return new CooperationSummaryResponseDto(
                entity.getKey().toString(),
                entity.getCommunityKey().toString(),
                entity.getName(),
                entity.getDescription(),
                entity.getBaseAmount(),
                entity.getStatus(),
                progress,
                totalAssigned,
                totalPending,
                totalPaid,
                entity.getStartDate(),
                entity.getDueDate()
        );
    }

    public List<CooperationSummaryResponseDto> toResponseSummaryList(List<CooperationEntity> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toResponseSummaryDto)
                .collect(Collectors.toList());
    }

    public CooperationResponseDto toResponseDto(CooperationEntity entity) {
        if (entity == null) return null;

        return new CooperationResponseDto(
                entity.getKey().toString(),
                entity.getCommunityKey().toString(),
                entity.getName(),
                entity.getDescription(),
                entity.getBaseAmount(),
                entity.getStartDate(),
                entity.getDueDate(),
                entity.getStatus(),
                entity.getAssignmentType(),
                new ArrayList<>(),
                new ArrayList<>(),
                entity.getAllowLateFee(),
                entity.getLateFeeAmount(),
                entity.getLateFeePeriod()

        );
    }

    public CooperationEntity updateEntityFromDto(
            CreateCooperationRequestDto dto, CooperationEntity entity){
        if (dto == null || entity == null) return entity;

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setBaseAmount(dto.getBaseAmount());
        entity.setStartDate(dto.getStartDate());
        entity.setDueDate(dto.getDueDate());
        entity.setAllowLateFee(dto.getHasLateFee());
        entity.setLateFeeAmount(dto.getLateFeeAmount());
        entity.setLateFeePeriod(dto.getLateFeePeriodicity());
        entity.setAssignmentType(dto.getAssignmentType().toString());
        entity.setStatus(dto.getStatus());
        entity.setUpdatedAt(LocalDateTime.now());

        return entity;

    }

}
