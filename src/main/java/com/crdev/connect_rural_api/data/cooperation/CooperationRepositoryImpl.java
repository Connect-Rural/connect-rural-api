package com.crdev.connect_rural_api.data.cooperation;

import com.crdev.connect_rural_api.business.cooperation.CooperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CooperationRepositoryImpl implements CooperationRepository {

    private final CooperationJpaRepository jpaRepository;

    @Override
    public List<CooperationEntity> findAllByCommunityKey(UUID communityKey) {
        return jpaRepository.findAllByCommunityKey(communityKey);
    }

    @Override
    public Page<CooperationEntity> findAll(Specification<CooperationEntity> spec, Pageable pageable) {
        return jpaRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<CooperationEntity> findByCommunityKeyAndKey(UUID communityKey, UUID cooperationKey) {
        return jpaRepository.findByCommunityKeyAndKey(communityKey, cooperationKey);
    }

    @Override
    public CooperationEntity save(CooperationEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public void deleteById(UUID key) {
        jpaRepository.deleteById(key);
    }
}
