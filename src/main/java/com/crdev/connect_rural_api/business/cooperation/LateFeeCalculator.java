package com.crdev.connect_rural_api.business.cooperation;

import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Calcula el recargo acumulado de una cooperación vencida.
 *
 * Lógica:
 *  - Si allowLateFee=false, dueDate=null, o la fecha de referencia no supera dueDate → recargo 0.
 *  - El número de periodos se calcula con techo (ceiling) desde dueDate hasta referenceDate.
 *  - Cada periodo iniciado se cobra completo: 1 día de retraso = 1 periodo.
 *  - El recargo total = lateFeeAmount × ceil(daysLate / periodDays).
 *
 * Frecuencias soportadas (lateFeePeriod):
 *  - DAILY   → 1 día por periodo
 *  - WEEKLY  → 7 días por periodo
 *  - MONTHLY → 30 días por periodo (aproximación calendario simple)
 *  - YEARLY  → 365 días por periodo
 *  - Cualquier otro valor → recargo fijo (1 periodo)
 */
@Component
@Slf4j
public class LateFeeCalculator {

    /**
     * Calcula el recargo total para la fecha de referencia dada.
     *
     * @param cooperation   entidad de la cooperación con su configuración de recargo
     * @param referenceDate fecha contra la que se evalúa el vencimiento (hoy al mostrar, paidAt al pagar)
     * @return recargo acumulado (nunca negativo)
     */
    public BigDecimal calculate(CooperationEntity cooperation, LocalDate referenceDate) {
        log.info("LateFee guard check: cooperationKey={}, allowLateFee={}, lateFeeAmount={}, dueDate={}, referenceDate={}",
                cooperation.getKey(), cooperation.getAllowLateFee(), cooperation.getLateFeeAmount(),
                cooperation.getDueDate(), referenceDate);
        if (!Boolean.TRUE.equals(cooperation.getAllowLateFee())) {
            log.info("LateFee ZERO: allowLateFee is false/null");
            return BigDecimal.ZERO;
        }
        if (cooperation.getLateFeeAmount() == null || cooperation.getLateFeeAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.info("LateFee ZERO: lateFeeAmount is null or <= 0 (value={})", cooperation.getLateFeeAmount());
            return BigDecimal.ZERO;
        }
        if (cooperation.getDueDate() == null) {
            log.info("LateFee ZERO: dueDate is null");
            return BigDecimal.ZERO;
        }
        if (!referenceDate.isAfter(cooperation.getDueDate())) {
            log.info("LateFee ZERO: referenceDate {} is not after dueDate {}", referenceDate, cooperation.getDueDate());
            return BigDecimal.ZERO;
        }

        long daysLate = ChronoUnit.DAYS.between(cooperation.getDueDate(), referenceDate);
        long periodDays = periodToDays(cooperation.getLateFeePeriod());
        // Ceiling: cada periodo iniciado se cobra completo (1 día de retraso = 1 periodo)
        long periods = (daysLate + periodDays - 1) / periodDays;

        BigDecimal result = cooperation.getLateFeeAmount().multiply(BigDecimal.valueOf(periods));
        log.info("LateFee calc: cooperationKey={}, dueDate={}, referenceDate={}, daysLate={}, period='{}', periodDays={}, periods={}, lateFeeAmount={}, total={}",
                cooperation.getKey(), cooperation.getDueDate(), referenceDate,
                daysLate, cooperation.getLateFeePeriod(), periodDays, periods,
                cooperation.getLateFeeAmount(), result);
        return result;
    }

    /**
     * Determina el paymentStatus semántico de un residente.
     *
     * @param isPaid        si ya está marcado como pagado
     * @param dueDate       fecha de vencimiento de la cooperación (puede ser null)
     * @param referenceDate fecha de evaluación (normalmente LocalDate.now())
     */
    public String resolvePaymentStatus(boolean isPaid, LocalDate dueDate, LocalDate referenceDate) {
        if (isPaid) return "PAGADO";
        if (dueDate != null && referenceDate.isAfter(dueDate)) return "VENCIDO";
        return "PENDIENTE";
    }

    /**
     * Calcula cuántos periodos de mora corresponden a una fecha de referencia.
     * Devuelve 0 si no aplica mora o la fecha no supera el vencimiento.
     */
    public long calculatePeriods(CooperationEntity cooperation, LocalDate referenceDate) {
        if (!Boolean.TRUE.equals(cooperation.getAllowLateFee())) return 0;
        if (cooperation.getLateFeeAmount() == null || cooperation.getLateFeeAmount().compareTo(BigDecimal.ZERO) <= 0) return 0;
        if (cooperation.getDueDate() == null) return 0;
        if (!referenceDate.isAfter(cooperation.getDueDate())) return 0;
        long daysLate = ChronoUnit.DAYS.between(cooperation.getDueDate(), referenceDate);
        long periodDays = periodToDays(cooperation.getLateFeePeriod());
        return (daysLate + periodDays - 1) / periodDays;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private long periodToDays(String period) {
        if (period == null) {
            log.warn("lateFeePeriod is null, defaulting to MONTHLY (30 days)");
            return 30;
        }
        return switch (period.trim().toUpperCase()) {
            case "DAILY", "DAYLY"   -> 1;
            case "WEEKLY"           -> 7;
            case "MONTHLY"          -> 30;
            case "YEARLY", "ANNUAL" -> 365;
            default -> {
                log.warn("Unrecognized lateFeePeriod value '{}', defaulting to MONTHLY (30 days). Valid: DAILY, WEEKLY, MONTHLY, YEARLY", period);
                yield 30;
            }
        };
    }
}
