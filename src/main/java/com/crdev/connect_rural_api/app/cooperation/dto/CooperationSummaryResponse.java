package com.crdev.connect_rural_api.app.cooperation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CooperationSummaryResponse {
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime closedAt;
}
