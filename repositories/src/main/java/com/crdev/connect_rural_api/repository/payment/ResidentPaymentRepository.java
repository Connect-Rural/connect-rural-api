package com.crdev.connect_rural_api.repository.payment;

import com.crdev.connect_rural_api.model.ResidentPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ResidentPaymentRepository extends JpaRepository<ResidentPaymentEntity, UUID> {

    List<ResidentPaymentEntity> findAllByKeyIn(Collection<UUID> keys);
}
