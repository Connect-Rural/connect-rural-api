package com.crdev.connect_rural_api.data.cooperation;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cooperations")
public class CooperationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cooperation_key", nullable = false, updatable = false)
    private UUID key;
    @Column(name = "community_key", nullable = false)
    private UUID communityKey;
    @Column(nullable = false, length = 255)
    private String name;
    @Column(columnDefinition = "text")
    private String description;
    @Column(name = "base_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseAmount;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "due_date")
    private LocalDate dueDate;
    @Column(name = "allow_late_fee")
    private Boolean allowLateFee = false;
    @Column(name = "late_fee_amount", precision = 12, scale = 2)
    private BigDecimal lateFeeAmount;
    @Column(name = "late_fee_period", length = 20)
    private String lateFeePeriod;
    @Column(name = "assignment_type", length = 20, nullable = false)
    private String assignmentType;
    @Column(length = 20)
    private String status = "OPEN";
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
