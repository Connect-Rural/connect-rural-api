package com.crdev.connect_rural_api.data.cooperationPeriod;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.crdev.connect_rural_api.model.CooperationPeriodEntity;

import java.util.UUID;

public interface CooperationPeriodJpaRepository  extends JpaRepository<CooperationPeriodEntity, UUID>,
        JpaSpecificationExecutor<CooperationPeriodEntity> {
}
