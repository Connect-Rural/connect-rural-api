package com.crdev.connect_rural_api.service.resident.dto;

/**
 * Criterios de búsqueda de habitantes (query object del dominio).
 *
 * Sustituye a {@code ResidentFilterRequest} (DTO HTTP) y a la
 * {@code Specification} de Spring Data dentro de business/: la capa data es
 * quien traduce este query a una Specification JPA.
 */
public record ResidentQuery(String keyword) {

    public ResidentQuery {
        keyword = (keyword == null) ? "" : keyword.trim();
    }
}
