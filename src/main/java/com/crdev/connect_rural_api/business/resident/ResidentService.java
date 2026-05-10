package com.crdev.connect_rural_api.business.resident;

import com.crdev.connect_rural_api.app.resident.dto.CreateResidentRequest;
import com.crdev.connect_rural_api.app.resident.dto.ResidentDetailResponse;
import com.crdev.connect_rural_api.app.resident.dto.ResidentFilterRequest;
import com.crdev.connect_rural_api.app.resident.dto.ResidentPageResponse;
import com.crdev.connect_rural_api.app.resident.dto.ResidentResponse;
import com.crdev.connect_rural_api.app.resident.dto.SimpleResidentResponse;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import com.crdev.connect_rural_api.data.resident.ResidentSpecs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final ResidentMapper mapper;

    public List<ResidentResponse> listByCommunity(String communityKey) {
        return mapper.toResponseList(
                residentRepository.findAllByCommunityKey(UUID.fromString(communityKey))
        );
    }

    public ResidentPageResponse getPaginated(String communityKey, ResidentFilterRequest filter) {
        Specification<ResidentEntity> spec = Specification.allOf(
                ResidentSpecs.withCommunity(UUID.fromString(communityKey)),
                ResidentSpecs.withKeyword(filter.getKeyword())
        );
        Page<ResidentEntity> result = residentRepository.findAll(spec,
                PageRequest.of(filter.getPage(), filter.getSize()));
        return new ResidentPageResponse(
                mapper.toResponseList(result.getContent()),
                filter.getPage(),
                filter.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public ResidentDetailResponse getByKey(String communityKey, String residentKey) {
        return mapper.toResponseDetail(findEntity(communityKey, residentKey));
    }

    public ResidentResponse create(String communityKey, CreateResidentRequest request) {
        log.info("Creating resident: communityKey={}", communityKey);
        ResidentEntity entity = new ResidentEntity(
                null,
                UUID.fromString(communityKey),
                request.getFirstName(),
                request.getLastName(),
                request.getBirthDate(),
                request.getPhoneNumber(),
                request.getEmail(),
                request.getAddress(),
                request.getAddressReference(),
                request.getJoinedAt(),
                true,
                null, null
        );
        return mapper.toResponse(residentRepository.save(entity));
    }

    @Transactional
    public ResidentResponse update(String communityKey, String residentKey, CreateResidentRequest request) {
        log.info("Updating resident: communityKey={}, residentKey={}", communityKey, residentKey);
        ResidentEntity existing = findEntity(communityKey, residentKey);
        mapper.updateEntityFromRequest(request, existing);
        return mapper.toResponse(residentRepository.save(existing));
    }

    @Transactional
    public void delete(String communityKey, String residentKey) {
        log.info("Deleting resident: communityKey={}, residentKey={}", communityKey, residentKey);
        ResidentEntity existing = findEntity(communityKey, residentKey);
        residentRepository.deleteById(existing.getKey());
    }

    public List<SimpleResidentResponse> getCatalog(String communityKey) {
        return residentRepository.getResidentsCatalogByCommunityKey(UUID.fromString(communityKey))
                .stream()
                .map(mapper::toSimpleResident)
                .collect(Collectors.toList());
    }

    public ResidentEntity findEntity(String communityKey, String residentKey) {
        return residentRepository.findByCommunityKeyAndKey(
                UUID.fromString(communityKey), UUID.fromString(residentKey))
                .orElseThrow(() -> new IllegalArgumentException("Resident not found"));
    }

    public List<ResidentEntity> listEntitiesByCommunity(String communityKey) {
        return residentRepository.findAllByCommunityKey(UUID.fromString(communityKey));
    }

    public Map<UUID, ResidentEntity> getByKeys(Collection<UUID> keys) {
        return residentRepository.findAllByKeyIn(keys).stream()
                .collect(Collectors.toMap(ResidentEntity::getKey, Function.identity()));
    }
}
