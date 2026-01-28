package com.crdev.connect_rural_api.business.cooperation.mapper;

import com.crdev.connect_rural_api.app.cooperation.dto.request.CreateCooperationRequestDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationDetailResponseDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationResponseDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationSummaryResponseDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.ResidentAssigned;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

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
                totalPaid,
                totalPending,
                entity.getStartDate(),
                entity.getDueDate()
        );
    }

    public CooperationResponseDto toResponseDto(CooperationEntity entity,
                                                List<String> assignedResidents,
                                                List<String> excludedResidents
                                                ) {
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
                assignedResidents,
                excludedResidents,
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

    public CooperationDetailResponseDto toDetailResponseDto(
            CooperationEntity entity,
            List<ResidentAssigned> assignments,
            double progressPercentage,
            int totalAssignedResidents,
            int paidResidents,
            int pendingResidents) {
        if (entity == null) return null;

        return new CooperationDetailResponseDto(
                entity.getKey().toString(),
                entity.getCommunityKey().toString(),
                entity.getName(),
                entity.getDescription(),
                entity.getBaseAmount(),
                entity.getStartDate(),
                entity.getDueDate(),
                entity.getStatus(),
                assignments,
                progressPercentage,
                totalAssignedResidents,
                paidResidents,
                pendingResidents,
                entity.getAllowLateFee(),
                entity.getLateFeeAmount(),
                entity.getLateFeePeriod()
        );
    }

}
