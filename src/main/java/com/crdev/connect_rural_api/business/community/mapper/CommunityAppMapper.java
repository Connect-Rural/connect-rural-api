package com.crdev.connect_rural_api.business.community.mapper;

import com.crdev.connect_rural_api.app.community.dto.request.CreateCommunityDto;
import com.crdev.connect_rural_api.app.community.dto.response.CommunityAdminResponseDto;
import com.crdev.connect_rural_api.app.community.dto.response.CommunityResponseDto;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                entity.getUpdatedAt(),
                entity.getWhatsappAppKey()
        );
    }

    public CommunityAdminResponseDto toAdminResponse(CommunityEntity entity) {
        if (entity == null) return null;
        String location = Stream.of(entity.getAddress(), entity.getMunicipality(), entity.getState(), entity.getPostalCode())
                    .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));

        return new CommunityAdminResponseDto(
                entity.getKey().toString(),
                entity.getName(),
                entity.getDescription(),
                entity.getLogoUrl(),
                location,
                entity.getSubscriptionPlan(),
                entity.getCompletedConfiguration(),
                "",
                "",
                1,
                2,
                entity.getActive(),
                entity.getWhatsappAppKey()
        );
    }

    public List<CommunityAdminResponseDto> toAdminResponseList(List<CommunityEntity> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toAdminResponse)
                .collect(Collectors.toList());
    }

    public List<CommunityResponseDto> toResponseList(List<CommunityEntity> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CommunityEntity updateFromDto(CreateCommunityDto dto,CommunityEntity entity){
        if (dto == null || entity == null) return entity;

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setLogoUrl(dto.getLogoUrl());
        entity.setSubscriptionPlan(dto.getSubscriptionPlan());

        return entity;

    }

}
