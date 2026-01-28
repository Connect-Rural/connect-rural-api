package com.crdev.connect_rural_api.app.cooperation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResidentAssigned {
    private String key;
    private String firstName;
    private String lastName;
    private Boolean isPaid;
}
