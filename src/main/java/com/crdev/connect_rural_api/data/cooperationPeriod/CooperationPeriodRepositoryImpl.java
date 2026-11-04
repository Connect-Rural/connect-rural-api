package com.crdev.connect_rural_api.data.cooperationPeriod;

import com.crdev.connect_rural_api.business.cooperationPeriod.CooperationPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CooperationPeriodRepositoryImpl implements CooperationPeriodRepository {
    private final CooperationPeriodJpaRepository jpaRepository;

}
