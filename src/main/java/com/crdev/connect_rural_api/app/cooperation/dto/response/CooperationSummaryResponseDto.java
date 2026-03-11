package com.crdev.connect_rural_api.app.cooperation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CooperationSummaryResponseDto {
    private String key;
    private String communityKey;
    private String name;
    private String description;
    private BigDecimal baseAmount;
    private String status;
    private double progressPercentage;
    private int totalAssignedResidents;
    private int paidResidents;
    private int pendingResidents;
    private LocalDate startDate;
    private LocalDate dueDate;

}

