package com.crdev.connect_rural_api.app.cooperation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CooperationFilterRequest {
    private String keyword;
    private Integer page;
    private Integer size;
}
