package com.crdev.connect_rural_api.business.community.usecases;

import com.crdev.connect_rural_api.business.community.CommunityService;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class DeleteCommunityUseCase {
    private final CommunityService service;

    @Transactional
    public String execute(String key) {
        CommunityEntity existing = service.getBykey(key);
         service.delete(key);
        return existing.getKey().toString();
    }

}
