package com.crdev.connect_rural_api.business.cooperationResident;

import com.crdev.connect_rural_api.data.cooperationResident.CooperationResidentEntity;
import com.crdev.connect_rural_api.data.cooperationResident.CooperationResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.UUID.*;

@Service
@RequiredArgsConstructor
public class CooperationResidentService {
    private final CooperationResidentRepository cooperationResidentRepository;

    @Transactional
    public void assignResidentsToCooperation(String cooperationKey, List<String> ids) {

        UUID cooperationId = fromString(cooperationKey);

        Set<CooperationResidentEntity> alreadyAssigned =
                new HashSet<>(cooperationResidentRepository.findByCooperationKey(cooperationId));

        Set<UUID> alreadyAssignedKeys = alreadyAssigned.stream()
                .map(CooperationResidentEntity::getResidentKey)
                .collect(Collectors.toSet());

        Set<UUID> toAsignKeys = ids.stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());

        List<CooperationResidentEntity> toAssign = toAsignKeys.stream()
                .filter(key -> !alreadyAssignedKeys.contains(key))
                .map(key -> new CooperationResidentEntity(
                        null,
                       cooperationId,
                        key,
                        false,
                        null,
                        null,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ))
                .toList();

        List<CooperationResidentEntity> toDelete = alreadyAssigned.stream()
                .filter(entity -> !toAsignKeys.contains(entity.getResidentKey()))
                .toList();

        cooperationResidentRepository.saveAll(toAssign);
        cooperationResidentRepository.deleteAll(toDelete);
    }



}
