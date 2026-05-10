package com.crdev.connect_rural_api.business.community;

import com.crdev.connect_rural_api.app.community.dto.CommunityAdminResponse;
import com.crdev.connect_rural_api.app.community.dto.CommunityPageResponse;
import com.crdev.connect_rural_api.app.community.dto.CommunityResponse;
import com.crdev.connect_rural_api.app.community.dto.CreateCommunityRequest;
import com.crdev.connect_rural_api.app.community.dto.CommunityFilterRequest;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMapper mapper;

    public CommunityResponse create(CreateCommunityRequest request) {
        log.info("Creating community: name={}", request.getName());
        CommunityEntity entity = new CommunityEntity(
                null,
                request.getName(),
                request.getDescription(),
                request.getLogoUrl(),
                null, null, null, null,
                request.getSubscriptionPlan(),
                false,
                null,
                true,
                null, null
        );
        return mapper.toResponse(communityRepository.save(entity));
    }

    public List<CommunityAdminResponse> getList() {
        return mapper.toAdminResponseList(communityRepository.findAll());
    }

    public CommunityAdminResponse getByKey(String key) {
        return mapper.toAdminResponse(findEntityByKey(key));
    }

    public CommunityPageResponse getPaginated(CommunityFilterRequest filter) {
        String keyword = filter.getKeyword();
        Specification<CommunityEntity> spec = (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like)
            );
        };
        Page<CommunityEntity> page = communityRepository.findAll(spec,
                PageRequest.of(filter.getPage(), filter.getSize()));
        return new CommunityPageResponse(
                mapper.toAdminResponseList(page.getContent()),
                filter.getPage(),
                filter.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional
    public CommunityAdminResponse update(String key, CreateCommunityRequest request) {
        log.info("Updating community: key={}", key);
        CommunityEntity existing = findEntityByKey(key);
        mapper.updateFromRequest(request, existing);
        return mapper.toAdminResponse(communityRepository.save(existing));
    }

    @Transactional
    public void delete(String key) {
        log.info("Deleting community: key={}", key);
        findEntityByKey(key);
        communityRepository.deleteById(UUID.fromString(key));
    }

    public CommunityEntity findEntityByKey(String key) {
        return communityRepository.findById(UUID.fromString(key))
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));
    }

    public Optional<CommunityEntity> findByWhatsappAppKey(UUID appKey) {
        return communityRepository.findByWhatsappAppKeyAndActiveTrue(appKey);
    }

    public CommunityEntity saveEntity(CommunityEntity entity) {
        return communityRepository.save(entity);
    }
}
