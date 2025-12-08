package com.crdev.connect_rural_api.data.resident;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResidentRepository extends JpaRepository<ResidentEntity, UUID>,
        JpaSpecificationExecutor<ResidentEntity> {
    List<ResidentEntity> findAllByCommunityKey(UUID communityKey);
    Optional<ResidentEntity> findByCommunityKeyAndKey(UUID communityKey, UUID residentKey);


}
