package com.crdev.connect_rural_api.app.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityResponseDto {
    private String key;
    private String name;
    private String description;
    private String logoUrl;
    private String address;
    private String state;
    private String municipality;
    private String postalCode;
    private String subscriptionPlan;
    private Boolean completedConfiguration;
    private Boolean active;
    private Date createdAt;
    private Date updatedAt;

}
