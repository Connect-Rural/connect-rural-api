package com.crdev.connect_rural_api.business.cooperation.usecases;



import com.crdev.connect_rural_api.app.cooperation.dto.request.CooperationFilterDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationSummaryPaginatedResponseDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationSummaryResponseDto;
import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.mapper.CooperationAppMapper;
import com.crdev.connect_rural_api.business.cooperationResident.CooperationResidentService;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import com.crdev.connect_rural_api.data.cooperationResident.CooperationResidentEntity;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@AllArgsConstructor
public class GetCooperationPaginatedUseCase {
    private final CooperationService service;
    private final CooperationResidentService cooperationResidentService;
    private final CooperationAppMapper mapper;

    @Transactional
    public CooperationSummaryPaginatedResponseDto execute(String communityKey, CooperationFilterDto filter) {

        Page<CooperationEntity> result = service.getAllPaginatedAndFiltered(
                communityKey,
                filter.getKeyword(),
                filter.getPage(),
                filter.getSize());

        List<CooperationSummaryResponseDto> assignedSummary = result.stream().map(cooperation -> {
                    var assignedResidents = cooperationResidentService.listByCooperation(cooperation.getKey().toString());
                    int totalAsigned = assignedResidents.size();
                    int totalPaid = (int) assignedResidents.stream()
                            .filter(CooperationResidentEntity::getIsPaid)
                            .count();

                    int totalPending = totalAsigned - totalPaid;
                    double progress = totalAsigned == 0 ? 0 : ((double) totalPaid / totalAsigned) * 100;

                    return mapper.toResponseSummaryDto(cooperation, progress,totalAsigned,totalPending,totalPaid);

                }
        ).toList();

        return new CooperationSummaryPaginatedResponseDto(
                assignedSummary,
                filter.getPage(),
                filter.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}
