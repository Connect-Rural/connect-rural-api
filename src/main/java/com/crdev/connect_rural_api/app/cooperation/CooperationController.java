package com.crdev.connect_rural_api.app.cooperation;

import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationSummaryResponseDto;
import com.crdev.connect_rural_api.business.cooperation.usecases.GetCooperationListUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/{communityKey}/cooperations")
@RequiredArgsConstructor
public class CooperationController {
    private final GetCooperationListUseCase getCooperationListUC;

    @GetMapping
    public ResponseEntity<List<CooperationSummaryResponseDto>> list(@PathVariable String communityKey) {

        return ResponseEntity.ok(getCooperationListUC.execute(communityKey));
    }

}
