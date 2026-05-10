package com.crdev.connect_rural_api.app.community.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPageResponse {
    private List<CommunityAdminResponse> data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
