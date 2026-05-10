package com.crdev.connect_rural_api.app.cooperation.dto;

import com.crdev.connect_rural_api.business.cooperation.CooperationPeriodicity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CooperationDetailResponse {

    // private String key;
    // private String communityKey;
    // private String name;
    // private String description;
    // private BigDecimal baseAmount;
    // private String status;
    // private CooperationPeriodicity periodicity;
    // private double progressPercentage;
    // private int totalAssignedResidents;
    // private int paidResidents;
    // private int pendingResidents;
    // private LocalDate startDate;
    // private LocalDate dueDate;
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // private LocalDateTime closedAt;
    private CooperationSummaryResponse info;
    private List<PeriodSummaryResponse> periods;
    private Boolean hasLateFee;
    private BigDecimal lateFeeAmount;
    private String lateFeePeriodicity;
}
