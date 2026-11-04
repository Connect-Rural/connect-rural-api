package com.crdev.connect_rural_api.app.cooperation.dto.request;

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
