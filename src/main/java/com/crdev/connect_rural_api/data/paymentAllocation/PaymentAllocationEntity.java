package com.crdev.connect_rural_api.data.paymentAllocation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_allocations", schema = "connect_rural")
public class PaymentAllocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "allocation_key", nullable = false, updatable = false)
    private UUID key;

    @Column(name = "payment_key", nullable = false, updatable = false)
    private UUID paymentKey;

    @Column(name = "obligation_key", nullable = false, updatable = false)
    private UUID obligationKey;

    @Column(name = "amount_applied", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountApplied;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
