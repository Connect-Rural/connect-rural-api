package com.crdev.connect_rural_api.data.resident;

import com.crdev.connect_rural_api.business.resident.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ResidentRepositoryImpl implements ResidentRepository {

    private final ResidentJpaRepository jpaRepository;

    @Override
    public List<ResidentEntity> findAllByCommunityKey(UUID communityKey) {
        return jpaRepository.findAllByCommunityKey(communityKey);
    }

    @Override
    public Page<ResidentEntity> findAll(Specification<ResidentEntity> spec, Pageable pageable) {
        return jpaRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<ResidentEntity> findByCommunityKeyAndKey(UUID communityKey, UUID residentKey) {
        return jpaRepository.findByCommunityKeyAndKey(communityKey, residentKey);
    }

    @Override
    public List<ResidentEntity> findAllByKeyIn(Collection<UUID> keys) {
        return jpaRepository.findAllByKeyIn(keys);
    }

    @Override
    public List<SimpleResident> getResidentsCatalogByCommunityKey(UUID communityKey) {
        return jpaRepository.getResidentsCatalogByCommunityKey(communityKey);
    }

    @Override
    public ResidentEntity save(ResidentEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public void deleteById(UUID key) {
        jpaRepository.deleteById(key);
    }
}
