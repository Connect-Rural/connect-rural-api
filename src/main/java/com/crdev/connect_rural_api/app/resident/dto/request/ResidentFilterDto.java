package com.crdev.connect_rural_api.app.resident.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResidentFilterDto {
    private String keyword;
    private Integer page;
    private Integer size;

}
