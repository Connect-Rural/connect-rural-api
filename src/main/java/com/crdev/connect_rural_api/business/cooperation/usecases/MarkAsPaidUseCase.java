package com.crdev.connect_rural_api.business.cooperation.usecases;

import com.crdev.connect_rural_api.app.cooperation.dto.response.ResidentAssigned;
import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.LateFeeCalculator;
import com.crdev.connect_rural_api.business.financialObligation.FinancialObligationService;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.business.residentPayment.ResidentPaymentService;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import com.crdev.connect_rural_api.data.financialObligation.FinancialObligationEntity;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import com.crdev.connect_rural_api.data.residentPayment.ResidentPaymentEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
public class MarkAsPaidUseCase {

    private final CooperationService cooperationService;
    private final FinancialObligationService financialObligationService;
    private final ResidentPaymentService residentPaymentService;
    private final ResidentService residentService;
    private final LateFeeCalculator lateFeeCalculator;

    @Transactional
    public ResidentAssigned execute(String communityKey, String cooperationKey,
                                    String residentKey, BigDecimal amountPaid, LocalDate paidAt) {

        CooperationEntity cooperation = cooperationService.getByKey(communityKey, cooperationKey);
        LocalDate effectivePaidAt = paidAt != null ? paidAt : LocalDate.now();

        BigDecimal lateFee = lateFeeCalculator.calculate(cooperation, effectivePaidAt);
        BigDecimal amount = amountPaid != null ? amountPaid : cooperation.getBaseAmount().add(lateFee);

        FinancialObligationEntity obligation = financialObligationService.getByCooperationAndResident(
                UUID.fromString(cooperationKey), UUID.fromString(residentKey));

        log.info("Marking as paid: cooperationKey={}, residentKey={}, paidAt={}, amount={}",
                cooperationKey, residentKey, effectivePaidAt, amount);

        ResidentPaymentEntity payment = residentPaymentService.createPaymentForObligation(
                UUID.fromString(residentKey), amount, effectivePaidAt, obligation.getKey());

        financialObligationService.markAsPaid(obligation.getKey(), lateFee);

        ResidentEntity resident = residentService.getByKey(communityKey, residentKey);
        String fullName = resident.getFirstName()
                + (resident.getLastName() != null ? " " + resident.getLastName() : "");

        BigDecimal baseAmount = cooperation.getBaseAmount();
        BigDecimal lateFeeAmountPaid = null;
        Long lateFeePeriodsCount = null;
        if (payment.getAmount().compareTo(baseAmount) > 0) {
            lateFeeAmountPaid = payment.getAmount().subtract(baseAmount);
            lateFeePeriodsCount = lateFeeCalculator.calculatePeriods(cooperation, effectivePaidAt);
            if (lateFeePeriodsCount == 0) lateFeePeriodsCount = null;
        }

        return new ResidentAssigned(
                resident.getKey().toString(),
                resident.getFirstName(),
                resident.getLastName(),
                fullName,
                cooperation.getAssignmentType(),
                resident.getPhoneNumber(),
                true,
                payment.getAmount(),
                payment.getPaidAt(),
                baseAmount,
                BigDecimal.ZERO,
                baseAmount,
                "PAGADO",
                lateFeeAmountPaid,
                lateFeePeriodsCount
        );
    }
}
