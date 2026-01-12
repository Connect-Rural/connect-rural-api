package com.crdev.connect_rural_api.business.cooperation.specs;

import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class CooperationSpecs {

    /**
     * Filtrar por communityKey (obligatorio)
     */
    public static Specification<CooperationEntity> withCommunity(UUID communityKey) {
        return (root, query, cb) ->
                cb.equal(root.get("communityKey"), communityKey);
    }

    /**
     * Filtro por nombre descripcion usando keyword (opcional)
     */
    public static Specification<CooperationEntity> withKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String like = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like)
            );
        };
    }
}