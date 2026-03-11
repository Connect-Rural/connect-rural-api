package com.crdev.connect_rural_api.data.cooperation;

import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CooperationRepository extends JpaRepository<CooperationEntity, UUID>,
        JpaSpecificationExecutor<CooperationEntity> {
    List<CooperationEntity> findAllByCommunityKey(UUID communityKey);

    Optional<CooperationEntity> findByCommunityKeyAndKey(UUID communityKey, UUID cooperationKey);

}
