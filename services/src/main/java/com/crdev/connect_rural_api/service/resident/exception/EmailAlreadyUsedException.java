package com.crdev.connect_rural_api.service.resident.exception;

import com.crdev.connect_rural_api.service.shared.DomainException;

public class EmailAlreadyUsedException extends DomainException {

    private final String email;

    public EmailAlreadyUsedException(String email) {
        super("Email already registered: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}