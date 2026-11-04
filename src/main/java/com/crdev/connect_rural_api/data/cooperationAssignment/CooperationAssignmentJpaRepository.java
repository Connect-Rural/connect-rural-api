package com.crdev.connect_rural_api.data.cooperationAssignment;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CooperationAssignmentJpaRepository extends JpaRepository<CooperationAssignmentEntity, UUID>,
        JpaSpecificationExecutor<CooperationAssignmentEntity> {
}
