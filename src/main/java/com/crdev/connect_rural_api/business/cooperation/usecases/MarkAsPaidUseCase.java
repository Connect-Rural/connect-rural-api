package com.crdev.connect_rural_api.business.cooperation.usecases;

import com.crdev.connect_rural_api.app.cooperation.dto.response.ResidentAssigned;
import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.LateFeeCalculator;
import com.crdev.connect_rural_api.business.cooperationResident.CooperationResidentService;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import com.crdev.connect_rural_api.data.cooperationResident.CooperationResidentEntity;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@AllArgsConstructor
@Slf4j
public class MarkAsPaidUseCase {

    private final CooperationService cooperationService;
    private final CooperationResidentService cooperationResidentService;
    private final ResidentService residentService;
    private final LateFeeCalculator lateFeeCalculator;

    /**
     * Marca el pago de un residente y devuelve el ResidentAssigned actualizado.
     *
     * - paidAt: fecha efectiva del pago. Si es null se usa hoy.
     * - amountPaid: si no se provee, se calcula como baseAmount + recargo acumulado
     *   hasta effectivePaidAt (según lateFeePeriod y dueDate de la cooperación).
     * - Una vez pagado, lateFeeAmount y totalAmount se devuelven como 0 / baseAmount
     *   porque el recargo ya está absorbido en amountPaid.
     */
    public ResidentAssigned execute(String communityKey, String cooperationKey,
                                    String residentKey, BigDecimal amountPaid, LocalDate paidAt) {

        CooperationEntity cooperation = cooperationService.getByKey(communityKey, cooperationKey);
        LocalDate effectivePaidAt = paidAt != null ? paidAt : LocalDate.now();

        BigDecimal amount;
        if (amountPaid != null) {
            amount = amountPaid;
        } else {
            BigDecimal lateFee = lateFeeCalculator.calculate(cooperation, effectivePaidAt);
            amount = cooperation.getBaseAmount().add(lateFee);
        }

        log.info("Marking as paid: cooperationKey={}, residentKey={}, paidAt={}, amount={}",
                cooperationKey, residentKey, effectivePaidAt, amount);
        CooperationResidentEntity assignment =
                cooperationResidentService.markAsPaid(cooperationKey, residentKey, amount, effectivePaidAt);

        ResidentEntity resident = residentService.getByKey(communityKey, residentKey);
        String fullName = resident.getFirstName()
                + (resident.getLastName() != null ? " " + resident.getLastName() : "");

        // Tooltip: mora efectivamente incluida en lo que pagó
        BigDecimal baseAmount = cooperation.getBaseAmount();
        BigDecimal lateFeeAmountPaid = null;
        Long lateFeePeriodsCount = null;
        if (assignment.getAmountPaid() != null && assignment.getAmountPaid().compareTo(baseAmount) > 0) {
            lateFeeAmountPaid = assignment.getAmountPaid().subtract(baseAmount);
            lateFeePeriodsCount = lateFeeCalculator.calculatePeriods(cooperation, effectivePaidAt);
        }

        return new ResidentAssigned(
                resident.getKey().toString(),
                resident.getFirstName(),
                resident.getLastName(),
                fullName,
                cooperation.getAssignmentType(),
                resident.getPhoneNumber(),
                true,
                assignment.getAmountPaid(),
                assignment.getPaidAt(),
                baseAmount,
                BigDecimal.ZERO,    // lateFeeAmount → 0 (absorbido en amountPaid)
                baseAmount,         // totalAmount → baseAmount (referencia base)
                "PAGADO",
                lateFeeAmountPaid,
                lateFeePeriodsCount
        );
    }
}
