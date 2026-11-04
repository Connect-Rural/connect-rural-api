package com.crdev.connect_rural_api.data.cooperationPeriod;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CooperationPeriodJpaRepository  extends JpaRepository<CooperationPeriodEntity, UUID>,
        JpaSpecificationExecutor<CooperationPeriodEntity> {
}
