package com.crdev.connect_rural_api.business.cooperation.usecases;

import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationSummaryResponseDto;
import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.mapper.CooperationAppMapper;
import com.crdev.connect_rural_api.business.financialObligation.FinancialObligationService;
import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@AllArgsConstructor
public class GetCooperationListUseCase {
    private final CooperationService service;
    private final FinancialObligationService financialObligationService;
    private final CooperationAppMapper mapper;

    @Transactional
    public List<CooperationSummaryResponseDto> execute(String communityKey) {
         List<CooperationEntity> cooperations = service.listByCommunity(communityKey);

        return cooperations.stream().map(cooperation -> {
                   var obligations = financialObligationService.listByCooperation(cooperation.getKey());
                   int totalAsigned = obligations.size();
                   int totalPaid = (int) obligations.stream()
                           .filter(o -> "PAID".equals(o.getStatus()))
                           .count();

                   int totalPending = totalAsigned - totalPaid;
                   double progress = totalAsigned == 0 ? 0 : ((double) totalPaid / totalAsigned) * 100;

                  return mapper.toResponseSummaryDto(cooperation, progress, totalAsigned, totalPending, totalPaid);

               }
        ).toList();
    }
}
