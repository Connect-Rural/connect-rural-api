package com.crdev.connect_rural_api.app.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleResidentResponse {
    private String key;
    private String communityKey;
    private String name;
}
