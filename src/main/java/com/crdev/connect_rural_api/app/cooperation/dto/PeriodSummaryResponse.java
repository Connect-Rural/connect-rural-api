package com.crdev.connect_rural_api.app.cooperation.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodSummaryResponse {
    private LocalDate periodRef;
    private String label;
    private LocalDate dueDate;
    private int totalAssignedResidents;
    private int paidResidents;
    private int pendingResidents;
    private double progressPercentage;
    private List<ResidentAssigned> assignments;
}  