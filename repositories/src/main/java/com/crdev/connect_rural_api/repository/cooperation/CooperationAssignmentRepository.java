package com.crdev.connect_rural_api.repository.cooperation;


import com.crdev.connect_rural_api.model.CooperationAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CooperationAssignmentRepository extends JpaRepository<CooperationAssignmentEntity, UUID>,
        JpaSpecificationExecutor<CooperationAssignmentEntity> {
}
