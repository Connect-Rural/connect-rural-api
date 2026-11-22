package com.crdev.connect_rural_api.service.resident;


import com.crdev.connect_rural_api.model.ResidentEntity;
import com.crdev.connect_rural_api.repository.resident.ResidentRepository;
import com.crdev.connect_rural_api.repository.resident.ResidentSpecs;
import com.crdev.connect_rural_api.service.resident.dto.Resident;
import com.crdev.connect_rural_api.service.resident.dto.ResidentQuery;
import com.crdev.connect_rural_api.service.resident.dto.SimpleResidentCommand;
import com.crdev.connect_rural_api.service.resident.exception.EmailAlreadyUsedException;
import com.crdev.connect_rural_api.service.resident.exception.ResidentNotFoundException;
import com.crdev.connect_rural_api.service.shared.PageParams;
import com.crdev.connect_rural_api.service.shared.PagedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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

    public List<Resident> listByCommunity(String communityKey) {
        return mapper.toDomainList(
                residentRepository.findAllByCommunityKey(UUID.fromString(communityKey))
        );
    }

    // obtner Habitantes paginados

    public PagedResult<Resident>  getPaginated(String communityKey, ResidentQuery query, PageParams pageParams) {
        Specification<ResidentEntity> spec = Specification.allOf(
                ResidentSpecs.withCommunity(UUID.fromString(communityKey)),
                ResidentSpecs.withKeyword(query.keyword())
        );
        PageRequest pageable = PageRequest.of(pageParams.page(), pageParams.size());
        Page<ResidentEntity> result = residentRepository.findAll(spec, pageable);
        return new PagedResult<>(
                mapper.toDomainList(result.getContent()),
                pageParams.page(),
                pageParams.size(),
                result.getTotalElements(),
                result.getTotalPages());
    }


    public Resident getByKey(String communityKey, String residentKey) {
        return findResidentByKey(communityKey, UUID.fromString(residentKey));
    }

    @Transactional
    public Resident create(String communityKey, Resident residentDraft) {

        Resident completeResident = Resident.newResident(UUID.fromString(communityKey), residentDraft);
        //validar datos
        log.info("Creating resident: communityKey={}", communityKey);
        if (residentRepository.findByEmail(completeResident.getEmail()).isPresent()) {
            throw new EmailAlreadyUsedException(completeResident.getEmail());
        }

        try {
            return mapper.toDomain(
                    residentRepository.save(mapper.toEntity(completeResident))
            );

        } catch (DataIntegrityViolationException ex) {

            throw new EmailAlreadyUsedException(completeResident.getEmail());
        }



    }

    @Transactional
    public Resident update(String communityKey, String residentKey, Resident residentDraft) {
        log.info("Updating resident: communityKey={}, residentKey={}", communityKey, residentKey);
        Resident existing = findResidentByKey(communityKey, UUID.fromString(residentKey));
        Resident updated = existing.applyChanges(residentDraft);
        ResidentEntity entity = mapper.toEntity(updated);
        return mapper.toDomain(residentRepository.save(entity));
    }

    @Transactional
    public void delete(String communityKey, String residentKey) {
        log.info("Deleting resident: communityKey={}, residentKey={}", communityKey, residentKey);
        Resident existing = findResidentByKey(communityKey,UUID.fromString(residentKey));
        residentRepository.deleteById(existing.getKey());
    }

    public List<SimpleResidentCommand> getCatalog(String communityKey) {
        return residentRepository.getResidentsCatalogByCommunityKey(UUID.fromString(communityKey))
                .stream()
                .map(mapper::toSimpleResidentCommand)
                .collect(Collectors.toList());
    }


    private Resident findResidentByKey(String communityKey, UUID residentKey) {
        return residentRepository.findByCommunityKeyAndKey(
                        UUID.fromString(communityKey), residentKey).map(mapper::toDomain)
                .orElseThrow(() ->  new ResidentNotFoundException(residentKey));
    }

    private List<ResidentEntity> listEntitiesByCommunity(String communityKey) {
        return residentRepository.findAllByCommunityKey(UUID.fromString(communityKey));
    }

    private Map<UUID, ResidentEntity> getByKeys(Collection<UUID> keys) {
        return residentRepository.findAllByKeyIn(keys).stream()
                .collect(Collectors.toMap(ResidentEntity::getKey, Function.identity()));
    }
}
