package com.crdev.connect_rural_api.service.resident.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class SimpleResidentCommand {
    private UUID key;
    private  UUID communityKey;
    private  String fullName;
}