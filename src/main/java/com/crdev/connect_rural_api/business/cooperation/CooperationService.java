package com.crdev.connect_rural_api.business.cooperation;

import com.crdev.connect_rural_api.data.cooperation.CooperationEntity;
import com.crdev.connect_rural_api.data.cooperation.CooperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CooperationService {
    private final CooperationRepository cooperationRepository;

    public List<CooperationEntity> listByCommunity(String communityKey) {
        return cooperationRepository.findAllByCommunityKey(UUID.fromString(communityKey));
    }
}
