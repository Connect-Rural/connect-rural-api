package com.crdev.connect_rural_api.business.resident;

import com.crdev.connect_rural_api.business.resident.specs.ResidentSpecs;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import com.crdev.connect_rural_api.data.resident.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.crdev.connect_rural_api.business.resident.specs.ResidentSpecs.*;


@Service
@RequiredArgsConstructor
public class ResidentService {
    private final ResidentRepository residentRepository;

    public List<ResidentEntity> listByCommunity(String communityKey) {
        return residentRepository.findAllByCommunityKey(UUID.fromString(communityKey));
    }

    public Page<ResidentEntity> getAllPaginatedAndFiltered(String communityKey, String keyword, int page, int size) {

        Specification<ResidentEntity> spec = Specification.allOf(
                ResidentSpecs.withCommunity(UUID.fromString(communityKey)),
                ResidentSpecs.withKeyword(keyword)
        );

        return residentRepository.findAll(spec, PageRequest.of(page, size));
    }
}
