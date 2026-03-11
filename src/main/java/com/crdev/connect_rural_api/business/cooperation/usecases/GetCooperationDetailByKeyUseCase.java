package com.crdev.connect_rural_api.business.cooperation.usecases;

import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationDetailResponseDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.ResidentAssigned;
import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.LateFeeCalculator;
import com.crdev.connect_rural_api.business.cooperation.mapper.CooperationAppMapper;
import com.crdev.connect_rural_api.business.cooperationResident.CooperationResidentService;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import com.crdev.connect_rural_api.data.cooperationResident.CooperationResidentEntity;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
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
    private final CooperationResidentService cooperationResidentService;
    private final ResidentService residentService;
    private final CooperationAppMapper mapper;
    private final LateFeeCalculator lateFeeCalculator;

    @Transactional
    public CooperationDetailResponseDto execute(String communityKey, String cooperationKey) {

        CooperationEntity cooperationEntity = cooperationService.getByKey(communityKey, cooperationKey);

        List<CooperationResidentEntity> cooperationResidents =
                cooperationResidentService.listByCooperation(cooperationKey);

        List<ResidentAssigned> assignments = new ArrayList<>();

        if (!cooperationResidents.isEmpty()) {
            Map<UUID, CooperationResidentEntity> assignmentByResident = cooperationResidents.stream()
                    .collect(Collectors.toMap(CooperationResidentEntity::getResidentKey, r -> r));

            Map<UUID, ResidentEntity> residentMap =
                    residentService.getByKeys(assignmentByResident.keySet());

            BigDecimal baseAmount = cooperationEntity.getBaseAmount();
            String residentType = cooperationEntity.getAssignmentType();
            LocalDate today = LocalDate.now();

            assignments = cooperationResidents.stream()
                    .map(assignment -> {
                        ResidentEntity resident = residentMap.get(assignment.getResidentKey());
                        if (resident == null) return null;

                        boolean isPaid = Boolean.TRUE.equals(assignment.getIsPaid());
                        String paymentStatus = lateFeeCalculator.resolvePaymentStatus(
                                isPaid, cooperationEntity.getDueDate(), today);

                        // Tooltip de mora: cuánto pagó de mora y cuántos periodos
                        BigDecimal lateFeeAmountPaid = null;
                        Long lateFeePeriodsCount = null;
                        BigDecimal effectiveLateFee;

                        if (isPaid) {
                            // Para pagados: recargo calculado contra paidAt (igual que MarkAsPaidUseCase)
                            LocalDate paidAt = assignment.getPaidAt() != null ? assignment.getPaidAt() : today;
                            effectiveLateFee = lateFeeCalculator.calculate(cooperationEntity, paidAt);
                            lateFeePeriodsCount = lateFeeCalculator.calculatePeriods(cooperationEntity, paidAt);
                            // Si pagó más que baseAmount, la diferencia es la mora que incluyó
                            if (assignment.getAmountPaid() != null
                                    && assignment.getAmountPaid().compareTo(baseAmount) > 0) {
                                lateFeeAmountPaid = assignment.getAmountPaid().subtract(baseAmount);
                            } else {
                                lateFeeAmountPaid = effectiveLateFee.compareTo(BigDecimal.ZERO) > 0
                                        ? effectiveLateFee : null;
                            }
                            if (lateFeePeriodsCount == 0) lateFeePeriodsCount = null;
                        } else {
                            // Para no pagados: recargo calculado contra hoy
                            effectiveLateFee = lateFeeCalculator.calculate(cooperationEntity, today);
                        }

                        BigDecimal totalAmount = baseAmount.add(effectiveLateFee);

                        String fullName = resident.getFirstName()
                                + (resident.getLastName() != null ? " " + resident.getLastName() : "");

                        ResidentAssigned ra = new ResidentAssigned(
                                resident.getKey().toString(),
                                resident.getFirstName(),
                                resident.getLastName(),
                                fullName,
                                residentType,
                                resident.getPhoneNumber(),
                                isPaid,
                                assignment.getAmountPaid(),
                                assignment.getPaidAt(),
                                baseAmount,
                                effectiveLateFee,
                                totalAmount,
                                paymentStatus,
                                lateFeeAmountPaid,
                                lateFeePeriodsCount
                        );
                        return ra;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        int totalAssignedResidents = assignments.size();
        int paidResidents = (int) assignments.stream()
                .filter(ResidentAssigned::getIsPaid)
                .count();
        int pendingResidents = totalAssignedResidents - paidResidents;

        double progressPercentage = totalAssignedResidents > 0
                ? (paidResidents * 100.0) / totalAssignedResidents
                : 0.0;

        return mapper.toDetailResponseDto(
                cooperationEntity,
                assignments,
                progressPercentage,
                totalAssignedResidents,
                paidResidents,
                pendingResidents
        );
    }
}
