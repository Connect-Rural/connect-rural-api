package com.crdev.connect_rural_api.app.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResidentFilterRequest {
    private String keyword;
    private Integer page;
    private Integer size;
}
