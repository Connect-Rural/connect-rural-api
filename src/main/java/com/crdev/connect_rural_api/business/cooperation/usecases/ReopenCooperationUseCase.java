package com.crdev.connect_rural_api.business.cooperation.usecases;

import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationSummaryResponseDto;
import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.mapper.CooperationAppMapper;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
@Slf4j
public class ReopenCooperationUseCase {

    private final CooperationService cooperationService;
    private final CooperationAppMapper mapper;

    @Transactional
    public CooperationSummaryResponseDto execute(String communityKey, String cooperationKey) {
        log.info("Reopening cooperation: communityKey={}, cooperationKey={}", communityKey, cooperationKey);
        CooperationEntity entity = cooperationService.getByKey(communityKey, cooperationKey);
        CooperationEntity reopened = cooperationService.reopen(entity);
        return mapper.toResponseSummaryDto(reopened);
    }
}
