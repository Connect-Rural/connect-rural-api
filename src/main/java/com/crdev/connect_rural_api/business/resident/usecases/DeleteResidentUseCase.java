package com.crdev.connect_rural_api.business.resident.usecases;

import com.crdev.connect_rural_api.business.community.CommunityService;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class DeleteResidentUseCase {
    private final ResidentService service;

    @Transactional
    public String execute(String communityKey, String residentKey) {
        ResidentEntity existing = service.getByKey(communityKey, residentKey);
         service.delete(residentKey);
        return existing.getKey().toString();
    }

}
