package com.crdev.connect_rural_api.data.cooperationResident;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CooperationResidentRepository extends JpaRepository<CooperationResidentEntity, UUID>,
        JpaSpecificationExecutor<CooperationResidentEntity> {

    List<CooperationResidentEntity> findByCooperationKey(UUID cooperationKey);

    Optional<CooperationResidentEntity> findByCooperationKeyAndResidentKey(UUID cooperationKey, UUID residentKey);
}
