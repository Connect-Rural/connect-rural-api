package com.crdev.connect_rural_api.service.shared;

/**
 * Base para todas las excepciones de negocio.
 * No conoce Spring ni HTTP: el mapeo a códigos de estado vive en la capa web.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}