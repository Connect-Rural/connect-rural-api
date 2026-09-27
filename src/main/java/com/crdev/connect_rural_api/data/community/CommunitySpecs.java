package com.crdev.connect_rural_api.data.community;

import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications JPA de comunidades.
 *
 * Vive en data/ porque es infraestructura: el dominio expone
 * {@code CommunityQuery} y esta clase lo traduce a criterios de JPA.
 */
public final class CommunitySpecs {

    private CommunitySpecs() {
    }

    public static Specification<CommunityEntity> withKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like)
            );
        };
    }
}
