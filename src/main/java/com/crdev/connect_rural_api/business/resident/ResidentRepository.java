package com.crdev.connect_rural_api.business.resident;

import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import com.crdev.connect_rural_api.data.resident.SimpleResident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResidentRepository {
    List<ResidentEntity> findAllByCommunityKey(UUID communityKey);
    Page<ResidentEntity> findAll(Specification<ResidentEntity> spec, Pageable pageable);
    Optional<ResidentEntity> findByCommunityKeyAndKey(UUID communityKey, UUID residentKey);
    List<ResidentEntity> findAllByKeyIn(Collection<UUID> keys);
    List<SimpleResident> getResidentsCatalogByCommunityKey(UUID communityKey);
    ResidentEntity save(ResidentEntity entity);
    void deleteById(UUID key);
}
