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
public class MarkAsUnpaidUseCase {

    private final CooperationService cooperationService;
    private final FinancialObligationService financialObligationService;
    private final ResidentPaymentService residentPaymentService;
    private final ResidentService residentService;
    private final LateFeeCalculator lateFeeCalculator;

    @Transactional
    public ResidentAssigned execute(String communityKey, String cooperationKey, String residentKey) {
        CooperationEntity cooperation = cooperationService.getByKey(communityKey, cooperationKey);

        FinancialObligationEntity obligation = financialObligationService.getByCooperationAndResident(
                UUID.fromString(cooperationKey), UUID.fromString(residentKey));

        log.info("Marking as unpaid: cooperationKey={}, residentKey={}", cooperationKey, residentKey);

        residentPaymentService.deletePaymentForObligation(obligation.getKey());
        financialObligationService.markAsUnpaid(obligation.getKey());

        ResidentEntity resident = residentService.getByKey(communityKey, residentKey);
        LocalDate today = LocalDate.now();
        BigDecimal lateFee = lateFeeCalculator.calculate(cooperation, today);
        BigDecimal totalAmount = cooperation.getBaseAmount().add(lateFee);
        String paymentStatus = lateFeeCalculator.resolvePaymentStatus(false, cooperation.getDueDate(), today);
        String fullName = resident.getFirstName()
                + (resident.getLastName() != null ? " " + resident.getLastName() : "");

        return new ResidentAssigned(
                resident.getKey().toString(),
                resident.getFirstName(),
                resident.getLastName(),
                fullName,
                cooperation.getAssignmentType(),
                resident.getPhoneNumber(),
                false,
                null,
                null,
                cooperation.getBaseAmount(),
                lateFee,
                totalAmount,
                paymentStatus,
                null,
                null
        );
    }
}
