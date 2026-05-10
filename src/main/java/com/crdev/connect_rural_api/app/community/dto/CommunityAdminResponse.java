package com.crdev.connect_rural_api.app.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityAdminResponse {
    private String key;
    private String name;
    private String description;
    private String logoUrl;
    private String location;
    private String subscriptionPlan;
    private Boolean completedConfiguration;
    private String adminEmail;
    private String adminPhone;
    private Number membersCount;
    private Number usersCount;
    private Boolean active;
}
