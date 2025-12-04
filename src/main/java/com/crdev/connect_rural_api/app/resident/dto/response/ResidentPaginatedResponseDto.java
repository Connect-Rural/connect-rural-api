package com.crdev.connect_rural_api.app.resident.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResidentPaginatedResponseDto {
    private List<ResidentResponseDto> data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
