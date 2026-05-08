package com.crdev.connect_rural_api.data.financialObligation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialObligationRepository extends JpaRepository<FinancialObligationEntity, UUID> {

    List<FinancialObligationEntity> findByOriginTypeAndOriginId(String originType, UUID originId);

    Optional<FinancialObligationEntity> findByOriginTypeAndOriginIdAndResidentKey(
            String originType, UUID originId, UUID residentKey);

    List<FinancialObligationEntity> findByOriginTypeAndOriginIdAndStatus(
            String originType, UUID originId, String status);

    void deleteByOriginTypeAndOriginId(String originType, UUID originId);
}
