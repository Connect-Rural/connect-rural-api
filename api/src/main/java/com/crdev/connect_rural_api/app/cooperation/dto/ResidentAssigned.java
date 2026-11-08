package com.crdev.connect_rural_api.app.cooperation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResidentAssigned {
    private String key;
    private String firstName;
    private String lastName;
    private String residentName;
    private String residentType;
    private String phoneNumber;
    private Boolean isPaid;
    private BigDecimal amountPaid;
    private LocalDate paidAt;
    private BigDecimal baseAmount;
    private BigDecimal lateFeeAmount;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private BigDecimal lateFeeAmountPaid;
    private Long lateFeePeriodsCount;
}
