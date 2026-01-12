package com.crdev.connect_rural_api.app.cooperation.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CooperationSummaryPaginatedResponseDto {
    private List<CooperationSummaryResponseDto> data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
