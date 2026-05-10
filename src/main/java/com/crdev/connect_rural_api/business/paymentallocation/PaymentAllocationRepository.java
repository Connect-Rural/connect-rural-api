package com.crdev.connect_rural_api.business.paymentallocation;

import com.crdev.connect_rural_api.data.paymentAllocation.PaymentAllocationEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentAllocationRepository {
    PaymentAllocationEntity save(PaymentAllocationEntity entity);
    Optional<PaymentAllocationEntity> findByObligationKey(UUID obligationKey);
    List<PaymentAllocationEntity> findByObligationKeyIn(Collection<UUID> obligationKeys);
    void deleteByObligationKey(UUID obligationKey);
    void delete(PaymentAllocationEntity entity);
}
