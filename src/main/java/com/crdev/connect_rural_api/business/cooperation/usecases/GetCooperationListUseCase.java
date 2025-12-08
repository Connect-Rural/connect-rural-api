package com.crdev.connect_rural_api.business.cooperation.usecases;

import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationSummaryResponseDto;
import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.mapper.CooperationAppMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class GetCooperationListUseCase {
    private final CooperationService service;
    private final CooperationAppMapper mapper;

    public List<CooperationSummaryResponseDto> execute(String communityKey) {
        return mapper.toResponseSummaryList(service.listByCommunity(communityKey));
    }
}
