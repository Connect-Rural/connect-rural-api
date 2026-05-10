package com.crdev.connect_rural_api.data.financialObligation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "financial_obligations", schema = "connect_rural")
public class FinancialObligationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "obligation_key", nullable = false, updatable = false)
    private UUID key;

    @Column(name = "resident_key", nullable = false, updatable = false)
    private UUID residentKey;

    @Column(name = "community_key", nullable = false, updatable = false)
    private UUID communityKey;

    @Column(name = "origin_type", nullable = false, length = 50)
    private String originType;

    @Column(name = "origin_id", nullable = false)
    private UUID originId;

    @Column(name = "amount_due", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountDue;

    @Column(name = "late_fee_applied", nullable = false, precision = 12, scale = 2)
    private BigDecimal lateFeeApplied = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING";

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "period_ref")
    private LocalDate periodRef;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
