package com.crdev.connect_rural_api.app.cooperation.dto.request;

import com.crdev.connect_rural_api.business.cooperation.enums.CooperationAssignmentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCooperationRequestDto {
    @NotBlank(message = "El nombre de la cooperacion es obligatorio")
    private String name;
    private String description;
    @NotNull(message = "El monto base es obligatorio")
    private BigDecimal baseAmount;
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;
    private LocalDate dueDate;
    private String status = "ACTIVE";
    @NotNull(message = "El tipo de asignación es obligatorio")
    private CooperationAssignmentType assignmentType = CooperationAssignmentType.ALL;
    private List<String> assignedResidentKeys;
    private List<String> excludedResidentKeys;
    private Boolean hasLateFee = false;
    private BigDecimal lateFeeAmount = BigDecimal.ZERO;
    private String lateFeePeriodicity = "MONTHLY";
}
