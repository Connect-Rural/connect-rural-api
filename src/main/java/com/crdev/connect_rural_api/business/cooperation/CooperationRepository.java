package com.crdev.connect_rural_api.business.cooperation;

import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CooperationRepository {
    List<CooperationEntity> findAllByCommunityKey(UUID communityKey);
    Page<CooperationEntity> findAll(Specification<CooperationEntity> spec, Pageable pageable);
    Optional<CooperationEntity> findByCommunityKeyAndKey(UUID communityKey, UUID cooperationKey);
    CooperationEntity save(CooperationEntity entity);
    void deleteById(UUID key);
}
