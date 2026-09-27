package com.crdev.connect_rural_api.app.community;

import com.crdev.connect_rural_api.app.community.dto.CommunityAdminResponse;
import com.crdev.connect_rural_api.app.community.dto.CommunityFilterRequest;
import com.crdev.connect_rural_api.app.community.dto.CommunityResponse;
import com.crdev.connect_rural_api.app.community.dto.CreateCommunityRequest;
import com.crdev.connect_rural_api.business.community.Community;
import com.crdev.connect_rural_api.business.community.CommunityQuery;
import com.crdev.connect_rural_api.business.shared.PageParams;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Mapea entre el modelo de dominio {@link Community} y los DTOs HTTP.
 *
 * Vive en app/ (adaptador) para que business/ no dependa de los DTO de
 * entrada/salida. El mapeo Entity ↔ Community es responsabilidad de data/.
 */
@Component
public class CommunityMapper {

    public CommunityResponse toResponse(Community community) {
        if (community == null) return null;
        return CommunityResponse.builder()
                .key(community.getKey() == null ? null : community.getKey().toString())
                .name(community.getName())
                .description(community.getDescription())
                .logoUrl(community.getLogoUrl())
                .address(community.getAddress())
                .state(community.getState())
                .municipality(community.getMunicipality())
                .postalCode(community.getPostalCode())
                .subscriptionPlan(community.getSubscriptionPlan())
                .completedConfiguration(community.getCompletedConfiguration())
                .active(community.getActive())
                .createdAt(toDate(community.getCreatedAt()))
                .updatedAt(toDate(community.getUpdatedAt()))
                .whatsappAppKey(community.getWhatsappAppKey())
                .build();
    }

    public CommunityAdminResponse toAdminResponse(Community community) {
        if (community == null) return null;
        String location = Stream.of(community.getAddress(), community.getMunicipality(),
                        community.getState(), community.getPostalCode())
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));

        return CommunityAdminResponse.builder()
                .key(community.getKey() == null ? null : community.getKey().toString())
                .name(community.getName())
                .description(community.getDescription())
                .logoUrl(community.getLogoUrl())
                .location(location)
                .subscriptionPlan(community.getSubscriptionPlan())
                .completedConfiguration(community.getCompletedConfiguration())
                // TODO: valores placeholder hasta existir el módulo de administradores/roles.
                .adminEmail("")
                .adminPhone("")
                .membersCount(1)
                .usersCount(2)
                .active(community.getActive())
                .build();
    }

    public List<CommunityAdminResponse> toAdminResponseList(List<Community> communities) {
        if (communities == null) return null;
        return communities.stream()
                .map(this::toAdminResponse)
                .collect(Collectors.toList());
    }

    public List<CommunityResponse> toResponseList(List<Community> communities) {
        if (communities == null) return null;
        return communities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * DTO de entrada → modelo de dominio (borrador para create/update).
     *
     * adminEmail/adminPhone vienen validados en el request pero no se
     * persisten: aún no existe entidad de administrador.
     */
    public Community toDomain(CreateCommunityRequest request) {
        if (request == null) return null;
        Community community = new Community();
        community.setName(request.getName());
        community.setDescription(request.getDescription());
        community.setLogoUrl(request.getLogoUrl());
        community.setSubscriptionPlan(request.getSubscriptionPlan());
        return community;
    }

    public CommunityQuery toQuery(CommunityFilterRequest filter) {
        return new CommunityQuery(filter == null ? null : filter.getKeyword());
    }

    public PageParams toPageParams(CommunityFilterRequest filter) {
        if (filter == null) return PageParams.of(null, null);
        return PageParams.of(filter.getPage(), filter.getSize());
    }

    /**
     * LocalDateTime (dominio) → Date (contrato HTTP actual, se serializa como
     * epoch millis). No cambiar sin coordinar con el frontend.
     */
    private Date toDate(LocalDateTime dateTime) {
        return dateTime == null ? null : Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
