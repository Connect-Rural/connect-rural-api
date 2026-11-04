package com.crdev.connect_rural_api.data.cooperationAssignment;

import com.crdev.connect_rural_api.business.cooperationAssignment.CooperationAssignmentRepository;
import com.crdev.connect_rural_api.business.cooperationPeriod.CooperationPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CooperationAssignmentRepositoryImpl implements CooperationAssignmentRepository {
    private final CooperationAssignmentJpaRepository jpaRepository;

}
