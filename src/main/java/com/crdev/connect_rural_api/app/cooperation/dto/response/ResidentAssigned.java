package com.crdev.connect_rural_api.app.cooperation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResidentAssigned {
    private String key;
    private String firstName;
    private String lastName;
    private String residentName;    // firstName + ' ' + lastName
    private String residentType;    // derivado del assignmentType de la cooperación
    private String phoneNumber;
    // Pago
    private Boolean isPaid;
    private BigDecimal amountPaid;
    private LocalDate paidAt;
    // Montos calculados
    private BigDecimal baseAmount;
    private BigDecimal lateFeeAmount;
    private BigDecimal totalAmount; // baseAmount + lateFeeAmount
    // Estado semántico: PENDIENTE | VENCIDO | PAGADO
    private String paymentStatus;
    // Tooltip de mora pagada (solo cuando isPaid=true y pagó con recargo)
    private BigDecimal lateFeeAmountPaid;   // mora efectivamente incluida en amountPaid
    private Long lateFeePeriodsCount;       // número de periodos que generaron esa mora
}
