package com.crdev.connect_rural_api.business.cooperation;

import com.crdev.connect_rural_api.business.cooperation.specs.CooperationSpecs;
import com.crdev.connect_rural_api.business.resident.specs.ResidentSpecs;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import com.crdev.connect_rural_api.data.cooperation.CooperationRepository;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CooperationService {
    private final CooperationRepository cooperationRepository;

    public List<CooperationEntity> listByCommunity(String communityKey) {
        return cooperationRepository.findAllByCommunityKey(UUID.fromString(communityKey));
    }

    public Page<CooperationEntity> getAllPaginatedAndFiltered(String communityKey, String keyword, int page, int size) {

        Specification<CooperationEntity> spec = Specification.allOf(
                CooperationSpecs.withCommunity(UUID.fromString(communityKey)),
                CooperationSpecs.withKeyword(keyword)
        );

        return cooperationRepository.findAll(spec, PageRequest.of(page, size));
    }

    public  CooperationEntity getByKey(String communityKey, String cooperationKey) {
        return cooperationRepository.findByCommunityKeyAndKey(UUID.fromString(communityKey), UUID.fromString(cooperationKey))
                .orElseThrow(() -> new IllegalArgumentException("Cooperation not found"));
    }



    public CooperationEntity create(CooperationEntity cooperation) {
        return cooperationRepository.save(cooperation);
    }

    public CooperationEntity update(CooperationEntity entity) {
        return cooperationRepository.save(entity);
    }

    public void delete(String cooperationKey){

        cooperationRepository.deleteById(UUID.fromString(cooperationKey));

    }



}
