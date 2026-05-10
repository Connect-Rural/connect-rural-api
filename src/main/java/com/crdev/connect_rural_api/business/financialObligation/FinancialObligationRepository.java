package com.crdev.connect_rural_api.business.financialobligation;

import com.crdev.connect_rural_api.data.financialobligation.FinancialObligationEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialObligationRepository {
    List<FinancialObligationEntity> saveAll(List<FinancialObligationEntity> entities);
    List<FinancialObligationEntity> findByOriginTypeAndOriginId(String originType, UUID originId);
    Optional<FinancialObligationEntity> findByOriginTypeAndOriginIdAndResidentKey(String originType, UUID originId, UUID residentKey);
    List<FinancialObligationEntity> findByOriginTypeAndOriginIdAndStatus(String originType, UUID originId, String status);
    Optional<FinancialObligationEntity> findById(UUID key);
    FinancialObligationEntity save(FinancialObligationEntity entity);
    void deleteByOriginTypeAndOriginId(String originType, UUID originId);
}
