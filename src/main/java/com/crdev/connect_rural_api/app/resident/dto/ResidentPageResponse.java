package com.crdev.connect_rural_api.app.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResidentPageResponse {
    private List<ResidentResponse> data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
