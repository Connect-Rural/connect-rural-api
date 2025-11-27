package com.crdev.connect_rural_api.app.community.mapper;

import com.crdev.connect_rural_api.app.community.dto.CommunityResponseDto;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommunityAppMapper {

    public CommunityResponseDto toResponse(CommunityEntity entity) {
        if (entity == null) return null;
        return new CommunityResponseDto(
                entity.getKey().toString(),
                entity.getName(),
                entity.getDescription(),
                entity.getLogoUrl(),
                entity.getAddress(),
                entity.getState(),
                entity.getMunicipality(),
                entity.getPostalCode(),
                entity.getSubscriptionPlan(),
                entity.getCompletedConfiguration(),
                entity.getActive(),
                entity.getCreateAt(),
                entity.getUpdatedAt()
        );
    }

    public List<CommunityResponseDto> toResponseList(List<CommunityEntity> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
