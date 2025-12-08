package com.crdev.connect_rural_api.business.cooperation.mapper;

import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationSummaryResponseDto;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import org.springframework.stereotype.Component;

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
}
