package com.crdev.connect_rural_api.data.residentpayment;

import com.crdev.connect_rural_api.business.residentpayment.ResidentPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ResidentPaymentRepositoryImpl implements ResidentPaymentRepository {

    private final ResidentPaymentJpaRepository jpaRepository;

    @Override
    public ResidentPaymentEntity save(ResidentPaymentEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<ResidentPaymentEntity> findById(UUID key) {
        return jpaRepository.findById(key);
    }

    @Override
    public List<ResidentPaymentEntity> findAllByKeyIn(Collection<UUID> keys) {
        return jpaRepository.findAllByKeyIn(keys);
    }

    @Override
    public void deleteById(UUID key) {
        jpaRepository.deleteById(key);
    }
}
