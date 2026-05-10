package com.crdev.connect_rural_api.business.residentpayment;

import com.crdev.connect_rural_api.data.paymentallocation.PaymentAllocationEntity;
import com.crdev.connect_rural_api.data.residentpayment.ResidentPaymentEntity;
import com.crdev.connect_rural_api.business.paymentallocation.PaymentAllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResidentPaymentService {

    private static final String DEFAULT_METHOD = "CASH";
    private static final String REFERENCE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final Random RANDOM = new Random();

    private final ResidentPaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;

    private String generateReference(LocalDate date) {
        String datePart = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        StringBuilder suffix = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            suffix.append(REFERENCE_CHARS.charAt(RANDOM.nextInt(REFERENCE_CHARS.length())));
        }
        return "PAY-" + datePart + "-" + suffix;
    }

    @Transactional
    public ResidentPaymentEntity createPaymentForObligation(UUID residentKey, BigDecimal amount,
                                                             LocalDate paidAt, UUID obligationKey) {
        ResidentPaymentEntity payment = new ResidentPaymentEntity(
                null, residentKey, amount, DEFAULT_METHOD, generateReference(paidAt), paidAt, null, null
        );
        ResidentPaymentEntity saved = paymentRepository.save(payment);

        PaymentAllocationEntity allocation = new PaymentAllocationEntity(
                null, saved.getKey(), obligationKey, amount, null
        );
        allocationRepository.save(allocation);

        return saved;
    }

    @Transactional
    public void deletePaymentForObligation(UUID obligationKey) {
        allocationRepository.findByObligationKey(obligationKey).ifPresent(allocation -> {
            UUID paymentKey = allocation.getPaymentKey();
            allocationRepository.delete(allocation);
            paymentRepository.deleteById(paymentKey);
        });
    }

    public Optional<ResidentPaymentEntity> findPaymentForObligation(UUID obligationKey) {
        return allocationRepository.findByObligationKey(obligationKey)
                .flatMap(allocation -> paymentRepository.findById(allocation.getPaymentKey()));
    }

    public Map<UUID, ResidentPaymentEntity> findPaymentsForObligations(Collection<UUID> obligationKeys) {
        List<PaymentAllocationEntity> allocations = allocationRepository.findByObligationKeyIn(obligationKeys);
        if (allocations.isEmpty()) return Map.of();

        List<UUID> paymentKeys = allocations.stream()
                .map(PaymentAllocationEntity::getPaymentKey)
                .toList();

        Map<UUID, ResidentPaymentEntity> paymentByKey = paymentRepository.findAllByKeyIn(paymentKeys)
                .stream()
                .collect(Collectors.toMap(ResidentPaymentEntity::getKey, p -> p));

        return allocations.stream()
                .filter(a -> paymentByKey.containsKey(a.getPaymentKey()))
                .collect(Collectors.toMap(
                        PaymentAllocationEntity::getObligationKey,
                        a -> paymentByKey.get(a.getPaymentKey())
                ));
    }
}
