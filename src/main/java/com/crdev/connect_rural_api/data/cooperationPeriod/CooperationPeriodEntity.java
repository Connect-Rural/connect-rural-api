package com.crdev.connect_rural_api.data.cooperationPeriod;

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
@Table(name = "cooperation_periods", schema = "connect_rural")
public class CooperationPeriodEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cooperation_period_key" , nullable = false)
    private UUID key;
    @Column(name = "cooperation_key", nullable = false)
    private UUID cooperationKey;
    @Column(name = "period_number")
    private Integer periodNumber;
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;
    @Column(name = "status", length = 20)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


}
