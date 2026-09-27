package com.crdev.connect_rural_api.business.community;

import com.crdev.connect_rural_api.business.shared.PageParams;
import com.crdev.connect_rural_api.business.shared.PagedResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de comunidades.
 *
 * Sin tipos de Spring Data (Page, Pageable, Specification) y sin entidades
 * JPA: todo se expone en términos del dominio (Community, CommunityQuery,
 * PagedResult). La implementación vive en data/community/.
 */
public interface CommunityRepository {

    Community save(Community community);

    Optional<Community> findById(UUID key);

    List<Community> findAll();

    PagedResult<Community> findAll(CommunityQuery query, PageParams page);

    void deleteById(UUID key);

    Optional<Community> findByWhatsappAppKeyAndActiveTrue(UUID whatsappAppKey);
}
