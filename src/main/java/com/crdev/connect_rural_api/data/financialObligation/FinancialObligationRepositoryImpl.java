package com.crdev.connect_rural_api.data.financialobligation;

import com.crdev.connect_rural_api.business.financialobligation.FinancialObligationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FinancialObligationRepositoryImpl implements FinancialObligationRepository {

    private final FinancialObligationJpaRepository jpaRepository;

    @Override
    public List<FinancialObligationEntity> saveAll(List<FinancialObligationEntity> entities) {
        return jpaRepository.saveAll(entities);
    }

    @Override
    public List<FinancialObligationEntity> findByOriginTypeAndOriginId(String originType, UUID originId) {
        return jpaRepository.findByOriginTypeAndOriginId(originType, originId);
    }

    @Override
    public Optional<FinancialObligationEntity> findByOriginTypeAndOriginIdAndResidentKey(
            String originType, UUID originId, UUID residentKey) {
        return jpaRepository.findByOriginTypeAndOriginIdAndResidentKey(originType, originId, residentKey);
    }

    @Override
    public List<FinancialObligationEntity> findByOriginTypeAndOriginIdAndStatus(
            String originType, UUID originId, String status) {
        return jpaRepository.findByOriginTypeAndOriginIdAndStatus(originType, originId, status);
    }

    @Override
    public Optional<FinancialObligationEntity> findById(UUID key) {
        return jpaRepository.findById(key);
    }

    @Override
    public FinancialObligationEntity save(FinancialObligationEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public void deleteByOriginTypeAndOriginId(String originType, UUID originId) {
        jpaRepository.deleteByOriginTypeAndOriginId(originType, originId);
    }
}
