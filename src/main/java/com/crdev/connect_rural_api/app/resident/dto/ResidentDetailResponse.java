package com.crdev.connect_rural_api.app.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResidentDetailResponse {
    private String key;
    private String communityKey;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String phoneNumber;
    private String email;
    private String address;
    private String addressReference;
    private LocalDate joinedAt;
    private Boolean active;
}
