package com.crdev.connect_rural_api.business.financialObligation;

import com.crdev.connect_rural_api.data.financialObligation.FinancialObligationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinancialObligationService {

    private static final String ORIGIN_COOPERATION = "COOPERATION";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PAID = "PAID";

    private final FinancialObligationRepository repository;

    @Transactional
    public void createForCooperation(UUID cooperationKey, UUID communityKey,
                                     LocalDate dueDate, BigDecimal amountDue,
                                     List<UUID> residentKeys) {
        List<FinancialObligationEntity> obligations = residentKeys.stream()
                .map(residentKey -> new FinancialObligationEntity(
                        null,
                        residentKey,
                        communityKey,
                        ORIGIN_COOPERATION,
                        cooperationKey,
                        amountDue,
                        BigDecimal.ZERO,
                        STATUS_PENDING,
                        dueDate,
                        null,
                        null
                ))
                .toList();
        repository.saveAll(obligations);
    }

    public List<FinancialObligationEntity> listByCooperation(UUID cooperationKey) {
        return repository.findByOriginTypeAndOriginId(ORIGIN_COOPERATION, cooperationKey);
    }

    public List<FinancialObligationEntity> listPendingByCooperation(UUID cooperationKey) {
        return repository.findByOriginTypeAndOriginIdAndStatus(ORIGIN_COOPERATION, cooperationKey, STATUS_PENDING);
    }

    public FinancialObligationEntity getByCooperationAndResident(UUID cooperationKey, UUID residentKey) {
        return repository.findByOriginTypeAndOriginIdAndResidentKey(ORIGIN_COOPERATION, cooperationKey, residentKey)
                .orElseThrow(() -> new IllegalArgumentException("Financial obligation not found for resident"));
    }

    @Transactional
    public FinancialObligationEntity markAsPaid(UUID obligationKey, BigDecimal lateFeeApplied) {
        FinancialObligationEntity obligation = repository.findById(obligationKey)
                .orElseThrow(() -> new IllegalArgumentException("Obligation not found"));
        obligation.setStatus(STATUS_PAID);
        obligation.setLateFeeApplied(lateFeeApplied != null ? lateFeeApplied : BigDecimal.ZERO);
        return repository.save(obligation);
    }

    @Transactional
    public void deleteByCooperation(UUID cooperationKey) {
        repository.deleteByOriginTypeAndOriginId(ORIGIN_COOPERATION, cooperationKey);
    }

    @Transactional
    public FinancialObligationEntity markAsUnpaid(UUID obligationKey) {
        FinancialObligationEntity obligation = repository.findById(obligationKey)
                .orElseThrow(() -> new IllegalArgumentException("Obligation not found"));
        obligation.setStatus(STATUS_PENDING);
        obligation.setLateFeeApplied(BigDecimal.ZERO);
        return repository.save(obligation);
    }
}
