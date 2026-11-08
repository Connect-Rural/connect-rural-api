package com.crdev.connect_rural_api.data.residentPayment;

import org.springframework.data.jpa.repository.JpaRepository;
import com.crdev.connect_rural_api.model.ResidentPaymentEntity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ResidentPaymentJpaRepository extends JpaRepository<ResidentPaymentEntity, UUID> {

    List<ResidentPaymentEntity> findAllByKeyIn(Collection<UUID> keys);
}
