package com.crdev.connect_rural_api.data.resident;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class SimpleResident {
    private  UUID key;
    private  UUID communityKey;
    private  String firstName;
    private  String lastName;
}