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
        return new CooperationSummaryResponse(
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
                entity.getDueDate(),
                "CLOSED".equals(entity.getStatus()) ? entity.getClosedAt() : null
        );
    }

    public CooperationResponse toResponse(CooperationEntity entity,
                                          List<String> assignedResidents,
                                          List<String> excludedResidents) {
        if (entity == null) return null;
        return new CooperationResponse(
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
                entity.getLateFeePeriod(),
                "CLOSED".equals(entity.getStatus()) ? entity.getClosedAt() : null
        );
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
        return new CooperationDetailResponse(
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
                entity.getLateFeePeriod(),
                "CLOSED".equals(entity.getStatus()) ? entity.getClosedAt() : null
        );
    }
}
