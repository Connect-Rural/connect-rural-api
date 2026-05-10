package com.crdev.connect_rural_api.business.residentpayment;

import com.crdev.connect_rural_api.data.residentpayment.ResidentPaymentEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResidentPaymentRepository {
    ResidentPaymentEntity save(ResidentPaymentEntity entity);
    Optional<ResidentPaymentEntity> findById(UUID key);
    List<ResidentPaymentEntity> findAllByKeyIn(Collection<UUID> keys);
    void deleteById(UUID key);
}
