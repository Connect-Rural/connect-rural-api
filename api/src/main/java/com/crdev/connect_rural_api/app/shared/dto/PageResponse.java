package com.crdev.connect_rural_api.app.shared.dto;

import com.crdev.connect_rural_api.service.shared.PagedResult;
import java.util.List;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> data,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <S, T> PageResponse<T> from(PagedResult<S> page, Function<S, T> mapper) {
        return new PageResponse<>(
                page.items().stream().map(mapper).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }
}   