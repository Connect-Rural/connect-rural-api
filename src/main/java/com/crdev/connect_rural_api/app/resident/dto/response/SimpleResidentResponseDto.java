package com.crdev.connect_rural_api.app.resident.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleResidentResponseDto {
    private  String key;
    private  String communityKey;
    private  String name;
}
