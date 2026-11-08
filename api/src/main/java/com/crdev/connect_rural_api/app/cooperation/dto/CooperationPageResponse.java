package com.crdev.connect_rural_api.app.cooperation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CooperationPageResponse {
    private List<CooperationSummaryResponse> data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
