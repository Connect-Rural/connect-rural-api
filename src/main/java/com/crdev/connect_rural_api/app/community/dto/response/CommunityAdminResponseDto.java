package com.crdev.connect_rural_api.app.community.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityAdminResponseDto {
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
    private UUID whatsappAppKey;
}
