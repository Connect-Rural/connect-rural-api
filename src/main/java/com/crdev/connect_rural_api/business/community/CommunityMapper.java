package com.crdev.connect_rural_api.business.community;

import com.crdev.connect_rural_api.app.community.dto.CreateCommunityRequest;
import com.crdev.connect_rural_api.app.community.dto.CommunityAdminResponse;
import com.crdev.connect_rural_api.app.community.dto.CommunityResponse;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CommunityMapper {

    public CommunityResponse toResponse(CommunityEntity entity) {
        if (entity == null) return null;
        return new CommunityResponse(
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

    public CommunityAdminResponse toAdminResponse(CommunityEntity entity) {
        if (entity == null) return null;
        String location = Stream.of(entity.getAddress(), entity.getMunicipality(), entity.getState(), entity.getPostalCode())
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));

        return new CommunityAdminResponse(
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
                entity.getActive()
        );
    }

    public List<CommunityAdminResponse> toAdminResponseList(List<CommunityEntity> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toAdminResponse)
                .collect(Collectors.toList());
    }

    public List<CommunityResponse> toResponseList(List<CommunityEntity> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CommunityEntity updateFromRequest(CreateCommunityRequest request, CommunityEntity entity) {
        if (request == null || entity == null) return entity;
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setLogoUrl(request.getLogoUrl());
        entity.setSubscriptionPlan(request.getSubscriptionPlan());
        return entity;
    }
}
