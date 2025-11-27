package com.crdev.connect_rural_api.data.community;


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
@Table(name = "communities")
public class CommunityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "community_key" , nullable = false)
    private UUID key;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "logo_url")
    private String logoUrl;
    @Column(name = "address")
    private String address;
    @Column(name = "state")
    private String state;
    @Column(name = "municipality")
    private String municipality;
    @Column(name = "postal_code")
    private String postalCode;
    @Column(name = "subscription_plan")
    private String subscriptionPlan;
    @Column(name = "completed_configuration")
    private Boolean completedConfiguration;
    @Column(name = "active")
    private Boolean active;
    @Column(name = "created_at", updatable = false)
    private Date createAt;
    @Column(name = "updated_at")
    private Date updatedAt;
}
