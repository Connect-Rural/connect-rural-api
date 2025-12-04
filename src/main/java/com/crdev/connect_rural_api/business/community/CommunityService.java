package com.crdev.connect_rural_api.business.community;

import com.crdev.connect_rural_api.business.community.mapper.CommunityAppMapper;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import com.crdev.connect_rural_api.data.community.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityAppMapper dataMapper;

    public List<CommunityEntity> getAll() {
        return communityRepository.findAll();
    }

    public Page<CommunityEntity> getAllPaginatedAndFiltered(String keyword, int page, int size) {

        Specification<CommunityEntity> spec = (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String like = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like)
            );
        };

        return communityRepository.findAll(spec, PageRequest.of(page, size));
    }

    public CommunityEntity create(CommunityEntity community) {
        return communityRepository.save(community);
    }

    public CommunityEntity getBykey(String key) {
        return communityRepository.findById( UUID.fromString(key)).orElseThrow(() -> new IllegalArgumentException("Community not found"));
    }

    public CommunityEntity update(CommunityEntity entity) {
        return communityRepository.save(entity);
    }

    public void delete(String key) {
        communityRepository.deleteById(UUID.fromString(key));
    }


}
