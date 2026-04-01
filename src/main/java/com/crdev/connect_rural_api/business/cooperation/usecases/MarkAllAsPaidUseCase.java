package com.crdev.connect_rural_api.business.cooperation.usecases;

import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.LateFeeCalculator;
import com.crdev.connect_rural_api.business.financialObligation.FinancialObligationService;
import com.crdev.connect_rural_api.business.residentPayment.ResidentPaymentService;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import com.crdev.connect_rural_api.data.financialObligation.FinancialObligationEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class MarkAllAsPaidUseCase {

    private final CooperationService cooperationService;
    private final FinancialObligationService financialObligationService;
    private final ResidentPaymentService residentPaymentService;
    private final LateFeeCalculator lateFeeCalculator;

    @Transactional
    public int execute(String communityKey, String cooperationKey) {
        CooperationEntity cooperation = cooperationService.getByKey(communityKey, cooperationKey);

        List<FinancialObligationEntity> pending =
                financialObligationService.listPendingByCooperation(cooperation.getKey());

        if (pending.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        BigDecimal lateFee = lateFeeCalculator.calculate(cooperation, today);
        BigDecimal totalAmount = cooperation.getBaseAmount().add(lateFee);

        log.info("Marking all as paid: cooperationKey={}, count={}, amount={} (base={} + lateFee={})",
                cooperationKey, pending.size(), totalAmount, cooperation.getBaseAmount(), lateFee);

        for (FinancialObligationEntity obligation : pending) {
            residentPaymentService.createPaymentForObligation(
                    obligation.getResidentKey(), totalAmount, today, obligation.getKey());
            financialObligationService.markAsPaid(obligation.getKey(), lateFee);
        }

        return pending.size();
    }
}
