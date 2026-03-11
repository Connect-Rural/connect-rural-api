package com.crdev.connect_rural_api.app.cooperation.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MarkAsPaidRequestDto {
    /** Fecha del pago. Si es null se usa la fecha actual. */
    private LocalDate paidAt;
    /** Monto pagado. Si es null se usa el baseAmount de la cooperación. */
    private BigDecimal amountPaid;
}
