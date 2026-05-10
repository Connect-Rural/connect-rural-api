package com.crdev.connect_rural_api.data.cooperation;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class CooperationSpecs {

    public static Specification<CooperationEntity> withCommunity(UUID communityKey) {
        return (root, query, cb) ->
                cb.equal(root.get("communityKey"), communityKey);
    }

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
