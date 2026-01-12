package com.crdev.connect_rural_api.app.cooperation;

import com.crdev.connect_rural_api.app.cooperation.dto.request.CooperationFilterDto;
import com.crdev.connect_rural_api.app.cooperation.dto.request.CreateCooperationRequestDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationResponseDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationSummaryPaginatedResponseDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationSummaryResponseDto;
import com.crdev.connect_rural_api.business.cooperation.usecases.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/{communityKey}/cooperations")
@RequiredArgsConstructor
public class CooperationController {
    private final GetCooperationListUseCase getCooperationListUC;
    private final GetCooperationPaginatedUseCase getCooperationPaginatedUC;
    private final GetCooperationByKeyUseCase getCooperationByKeyUC;
    private final CreateCooperationUseCase createCooperationUC;
    private final UpdateCooperationUseCase updateCooperationUC;
    private final DeleteCooperationUseCase deleteCooperationUC;

    @GetMapping
    public ResponseEntity<List<CooperationSummaryResponseDto>> list(@PathVariable String communityKey) {
        return ResponseEntity.ok(getCooperationListUC.execute(communityKey));
    }

    @GetMapping("/paginated")
    public ResponseEntity<CooperationSummaryPaginatedResponseDto> getPaginated(
            @PathVariable String communityKey,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size){
        CooperationFilterDto filter =
                new CooperationFilterDto(keyword, page, size);
        return ResponseEntity.ok(
                getCooperationPaginatedUC.execute(communityKey,filter)
        );
    }

    @GetMapping("/{cooperationKey}")
    public ResponseEntity<CooperationResponseDto>getByKey(@PathVariable String communityKey, @PathVariable String cooperationKey) {
        return ResponseEntity.ok(getCooperationByKeyUC.execute(communityKey, cooperationKey));
    }

    @PostMapping
    public ResponseEntity<CooperationSummaryResponseDto> create(@PathVariable String communityKey,
                                                                @Valid @RequestBody CreateCooperationRequestDto request) {
        return ResponseEntity.status(201).body(
                createCooperationUC.execute(communityKey,request)
        );

    }

    @PatchMapping("/{cooperationKey}")
    public ResponseEntity<CooperationSummaryResponseDto> update(@PathVariable String communityKey,
                                                      @PathVariable String cooperationKey,
                                                      @Valid @RequestBody CreateCooperationRequestDto updateRequest) {
        return ResponseEntity.ok(updateCooperationUC.execute(communityKey,cooperationKey, updateRequest));
    }


    @DeleteMapping("/{cooperationKey}")
    public ResponseEntity<?> delete(@PathVariable String communityKey,
                                    @PathVariable String cooperationKey) {
        deleteCooperationUC.execute(communityKey, cooperationKey);
        return ResponseEntity.noContent().build();
    }
}
