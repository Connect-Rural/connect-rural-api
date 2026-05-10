package com.crdev.connect_rural_api.business.community;

import com.crdev.connect_rural_api.data.community.CommunityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityRepository {
    List<CommunityEntity> findAll();
    Page<CommunityEntity> findAll(Specification<CommunityEntity> spec, Pageable pageable);
    CommunityEntity save(CommunityEntity entity);
    Optional<CommunityEntity> findById(UUID key);
    void deleteById(UUID key);
    Optional<CommunityEntity> findByWhatsappAppKeyAndActiveTrue(UUID whatsappAppKey);
}
