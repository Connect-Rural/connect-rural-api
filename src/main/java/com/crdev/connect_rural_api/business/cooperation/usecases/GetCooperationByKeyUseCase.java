package com.crdev.connect_rural_api.business.cooperation.usecases;

import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationResponseDto;
import com.crdev.connect_rural_api.business.cooperation.CooperationService;
import com.crdev.connect_rural_api.business.cooperation.mapper.CooperationAppMapper;
import com.crdev.connect_rural_api.business.financialObligation.FinancialObligationService;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@AllArgsConstructor
public class GetCooperationByKeyUseCase {
	private final CooperationService service;
	private final FinancialObligationService financialObligationService;
	private final ResidentService residentService;
	private final CooperationAppMapper mapper;

	@Transactional
	public CooperationResponseDto execute(String communityKey, String cooperationKey) {
		var cooperationEntity = service.getByKey(communityKey, cooperationKey);

		List<String> assignedResidentsKeys = new ArrayList<>();
		List<String> excludedResidentsKeys = new ArrayList<>();

		var obligations = financialObligationService.listByCooperation(UUID.fromString(cooperationKey));

		if (cooperationEntity.getAssignmentType().equals("ALL_EXCEPT")) {
			List<ResidentEntity> residentsInCommunity = residentService.listByCommunity(communityKey);
			excludedResidentsKeys = residentsInCommunity.stream()
					.filter(resident -> obligations.stream()
							.noneMatch(o -> o.getResidentKey().equals(resident.getKey())))
					.map(resident -> resident.getKey().toString()).toList();
		} else if (cooperationEntity.getAssignmentType().equals("INDIVIDUAL")) {
			assignedResidentsKeys = obligations.stream()
					.map(o -> o.getResidentKey().toString()).toList();
		}

		return mapper.toResponseDto(cooperationEntity, assignedResidentsKeys, excludedResidentsKeys);
	}
}
