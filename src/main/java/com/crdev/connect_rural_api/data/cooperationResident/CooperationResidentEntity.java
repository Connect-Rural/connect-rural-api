package com.crdev.connect_rural_api.data.cooperationResident;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cooperation_residents")
public class CooperationResidentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cooperation_resident_key", updatable = false, nullable = false)
    private UUID key;
    @Column(name = "cooperation_key", nullable = false, updatable = false)
    private UUID cooperationKey;
    @Column(name = "resident_key", nullable = false, updatable = false)
    private UUID residentKey;
    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;
    @Column(name = "amount_paid", precision = 12, scale = 2)
    private BigDecimal amountPaid;
    @Column(name = "paid_at")
    private LocalDate paidAt;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}

