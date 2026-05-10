package com.crdev.connect_rural_api.data.paymentallocation;

import com.crdev.connect_rural_api.business.paymentallocation.PaymentAllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentAllocationRepositoryImpl implements PaymentAllocationRepository {

    private final PaymentAllocationJpaRepository jpaRepository;

    @Override
    public PaymentAllocationEntity save(PaymentAllocationEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<PaymentAllocationEntity> findByObligationKey(UUID obligationKey) {
        return jpaRepository.findByObligationKey(obligationKey);
    }

    @Override
    public List<PaymentAllocationEntity> findByObligationKeyIn(Collection<UUID> obligationKeys) {
        return jpaRepository.findByObligationKeyIn(obligationKeys);
    }

    @Override
    public void deleteByObligationKey(UUID obligationKey) {
        jpaRepository.deleteByObligationKey(obligationKey);
    }

    @Override
    public void delete(PaymentAllocationEntity entity) {
        jpaRepository.delete(entity);
    }
}
