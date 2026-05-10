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
        return CommunityResponse.builder()
                .key(entity.getKey().toString())
                .name(entity.getName())
                .description(entity.getDescription())
                .logoUrl(entity.getLogoUrl())
                .address(entity.getAddress())
                .state(entity.getState())
                .municipality(entity.getMunicipality())
                .postalCode(entity.getPostalCode())
                .subscriptionPlan(entity.getSubscriptionPlan())
                .completedConfiguration(entity.getCompletedConfiguration())
                .active(entity.getActive())
                .createdAt(entity.getCreateAt())
                .updatedAt(entity.getUpdatedAt())
                .whatsappAppKey(entity.getWhatsappAppKey())
                .build();
    }

    public CommunityAdminResponse toAdminResponse(CommunityEntity entity) {
        if (entity == null) return null;
        String location = Stream.of(entity.getAddress(), entity.getMunicipality(), entity.getState(), entity.getPostalCode())
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));

        return CommunityAdminResponse.builder()
                .key(entity.getKey().toString())
                .name(entity.getName())
                .description(entity.getDescription())
                .logoUrl(entity.getLogoUrl())
                .location(location)
                .subscriptionPlan(entity.getSubscriptionPlan())
                .completedConfiguration(entity.getCompletedConfiguration())
                .adminEmail("")
                .adminPhone("")
                .membersCount(1)
                .usersCount(2)
                .active(entity.getActive())
                .build();
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
