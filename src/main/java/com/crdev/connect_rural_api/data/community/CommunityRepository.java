package com.crdev.connect_rural_api.data.community;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CommunityRepository extends JpaRepository<CommunityEntity, UUID>,
        JpaSpecificationExecutor<CommunityEntity> {
}
