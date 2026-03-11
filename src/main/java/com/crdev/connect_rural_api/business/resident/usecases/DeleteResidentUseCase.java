package com.crdev.connect_rural_api.business.resident.usecases;

import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
@Slf4j
public class DeleteResidentUseCase {
    private final ResidentService service;

    @Transactional
    public String execute(String communityKey, String residentKey) {
        log.info("Deleting resident: communityKey={}, residentKey={}", communityKey, residentKey);
        ResidentEntity existing = service.getByKey(communityKey, residentKey);
         service.delete(residentKey);
        return existing.getKey().toString();
    }

}
