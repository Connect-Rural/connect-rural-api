package com.crdev.connect_rural_api.repository.cooperation;


import com.crdev.connect_rural_api.model.CooperationPeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CooperationPeriodRepository extends JpaRepository<CooperationPeriodEntity, UUID>,
        JpaSpecificationExecutor<CooperationPeriodEntity> {
}
