package com.crdev.connect_rural_api.business.cooperation.usecases;

import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationDetailResponseDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.ResidentAssigned;
import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.LateFeeCalculator;
import com.crdev.connect_rural_api.business.cooperation.mapper.CooperationAppMapper;
import com.crdev.connect_rural_api.business.financialObligation.FinancialObligationService;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.business.residentPayment.ResidentPaymentService;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import com.crdev.connect_rural_api.data.financialObligation.FinancialObligationEntity;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import com.crdev.connect_rural_api.data.residentPayment.ResidentPaymentEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class GetCooperationDetailByKeyUseCase {
    private final CooperationService cooperationService;
    private final FinancialObligationService financialObligationService;
    private final ResidentPaymentService residentPaymentService;
    private final ResidentService residentService;
    private final CooperationAppMapper mapper;
    private final LateFeeCalculator lateFeeCalculator;

    @Transactional
    public CooperationDetailResponseDto execute(String communityKey, String cooperationKey) {
        CooperationEntity cooperation = cooperationService.getByKey(communityKey, cooperationKey);

        List<FinancialObligationEntity> obligations =
                financialObligationService.listByCooperation(cooperation.getKey());

        List<ResidentAssigned> assignments = new ArrayList<>();

        if (!obligations.isEmpty()) {
            Set<UUID> residentKeys = obligations.stream()
                    .map(FinancialObligationEntity::getResidentKey)
                    .collect(Collectors.toSet());

            Map<UUID, ResidentEntity> residentMap = residentService.getByKeys(residentKeys);

            Set<UUID> paidObligationKeys = obligations.stream()
                    .filter(o -> "PAID".equals(o.getStatus()))
                    .map(FinancialObligationEntity::getKey)
                    .collect(Collectors.toSet());

            Map<UUID, ResidentPaymentEntity> paymentByObligation =
                    residentPaymentService.findPaymentsForObligations(paidObligationKeys);

            BigDecimal baseAmount = cooperation.getBaseAmount();
            String residentType = cooperation.getAssignmentType();
            LocalDate today = LocalDate.now();

            assignments = obligations.stream()
                    .map(obligation -> {
                        ResidentEntity resident = residentMap.get(obligation.getResidentKey());
                        if (resident == null) return null;

                        boolean isPaid = "PAID".equals(obligation.getStatus());
                        String paymentStatus = lateFeeCalculator.resolvePaymentStatus(
                                isPaid, cooperation.getDueDate(), today);

                        BigDecimal lateFeeAmountPaid = null;
                        Long lateFeePeriodsCount = null;
                        BigDecimal effectiveLateFee;
                        BigDecimal amountPaid = null;
                        LocalDate paidAt = null;

                        if (isPaid) {
                            ResidentPaymentEntity payment = paymentByObligation.get(obligation.getKey());
                            if (payment != null) {
                                amountPaid = payment.getAmount();
                                paidAt = payment.getPaidAt();
                            }
                            LocalDate effectivePaidAt = paidAt != null ? paidAt : today;
                            effectiveLateFee = lateFeeCalculator.calculate(cooperation, effectivePaidAt);
                            lateFeePeriodsCount = lateFeeCalculator.calculatePeriods(cooperation, effectivePaidAt);
                            if (amountPaid != null && amountPaid.compareTo(baseAmount) > 0) {
                                lateFeeAmountPaid = amountPaid.subtract(baseAmount);
                            } else if (effectiveLateFee.compareTo(BigDecimal.ZERO) > 0) {
                                lateFeeAmountPaid = effectiveLateFee;
                            }
                            if (lateFeePeriodsCount == 0) lateFeePeriodsCount = null;
                        } else {
                            effectiveLateFee = lateFeeCalculator.calculate(cooperation, today);
                        }

                        BigDecimal totalAmount = baseAmount.add(effectiveLateFee);
                        String fullName = resident.getFirstName()
                                + (resident.getLastName() != null ? " " + resident.getLastName() : "");

                        return new ResidentAssigned(
                                resident.getKey().toString(),
                                resident.getFirstName(),
                                resident.getLastName(),
                                fullName,
                                residentType,
                                resident.getPhoneNumber(),
                                isPaid,
                                amountPaid,
                                paidAt,
                                baseAmount,
                                effectiveLateFee,
                                totalAmount,
                                paymentStatus,
                                lateFeeAmountPaid,
                                lateFeePeriodsCount
                        );
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        int totalAssigned = assignments.size();
        int paid = (int) assignments.stream().filter(ResidentAssigned::getIsPaid).count();
        int pending = totalAssigned - paid;
        double progress = totalAssigned > 0 ? (paid * 100.0) / totalAssigned : 0.0;

        return mapper.toDetailResponseDto(cooperation, assignments, progress, totalAssigned, paid, pending);
    }
}
