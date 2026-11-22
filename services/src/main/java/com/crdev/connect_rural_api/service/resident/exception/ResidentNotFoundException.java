package com.crdev.connect_rural_api.service.resident.exception;


import com.crdev.connect_rural_api.service.shared.DomainException;

import java.util.UUID;

public class ResidentNotFoundException extends DomainException {

    private final UUID residentKey;

    public ResidentNotFoundException(UUID residentKey) {
        super("Resident not found: " + residentKey);
        this.residentKey = residentKey;
    }

    public UUID getResidentKey() {
        return residentKey;
    }
}