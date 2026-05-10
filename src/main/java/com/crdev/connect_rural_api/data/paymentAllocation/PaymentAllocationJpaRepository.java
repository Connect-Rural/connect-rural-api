package com.crdev.connect_rural_api.data.paymentallocation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentAllocationJpaRepository extends JpaRepository<PaymentAllocationEntity, UUID> {

    Optional<PaymentAllocationEntity> findByObligationKey(UUID obligationKey);

    List<PaymentAllocationEntity> findByObligationKeyIn(Collection<UUID> obligationKeys);

    void deleteByObligationKey(UUID obligationKey);
}
