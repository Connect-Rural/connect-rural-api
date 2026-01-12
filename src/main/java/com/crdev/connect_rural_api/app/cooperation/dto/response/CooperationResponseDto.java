package com.crdev.connect_rural_api.app.cooperation.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CooperationResponseDto {
    private String key;
    private String communityKey;
    private String name;
    private String description;
    private BigDecimal baseAmount;
    private LocalDate startDate;
    private LocalDate dueDate;
    private String status;
    private String assignmentType;
    private List<String> assignedResidentKeys;
    private List<String> excludedResidentKeys;
    private Boolean hasLateFee;
    private BigDecimal lateFeeAmount;
    private String lateFeePeriodicity;
}
