package com.crdev.connect_rural_api.business.cooperation.usecases;



import com.crdev.connect_rural_api.app.cooperation.dto.request.CooperationFilterDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationSummaryPaginatedResponseDto;
import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.mapper.CooperationAppMapper;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GetCooperationPaginatedUseCase {
    private final CooperationService service;
    private final CooperationAppMapper mapper;


    public CooperationSummaryPaginatedResponseDto execute(String communityKey, CooperationFilterDto filter) {

        Page<CooperationEntity> result = service.getAllPaginatedAndFiltered(
                communityKey,
                filter.getKeyword(),
                filter.getPage(),
                filter.getSize());

        return new CooperationSummaryPaginatedResponseDto(
                mapper.toResponseSummaryList(result.getContent()),
                filter.getPage(),
                filter.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}
