package com.crdev.connect_rural_api.data.resident;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "residents")
public class ResidentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "resident_key" , nullable = false)
    private UUID key;
    @Column(name = "community_key", nullable = false)
    private UUID communityKey;
    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "birth_date", nullable = false)
    private Date birthDate;
    @Column(name = "phone_number")
    private String phoneNumber;
    @Column(name = "email")
    private String email;
    @Column(name = "address")
    private String address;
    @Column(name = "address_reference")
    private String addressReference;
    @Column(name = "joined_at", nullable = false)
    private Date joinedAt;
    @Column(name = "active")
    private Boolean active;
    @Column(name = "created_at" , updatable = false)
    private Date createdAt;
    @Column(name = "updated_at")
    private Date updatedAt;

}
