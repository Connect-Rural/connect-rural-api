package com.crdev.connect_rural_api.repository.resident;

import com.crdev.connect_rural_api.model.ResidentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResidentRepository extends JpaRepository<ResidentEntity, UUID>,
        JpaSpecificationExecutor<ResidentEntity> {


    Optional<ResidentEntity> findByEmail(String email);

    List<ResidentEntity> findAllByCommunityKey(UUID communityKey);

    Optional<ResidentEntity> findByCommunityKeyAndKey(UUID communityKey, UUID residentKey);

    List<ResidentEntity> findAllByKeyIn(Collection<UUID> keys);

    List<SimpleResident> getResidentsCatalogByCommunityKey(UUID communityKey);
}
