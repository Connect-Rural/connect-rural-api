package com.crdev.connect_rural_api.business.shared;

import java.util.List;

/**
 * Resultado paginado del dominio.
 *
 * Sustituye a {@code org.springframework.data.dto.Page} en las interfaces
 * puerto para que business/ no conozca Spring Data. La capa data traduce el
 * Page de JPA a este tipo; totalPages se recibe tal cual para mantener
 * exactamente el mismo contrato HTTP que hasta ahora.
 */
public record PagedResult<T>(List<T> items, int page, int size, long totalElements, int totalPages) {

    public PagedResult {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
