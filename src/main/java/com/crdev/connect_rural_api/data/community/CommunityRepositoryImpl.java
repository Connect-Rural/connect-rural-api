package com.crdev.connect_rural_api.data.community;

import com.crdev.connect_rural_api.business.community.Community;
import com.crdev.connect_rural_api.business.community.CommunityQuery;
import com.crdev.connect_rural_api.business.community.CommunityRepository;
import com.crdev.connect_rural_api.business.shared.PageParams;
import com.crdev.connect_rural_api.business.shared.PagedResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia: traduce el puerto {@link CommunityRepository}
 * (dominio) a Spring Data JPA. Es el único sitio donde conviven tipos de
 * Spring Data y modelos de dominio.
 */
@Repository
@RequiredArgsConstructor
public class CommunityRepositoryImpl implements CommunityRepository {

    private final CommunityJpaRepository jpaRepository;
    private final CommunityEntityMapper mapper;

    @Override
    public List<Community> findAll() {
        return mapper.toDomainList(jpaRepository.findAll());
    }

    @Override
    public PagedResult<Community> findAll(CommunityQuery query, PageParams page) {
        Specification<CommunityEntity> spec = CommunitySpecs.withKeyword(query.keyword());
        Page<CommunityEntity> result = jpaRepository.findAll(spec,
                PageRequest.of(page.page(), page.size()));
        return new PagedResult<>(
                mapper.toDomainList(result.getContent()),
                page.page(),
                page.size(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public Community save(Community community) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(community)));
    }

    @Override
    public Optional<Community> findById(UUID key) {
        return jpaRepository.findById(key).map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID key) {
        jpaRepository.deleteById(key);
    }

    @Override
    public Optional<Community> findByWhatsappAppKeyAndActiveTrue(UUID whatsappAppKey) {
        return jpaRepository.findByWhatsappAppKeyAndActiveTrue(whatsappAppKey).map(mapper::toDomain);
    }
}
