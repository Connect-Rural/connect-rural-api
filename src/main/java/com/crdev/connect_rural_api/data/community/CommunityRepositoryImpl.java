package com.crdev.connect_rural_api.data.community;

import com.crdev.connect_rural_api.business.community.CommunityRepository;
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
public class CommunityRepositoryImpl implements CommunityRepository {

    private final CommunityJpaRepository jpaRepository;

    @Override
    public List<CommunityEntity> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Page<CommunityEntity> findAll(Specification<CommunityEntity> spec, Pageable pageable) {
        return jpaRepository.findAll(spec, pageable);
    }

    @Override
    public CommunityEntity save(CommunityEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<CommunityEntity> findById(UUID key) {
        return jpaRepository.findById(key);
    }

    @Override
    public void deleteById(UUID key) {
        jpaRepository.deleteById(key);
    }

    @Override
    public Optional<CommunityEntity> findByWhatsappAppKeyAndActiveTrue(UUID whatsappAppKey) {
        return jpaRepository.findByWhatsappAppKeyAndActiveTrue(whatsappAppKey);
    }
}
