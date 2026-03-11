package com.crdev.connect_rural_api.business.cooperation.usecases;

import com.crdev.connect_rural_api.app.cooperation.dto.response.ResidentAssigned;
import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.LateFeeCalculator;
import com.crdev.connect_rural_api.business.cooperationResident.CooperationResidentService;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@AllArgsConstructor
@Slf4j
public class MarkAsUnpaidUseCase {

    private final CooperationService cooperationService;
    private final CooperationResidentService cooperationResidentService;
    private final ResidentService residentService;
    private final LateFeeCalculator lateFeeCalculator;

    /**
     * Desmarca el pago de un residente y devuelve el ResidentAssigned recalculado.
     * amountPaid y paidAt quedan null; lateFeeAmount y totalAmount se recalculan
     * a la fecha de hoy para reflejar el estado actual de la deuda.
     */
    public ResidentAssigned execute(String communityKey, String cooperationKey, String residentKey) {
        CooperationEntity cooperation = cooperationService.getByKey(communityKey, cooperationKey);

        log.info("Marking as unpaid: cooperationKey={}, residentKey={}", cooperationKey, residentKey);
        cooperationResidentService.markAsUnpaid(cooperationKey, residentKey);

        ResidentEntity resident = residentService.getByKey(communityKey, residentKey);

        LocalDate today = LocalDate.now();
        String paymentStatus = lateFeeCalculator.resolvePaymentStatus(false, cooperation.getDueDate(), today);
        BigDecimal lateFee = lateFeeCalculator.calculate(cooperation, today);
        BigDecimal totalAmount = cooperation.getBaseAmount().add(lateFee);
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
                null,       // amountPaid → null tras desmarcar
                null,       // paidAt     → null tras desmarcar
                cooperation.getBaseAmount(),
                lateFee,    // recargo recalculado a hoy
                totalAmount,
                paymentStatus,
                null,       // lateFeeAmountPaid → no aplica (no está pagado)
                null        // lateFeePeriodsCount → no aplica
        );
    }
}
