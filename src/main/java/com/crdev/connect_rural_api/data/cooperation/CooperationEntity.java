package com.crdev.connect_rural_api.data.cooperation;

import jakarta.persistence.*;
import lombok.*;
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
@Table(name = "cooperations", schema = "connect_rural")
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
    private String status = "ACTIVE";
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

}
