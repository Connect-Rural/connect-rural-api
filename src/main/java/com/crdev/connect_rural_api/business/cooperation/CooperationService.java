package com.crdev.connect_rural_api.business.cooperation;

import com.crdev.connect_rural_api.app.cooperation.dto.*;
import com.crdev.connect_rural_api.business.financialObligation.FinancialObligationService;
import com.crdev.connect_rural_api.business.resident.ResidentRepository;
import com.crdev.connect_rural_api.business.residentPayment.ResidentPaymentService;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import com.crdev.connect_rural_api.data.cooperation.CooperationSpecs;
import com.crdev.connect_rural_api.data.financialObligation.FinancialObligationEntity;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import com.crdev.connect_rural_api.data.residentPayment.ResidentPaymentEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Collections;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CooperationService {

    private final CooperationRepository cooperationRepository;
    private final CooperationMapper mapper;
    private final FinancialObligationService financialObligationService;
    private final ResidentRepository residentRepository;
    private final ResidentPaymentService residentPaymentService;
    private final LateFeeCalculator lateFeeCalculator;

    public List<CooperationSummaryResponse> getList(String communityKey) {
        return cooperationRepository.findAllByCommunityKey(UUID.fromString(communityKey)).stream()
                .map(coop -> {
                    var obligations = financialObligationService.listByCooperation(coop.getKey());
                    int total = obligations.size();
                    int paid = (int) obligations.stream().filter(o -> "PAID".equals(o.getStatus())).count();
                    double progress = total == 0 ? 0 : ((double) paid / total) * 100;
                    return mapper.toSummaryResponse(coop, progress, total, total - paid, paid);
                })
                .toList();
    }

    @Transactional
    public CooperationPageResponse getPaginated(String communityKey, CooperationFilterRequest filter) {
        Specification<CooperationEntity> spec = Specification.allOf(
                CooperationSpecs.withCommunity(UUID.fromString(communityKey)),
                CooperationSpecs.withKeyword(filter.getKeyword()));
        Page<CooperationEntity> result = cooperationRepository.findAll(spec,
                PageRequest.of(filter.getPage(), filter.getSize()));

        List<CooperationSummaryResponse> items = result.stream().map(coop -> {
            var obligations = financialObligationService.listByCooperation(coop.getKey());
            int total = obligations.size();
            int paid = (int) obligations.stream().filter(o -> "PAID".equals(o.getStatus())).count();
            double progress = total == 0 ? 0 : ((double) paid / total) * 100;
            return mapper.toSummaryResponse(coop, progress, total, total - paid, paid);
        }).toList();

        return new CooperationPageResponse(items, filter.getPage(), filter.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional
    public CooperationResponse getByKey(String communityKey, String cooperationKey) {
        CooperationEntity entity = findEntity(communityKey, cooperationKey);
        List<String> assigned = new ArrayList<>();
        List<String> excluded = new ArrayList<>();
        var obligations = financialObligationService.listByCooperation(UUID.fromString(cooperationKey));

        if ("ALL_EXCEPT".equals(entity.getAssignmentType())) {
            excluded = residentRepository.findAllByCommunityKey(UUID.fromString(communityKey)).stream()
                    .filter(r -> obligations.stream().noneMatch(o -> o.getResidentKey().equals(r.getKey())))
                    .map(r -> r.getKey().toString()).toList();
        } else if ("INDIVIDUAL".equals(entity.getAssignmentType())) {
            assigned = obligations.stream().map(o -> o.getResidentKey().toString()).toList();
        }
        return mapper.toResponse(entity, assigned, excluded);
    }

    @Transactional
    public CooperationDetailResponse getDetail(String communityKey, String cooperationKey) {
        CooperationEntity coop = findEntity(communityKey, cooperationKey);
        List<FinancialObligationEntity> obligations = financialObligationService.listByCooperation(coop.getKey());

        BigDecimal baseAmount = coop.getBaseAmount();
        LocalDate today = LocalDate.now();

    
        Set<UUID> residentKeys = obligations.stream()
                .map(FinancialObligationEntity::getResidentKey).collect(Collectors.toSet());
        Map<UUID, ResidentEntity> residentMap = residentKeys.isEmpty() ? Collections.emptyMap()
                : residentRepository.findAllByKeyIn(residentKeys).stream()
                        .collect(Collectors.toMap(ResidentEntity::getKey, Function.identity()));

        Set<UUID> paidObligationKeys = obligations.stream()
                .filter(o -> "PAID".equals(o.getStatus()))
                .map(FinancialObligationEntity::getKey).collect(Collectors.toSet());
        Map<UUID, ResidentPaymentEntity> paymentByObligation = paidObligationKeys.isEmpty() ? Collections.emptyMap()
                : residentPaymentService.findPaymentsForObligations(paidObligationKeys);

        // Compute totals directly from obligations for the summary
        int total = obligations.size();
        int paid = (int) obligations.stream().filter(o -> "PAID".equals(o.getStatus())).count();
        double progress = total > 0 ? (paid * 100.0) / total : 0.0;
        CooperationSummaryResponse summaryResponse = mapper.toSummaryResponse(coop, progress, total, total - paid, paid);

        // Build per-period summaries
        List<LocalDate> periods = financialObligationService.getPeriods(coop.getKey());
        List<PeriodSummaryResponse> periodsResponse = new ArrayList<>();
        for (LocalDate periodRef : periods) {
            List<FinancialObligationEntity> periodObligations = obligations.stream()
                    .filter(o -> o.getPeriodRef().equals(periodRef)).toList();

            int totalPeriod = periodObligations.size();
            int paidPeriod = (int) periodObligations.stream().filter(o -> "PAID".equals(o.getStatus())).count();
            double progressPeriod = totalPeriod > 0 ? (paidPeriod * 100.0) / totalPeriod : 0.0;

            List<ResidentAssigned> periodAssignments = periodObligations.stream()
                    .map(o -> buildResidentAssigned(o, coop, residentMap, paymentByObligation, baseAmount, today))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            LocalDate dueDate = periodObligations.isEmpty() ? null : periodObligations.getFirst().getDueDate();
            periodsResponse.add(mapper.toPeriodSummaryResponse(
                    periodRef, dueDate, progressPeriod, totalPeriod, totalPeriod - paidPeriod, paidPeriod, periodAssignments));
        }

        return mapper.toDetailResponse(coop, periodsResponse, summaryResponse);
    }

    @Transactional
    public CooperationSummaryResponse create(String communityKey, CreateCooperationRequest request) {
        log.info("Creating cooperation: communityKey={}, name={}", communityKey, request.getName());
        CooperationEntity entity = new CooperationEntity(
                null, UUID.fromString(communityKey), request.getName(), request.getDescription(),
                request.getBaseAmount(), request.getStartDate(), request.getDueDate(),
                request.getHasLateFee(), request.getLateFeeAmount(), request.getLateFeePeriodicity(),
                request.getAssignmentType().toString(), request.getPeriodicity().toString(), request.getEndDate(),
                request.getStatus(), null, null, null);
        CooperationEntity saved = cooperationRepository.save(entity);
        List<UUID> residents = resolveResidents(communityKey, request);
        if (!residents.isEmpty()) {
            LocalDate periodRef = request.getStartDate();
            financialObligationService.createForCooperation(saved.getKey(),
                    UUID.fromString(communityKey), request.getDueDate(), periodRef, request.getBaseAmount(), residents);
        }
        return mapper.toSummaryResponse(saved);
    }

    @Transactional
    public CooperationSummaryResponse update(String communityKey, String cooperationKey,
            CreateCooperationRequest request) {
        log.info("Updating cooperation: communityKey={}, cooperationKey={}", communityKey, cooperationKey);
        CooperationEntity existing = findEntity(communityKey, cooperationKey);
        mapper.updateEntityFromRequest(request, existing);

        List<UUID> residents = resolveResidents(communityKey, request);
        financialObligationService.deleteByCooperation(UUID.fromString(cooperationKey));
        if (!residents.isEmpty()) {
            LocalDate periodRef = request.getStartDate();
            financialObligationService.createForCooperation(UUID.fromString(cooperationKey),
                    existing.getCommunityKey(), request.getDueDate(), periodRef, request.getBaseAmount(), residents);
        }
        return mapper.toSummaryResponse(cooperationRepository.save(existing));
    }

    @Transactional
    public void delete(String communityKey, String cooperationKey) {
        log.info("Deleting cooperation: communityKey={}, cooperationKey={}", communityKey, cooperationKey);
        findEntity(communityKey, cooperationKey);
        cooperationRepository.deleteById(UUID.fromString(cooperationKey));
    }

    @Transactional
    public CooperationSummaryResponse close(String communityKey, String cooperationKey) {
        log.info("Closing cooperation: communityKey={}, cooperationKey={}", communityKey, cooperationKey);
        CooperationEntity entity = findEntity(communityKey, cooperationKey);
        if ("CLOSED".equals(entity.getStatus()))
            throw new IllegalStateException("Cooperation is already closed");
        entity.setStatus("CLOSED");
        entity.setClosedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return mapper.toSummaryResponse(cooperationRepository.save(entity));
    }

    @Transactional
    public CooperationSummaryResponse reopen(String communityKey, String cooperationKey) {
        log.info("Reopening cooperation: communityKey={}, cooperationKey={}", communityKey, cooperationKey);
        CooperationEntity entity = findEntity(communityKey, cooperationKey);
        if (!"CLOSED".equals(entity.getStatus()))
            throw new IllegalStateException("Cooperation is not closed");
        entity.setStatus("ACTIVE");
        entity.setClosedAt(null);
        entity.setUpdatedAt(LocalDateTime.now());
        return mapper.toSummaryResponse(cooperationRepository.save(entity));
    }

    @Transactional
    public ResidentAssigned markAsPaid(String communityKey, String cooperationKey,
            String residentKey, BigDecimal amountPaid, LocalDate paidAt) {
        CooperationEntity cooperation = findEntity(communityKey, cooperationKey);
        LocalDate effectivePaidAt = paidAt != null ? paidAt : LocalDate.now();
        BigDecimal lateFee = lateFeeCalculator.calculate(cooperation, effectivePaidAt);
        BigDecimal amount = amountPaid != null ? amountPaid : cooperation.getBaseAmount().add(lateFee);

        FinancialObligationEntity obligation = financialObligationService.getByCooperationAndResident(
                UUID.fromString(cooperationKey), UUID.fromString(residentKey));

        log.info("Marking as paid: cooperationKey={}, residentKey={}", cooperationKey, residentKey);
        ResidentPaymentEntity payment = residentPaymentService.createPaymentForObligation(
                UUID.fromString(residentKey), amount, effectivePaidAt, obligation.getKey());
        financialObligationService.markAsPaid(obligation.getKey(), lateFee);

        ResidentEntity resident = residentRepository.findByCommunityKeyAndKey(
                UUID.fromString(communityKey), UUID.fromString(residentKey))
                .orElseThrow(() -> new IllegalArgumentException("Resident not found"));
        String fullName = resident.getFirstName()
                + (resident.getLastName() != null ? " " + resident.getLastName() : "");
        BigDecimal base = cooperation.getBaseAmount();
        BigDecimal lateFeeAmountPaid = null;
        Long lateFeePeriodsCount = null;
        if (payment.getAmount().compareTo(base) > 0) {
            lateFeeAmountPaid = payment.getAmount().subtract(base);
            lateFeePeriodsCount = lateFeeCalculator.calculatePeriods(cooperation, effectivePaidAt);
            if (lateFeePeriodsCount == 0)
                lateFeePeriodsCount = null;
        }

        return ResidentAssigned.builder()
                .key(resident.getKey().toString())
                .firstName(resident.getFirstName())
                .lastName(resident.getLastName())
                .residentName(fullName)
                .residentType(cooperation.getAssignmentType())
                .phoneNumber(resident.getPhoneNumber())
                .isPaid(true)
                .amountPaid(payment.getAmount())
                .paidAt(payment.getPaidAt())
                .baseAmount(base)
                .lateFeeAmount(BigDecimal.ZERO)
                .totalAmount(base)
                .paymentStatus("PAGADO")
                .lateFeeAmountPaid(lateFeeAmountPaid)
                .lateFeePeriodsCount(lateFeePeriodsCount)
                .build();
    }

    @Transactional
    public ResidentAssigned markAsUnpaid(String communityKey, String cooperationKey, String residentKey) {
        CooperationEntity coop = findEntity(communityKey, cooperationKey);
        FinancialObligationEntity obligation = financialObligationService.getByCooperationAndResident(
                UUID.fromString(cooperationKey), UUID.fromString(residentKey));

        log.info("Marking as unpaid: cooperationKey={}, residentKey={}", cooperationKey, residentKey);
        residentPaymentService.deletePaymentForObligation(obligation.getKey());
        financialObligationService.markAsUnpaid(obligation.getKey());

        ResidentEntity resident = residentRepository.findByCommunityKeyAndKey(
                UUID.fromString(communityKey), UUID.fromString(residentKey))
                .orElseThrow(() -> new IllegalArgumentException("Resident not found"));
        LocalDate today = LocalDate.now();
        BigDecimal lateFee = lateFeeCalculator.calculate(coop, today);
        String fullName = resident.getFirstName()
                + (resident.getLastName() != null ? " " + resident.getLastName() : "");

        return ResidentAssigned.builder()
                .key(resident.getKey().toString())
                .firstName(resident.getFirstName())
                .lastName(resident.getLastName())
                .residentName(fullName)
                .residentType(coop.getAssignmentType())
                .phoneNumber(resident.getPhoneNumber())
                .isPaid(false)
                .amountPaid(null)
                .paidAt(null)
                .baseAmount(coop.getBaseAmount())
                .lateFeeAmount(lateFee)
                .totalAmount(coop.getBaseAmount().add(lateFee))
                .paymentStatus(lateFeeCalculator.resolvePaymentStatus(false, coop.getDueDate(), today))
                .lateFeeAmountPaid(null)
                .lateFeePeriodsCount(null)
                .build();
    }

    @Transactional
    public int markAllAsPaid(String communityKey, String cooperationKey) {
        CooperationEntity coop = findEntity(communityKey, cooperationKey);
        List<FinancialObligationEntity> pending = financialObligationService.listPendingByCooperation(coop.getKey());
        if (pending.isEmpty())
            return 0;

        LocalDate today = LocalDate.now();
        BigDecimal lateFee = lateFeeCalculator.calculate(coop, today);
        BigDecimal total = coop.getBaseAmount().add(lateFee);

        log.info("Marking all as paid: cooperationKey={}, count={}", cooperationKey, pending.size());
        for (FinancialObligationEntity ob : pending) {
            residentPaymentService.createPaymentForObligation(ob.getResidentKey(), total, today, ob.getKey());
            financialObligationService.markAsPaid(ob.getKey(), lateFee);
        }
        return pending.size();
    }

    private CooperationEntity findEntity(String communityKey, String cooperationKey) {
        return cooperationRepository.findByCommunityKeyAndKey(
                UUID.fromString(communityKey), UUID.fromString(cooperationKey))
                .orElseThrow(() -> new IllegalArgumentException("Cooperation not found"));
    }

    private List<UUID> resolveResidents(String communityKey, CreateCooperationRequest request) {
        CooperationAssignmentType type = request.getAssignmentType();
        if (type == CooperationAssignmentType.INDIVIDUAL) {
            return request.getAssignedResidentKeys().stream().map(UUID::fromString).toList();
        }
        List<ResidentEntity> all = residentRepository.findAllByCommunityKey(UUID.fromString(communityKey));
        if (type == CooperationAssignmentType.ALL) {
            return all.stream().map(ResidentEntity::getKey).toList();
        }
        if (type == CooperationAssignmentType.ALL_EXCEPT) {
            return all.stream()
                    .filter(r -> !request.getExcludedResidentKeys().contains(r.getKey().toString()))
                    .map(ResidentEntity::getKey).toList();
        }
        return List.of();
    }

    private ResidentAssigned buildResidentAssigned(
            FinancialObligationEntity obligation,
            CooperationEntity coop,
            Map<UUID, ResidentEntity> residentMap,
            Map<UUID, ResidentPaymentEntity> paymentByObligation,
            BigDecimal baseAmount,
            LocalDate today) {

        ResidentEntity resident = residentMap.get(obligation.getResidentKey());
        if (resident == null) return null;

        boolean isPaid = "PAID".equals(obligation.getStatus());
        String paymentStatus = lateFeeCalculator.resolvePaymentStatus(isPaid, coop.getDueDate(), today);
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
            effectiveLateFee = lateFeeCalculator.calculate(coop, effectivePaidAt);
            lateFeePeriodsCount = lateFeeCalculator.calculatePeriods(coop, effectivePaidAt);
            if (amountPaid != null && amountPaid.compareTo(baseAmount) > 0) {
                lateFeeAmountPaid = amountPaid.subtract(baseAmount);
            } else if (effectiveLateFee.compareTo(BigDecimal.ZERO) > 0) {
                lateFeeAmountPaid = effectiveLateFee;
            }
            if (lateFeePeriodsCount == 0) lateFeePeriodsCount = null;
        } else {
            effectiveLateFee = lateFeeCalculator.calculate(coop, today);
        }

        String fullName = resident.getFirstName()
                + (resident.getLastName() != null ? " " + resident.getLastName() : "");
        return ResidentAssigned.builder()
                .key(resident.getKey().toString())
                .firstName(resident.getFirstName())
                .lastName(resident.getLastName())
                .residentName(fullName)
                .residentType(coop.getAssignmentType())
                .phoneNumber(resident.getPhoneNumber())
                .isPaid(isPaid)
                .amountPaid(amountPaid)
                .paidAt(paidAt)
                .baseAmount(baseAmount)
                .lateFeeAmount(effectiveLateFee)
                .totalAmount(baseAmount.add(effectiveLateFee))
                .paymentStatus(paymentStatus)
                .lateFeeAmountPaid(lateFeeAmountPaid)
                .lateFeePeriodsCount(lateFeePeriodsCount)
                .build();
    }
}
