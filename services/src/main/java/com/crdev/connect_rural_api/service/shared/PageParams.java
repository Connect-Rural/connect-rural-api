package com.crdev.connect_rural_api.service.shared;

/**
 * Parámetros de paginación del dominio.
 *
 * Vive en business/ para que las interfaces puerto no dependan de
 * {@code org.springframework.data.domain.Pageable}. La capa data traduce
 * estos valores a {@code PageRequest}.
 *
 * Reglas: page debe ser >= 0 y size >= 1 (valores inválidos lanzan
 * IllegalArgumentException → HTTP 400). Un size excesivo se limita a
 * {@link #MAX_SIZE} en lugar de rechazarse, para no romper clientes que
 * pidan listados grandes.
 */
public record PageParams(int page, int size) {

    public static final int MAX_SIZE = 100;
    public static final int DEFAULT_SIZE = 10;

    public PageParams {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be >= 1");
        }
        if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
    }

    /**
     * Construye desde parámetros HTTP opcionales (null → valores por defecto).
     */
    public static PageParams of(Integer page, Integer size) {
        return new PageParams(page == null ? 0 : page, size == null ? DEFAULT_SIZE : size);
    }
}
