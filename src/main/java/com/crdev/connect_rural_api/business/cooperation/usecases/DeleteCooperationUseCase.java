package com.crdev.connect_rural_api.business.cooperation.usecases;

import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
@Slf4j
public class DeleteCooperationUseCase {
    private final CooperationService service;

    @Transactional
    public String execute(String communityKey, String residentKey) {
        log.info("Deleting cooperation: communityKey={}, cooperationKey={}", communityKey, residentKey);
        CooperationEntity existing = service.getByKey(communityKey, residentKey);
         service.delete(residentKey);
        return existing.getKey().toString();
    }

}
