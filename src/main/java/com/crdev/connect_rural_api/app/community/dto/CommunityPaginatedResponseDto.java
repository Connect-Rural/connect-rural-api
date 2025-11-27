package com.crdev.connect_rural_api.app.community.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPaginatedResponseDto {
    private List<CommunityResponseDto> data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
