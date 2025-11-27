package com.crdev.connect_rural_api.app.community.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommunityFilterRequestDto {
    private String keyword;
    private Integer page;
    private Integer size;

}
