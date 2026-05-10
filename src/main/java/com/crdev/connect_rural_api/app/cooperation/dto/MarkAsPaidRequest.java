package com.crdev.connect_rural_api.app.cooperation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MarkAsPaidRequest {
    private LocalDate paidAt;
    private BigDecimal amountPaid;
}
