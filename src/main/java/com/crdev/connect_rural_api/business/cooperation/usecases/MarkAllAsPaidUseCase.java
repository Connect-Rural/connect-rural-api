package com.crdev.connect_rural_api.business.cooperation.usecases;

import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.LateFeeCalculator;
import com.crdev.connect_rural_api.business.cooperationResident.CooperationResidentService;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@AllArgsConstructor
@Slf4j
public class MarkAllAsPaidUseCase {

    private final CooperationService cooperationService;
    private final CooperationResidentService cooperationResidentService;
    private final LateFeeCalculator lateFeeCalculator;

    /**
     * Marca todos los residentes pendientes como pagados usando hoy como fecha de pago.
     * El monto aplicado es baseAmount + recargo calculado a hoy según lateFeePeriod.
     *
     * @return número de registros actualizados
     */
    public int execute(String communityKey, String cooperationKey) {
        CooperationEntity cooperation = cooperationService.getByKey(communityKey, cooperationKey);

        LocalDate today = LocalDate.now();
        BigDecimal lateFee = lateFeeCalculator.calculate(cooperation, today);
        BigDecimal totalAmount = cooperation.getBaseAmount().add(lateFee);

        log.info("Marking all as paid: cooperationKey={}, amount={} (base={} + lateFee={})",
                cooperationKey, totalAmount, cooperation.getBaseAmount(), lateFee);
        return cooperationResidentService.markAllAsPaid(cooperationKey, totalAmount);
    }
}
