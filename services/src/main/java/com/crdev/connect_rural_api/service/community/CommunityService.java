package com.crdev.connect_rural_api.service.community;

import com.crdev.connect_rural_api.service.shared.PageParams;
import com.crdev.connect_rural_api.service.shared.PagedResult;
import com.crdev.connect_rural_api.repository.community.CommunitySpecs;
import com.crdev.connect_rural_api.model.CommunityEntity;
import com.crdev.connect_rural_api.repository.community.CommunityRepository;
import com.crdev.connect_rural_api.service.community.dto.Community;
import com.crdev.connect_rural_api.service.community.dto.CommunityQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Casos de uso del dominio comunidad.
 *
 * Trabaja únicamente con el modelo {@link Community}: no conoce entidades JPA
 * ni DTOs HTTP. El Controller (app/) traduce DTO → Community antes de llamar y
 * Community → DTO después de recibir.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMapper mapper;

    public Community create(Community draft) {
        log.info("Creating community: name={}", draft.getName());
        draft.setKey(null);
        draft.setCompletedConfiguration(false);
        draft.setActive(true);
        draft.setCreatedAt(null);
        draft.setUpdatedAt(null);
        CommunityEntity entity = mapper.toEntity(draft);
        return mapper.toDomain(communityRepository.save(entity));
    }

    public List<Community> getList() {
        return mapper.toDomainList(communityRepository.findAll());
    }

    public Community getByKey(String key) {
        return find(key);
    }

    @Transactional(readOnly = true)
    public PagedResult<Community> getPaginated(CommunityQuery query, PageParams page) {
        Specification<CommunityEntity> spec = CommunitySpecs.withKeyword(query.keyword());
        PageRequest pageable = PageRequest.of(page.page(), page.size());
        Page<CommunityEntity> result = communityRepository.findAll(spec, pageable);

        return new PagedResult<>(
                mapper.toDomainList(result.getContent()),
                page.page(),
                page.size(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Transactional
    public Community update(String key, Community draft) {
        log.info("Updating community: key={}", key);
        Community existing = find(key);
        existing.updateFrom(draft);
        existing.setUpdatedAt(LocalDateTime.now());
        CommunityEntity entity = mapper.toEntity(existing);
        return  mapper.toDomain(communityRepository.save(entity));
    }

    @Transactional
    public void delete(String key) {
        log.info("Deleting community: key={}", key);
        find(key);
        communityRepository.deleteById(UUID.fromString(key));
    }

    private Community find(String key){
        return communityRepository.findById(UUID.fromString(key))
                .map(mapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));
    }

}
