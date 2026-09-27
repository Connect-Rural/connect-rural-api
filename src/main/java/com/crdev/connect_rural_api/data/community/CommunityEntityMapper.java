package com.crdev.connect_rural_api.data.community;

import com.crdev.connect_rural_api.business.community.Community;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapea entre la entidad JPA {@link CommunityEntity} y el modelo de dominio
 * {@link Community}. Es el único punto donde data/ y business/ se conocen.
 */
@Component
public class CommunityEntityMapper {

    public CommunityEntity toEntity(Community community) {
        if (community == null) return null;
        CommunityEntity entity = new CommunityEntity();
        entity.setKey(community.getKey());
        entity.setName(community.getName());
        entity.setDescription(community.getDescription());
        entity.setLogoUrl(community.getLogoUrl());
        entity.setAddress(community.getAddress());
        entity.setState(community.getState());
        entity.setMunicipality(community.getMunicipality());
        entity.setPostalCode(community.getPostalCode());
        entity.setSubscriptionPlan(community.getSubscriptionPlan());
        entity.setCompletedConfiguration(community.getCompletedConfiguration());
        entity.setWhatsappAppKey(community.getWhatsappAppKey());
        entity.setActive(community.getActive());
        entity.setCreatedAt(community.getCreatedAt());
        entity.setUpdatedAt(community.getUpdatedAt());
        return entity;
    }

    public Community toDomain(CommunityEntity entity) {
        if (entity == null) return null;
        Community community = new Community();
        community.setKey(entity.getKey());
        community.setName(entity.getName());
        community.setDescription(entity.getDescription());
        community.setLogoUrl(entity.getLogoUrl());
        community.setAddress(entity.getAddress());
        community.setState(entity.getState());
        community.setMunicipality(entity.getMunicipality());
        community.setPostalCode(entity.getPostalCode());
        community.setSubscriptionPlan(entity.getSubscriptionPlan());
        community.setCompletedConfiguration(entity.getCompletedConfiguration());
        community.setWhatsappAppKey(entity.getWhatsappAppKey());
        community.setActive(entity.getActive());
        community.setCreatedAt(entity.getCreatedAt());
        community.setUpdatedAt(entity.getUpdatedAt());
        return community;
    }

    public List<Community> toDomainList(List<CommunityEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toDomain).toList();
    }

    public List<CommunityEntity> toEntityList(List<Community> communities) {
        if (communities == null) return List.of();
        return communities.stream().map(this::toEntity).toList();
    }
}
