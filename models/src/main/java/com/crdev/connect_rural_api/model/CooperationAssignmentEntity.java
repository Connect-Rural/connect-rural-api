package com.crdev.connect_rural_api.model;

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
@Table(name = "cooperation_assignments", schema = "connect_rural")
public class CooperationAssignmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cooperation_assignment_key" , nullable = false)
    private UUID key;
    @Column(name = "cooperation_key", nullable = false)
    private UUID cooperationKey;
    @Column(name = "resident_key", nullable = false)
    private UUID residentKey;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


}
