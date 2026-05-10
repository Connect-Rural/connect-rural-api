package com.crdev.connect_rural_api.business.cooperation;

import com.crdev.connect_rural_api.app.cooperation.dto.CreateCooperationRequest;
import com.crdev.connect_rural_api.app.cooperation.dto.CooperationDetailResponse;
import com.crdev.connect_rural_api.app.cooperation.dto.CooperationResponse;
import com.crdev.connect_rural_api.app.cooperation.dto.CooperationSummaryResponse;
import com.crdev.connect_rural_api.app.cooperation.dto.ResidentAssigned;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CooperationMapper {

    public CooperationSummaryResponse toSummaryResponse(CooperationEntity entity) {
        return toSummaryResponse(entity, 80, 0, 0, 0);
    }

    public CooperationSummaryResponse toSummaryResponse(CooperationEntity entity, double progress,
                                                         int totalAssigned, int totalPending, int totalPaid) {
        if (entity == null) return null;
        return CooperationSummaryResponse.builder()
                .key(entity.getKey().toString())
                .communityKey(entity.getCommunityKey().toString())
                .name(entity.getName())
                .description(entity.getDescription())
                .baseAmount(entity.getBaseAmount())
                .status(entity.getStatus())
                .progressPercentage(progress)
                .totalAssignedResidents(totalAssigned)
                .paidResidents(totalPaid)
                .pendingResidents(totalPending)
                .startDate(entity.getStartDate())
                .dueDate(entity.getDueDate())
                .closedAt("CLOSED".equals(entity.getStatus()) ? entity.getClosedAt() : null)
                .build();
    }

    public CooperationResponse toResponse(CooperationEntity entity,
                                          List<String> assignedResidents,
                                          List<String> excludedResidents) {
        if (entity == null) return null;
        return CooperationResponse.builder()
                .key(entity.getKey().toString())
                .communityKey(entity.getCommunityKey().toString())
                .name(entity.getName())
                .description(entity.getDescription())
                .baseAmount(entity.getBaseAmount())
                .startDate(entity.getStartDate())
                .dueDate(entity.getDueDate())
                .status(entity.getStatus())
                .assignmentType(entity.getAssignmentType())
                .assignedResidentKeys(assignedResidents)
                .excludedResidentKeys(excludedResidents)
                .hasLateFee(entity.getAllowLateFee())
                .lateFeeAmount(entity.getLateFeeAmount())
                .lateFeePeriodicity(entity.getLateFeePeriod())
                .closedAt("CLOSED".equals(entity.getStatus()) ? entity.getClosedAt() : null)
                .build();
    }

    public CooperationEntity updateEntityFromRequest(CreateCooperationRequest request, CooperationEntity entity) {
        if (request == null || entity == null) return entity;
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setBaseAmount(request.getBaseAmount());
        entity.setStartDate(request.getStartDate());
        entity.setDueDate(request.getDueDate());
        entity.setAllowLateFee(request.getHasLateFee());
        entity.setLateFeeAmount(request.getLateFeeAmount());
        entity.setLateFeePeriod(request.getLateFeePeriodicity());
        entity.setAssignmentType(request.getAssignmentType().toString());
        entity.setStatus(request.getStatus());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    public CooperationDetailResponse toDetailResponse(CooperationEntity entity,
                                                       List<ResidentAssigned> assignments,
                                                       double progressPercentage,
                                                       int totalAssignedResidents,
                                                       int paidResidents,
                                                       int pendingResidents) {
        if (entity == null) return null;
        return CooperationDetailResponse.builder()
                .key(entity.getKey().toString())
                .communityKey(entity.getCommunityKey().toString())
                .name(entity.getName())
                .description(entity.getDescription())
                .baseAmount(entity.getBaseAmount())
                .startDate(entity.getStartDate())
                .dueDate(entity.getDueDate())
                .status(entity.getStatus())
                .assignments(assignments)
                .progressPercentage(progressPercentage)
                .totalAssignedResidents(totalAssignedResidents)
                .paidResidents(paidResidents)
                .pendingResidents(pendingResidents)
                .hasLateFee(entity.getAllowLateFee())
                .lateFeeAmount(entity.getLateFeeAmount())
                .lateFeePeriodicity(entity.getLateFeePeriod())
                .closedAt("CLOSED".equals(entity.getStatus()) ? entity.getClosedAt() : null)
                .build();
    }
}
