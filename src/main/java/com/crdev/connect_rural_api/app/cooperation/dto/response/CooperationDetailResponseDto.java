package com.crdev.connect_rural_api.app.cooperation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CooperationDetailResponseDto {
    private String key;
    private String communityKey;
    private String name;
    private String description;
    private BigDecimal baseAmount;
    private LocalDate startDate;
    private LocalDate dueDate;
    private String status;
    private List<ResidentAssigned> assignments;
    private double progressPercentage;
    private int totalAssignedResidents;
    private int paidResidents;
    private int pendingResidents;
    private Boolean hasLateFee;
    private BigDecimal lateFeeAmount;
    private String lateFeePeriodicity;
}
