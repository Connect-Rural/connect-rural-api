package com.crdev.connect_rural_api.business.resident.specs;

import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ResidentSpecs {

    /**
     * Filtrar por communityKey (obligatorio)
     */
    public static Specification<ResidentEntity> withCommunity(UUID communityKey) {
        return (root, query, cb) ->
                cb.equal(root.get("communityKey"), communityKey);
    }

    /**
     * Filtro por nombre/apellido usando keyword (opcional)
     */
    public static Specification<ResidentEntity> withKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String like = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), like),
                    cb.like(cb.lower(root.get("lastName")), like)
            );
        };
    }
}