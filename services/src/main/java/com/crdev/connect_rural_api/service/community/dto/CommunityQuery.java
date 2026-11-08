package com.crdev.connect_rural_api.service.community.dto;

/**
 * Criterios de búsqueda de comunidades (query object del dominio).
 *
 * Sustituye a {@code CommunityFilterRequest} (DTO HTTP) y a la
 * {@code Specification} de Spring Data dentro de business/: la capa data es
 * quien traduce este query a una Specification JPA.
 */
public record CommunityQuery(String keyword) {

    public CommunityQuery {
        keyword = (keyword == null) ? "" : keyword.trim();
    }
}
