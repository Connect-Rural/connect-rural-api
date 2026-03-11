package com.crdev.connect_rural_api.app.cooperation;

import com.crdev.connect_rural_api.app.cooperation.dto.request.CooperationFilterDto;
import com.crdev.connect_rural_api.app.cooperation.dto.request.CreateCooperationRequestDto;
import com.crdev.connect_rural_api.app.cooperation.dto.request.MarkAsPaidRequestDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationDetailResponseDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationResponseDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationSummaryPaginatedResponseDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.CooperationSummaryResponseDto;
import com.crdev.connect_rural_api.app.cooperation.dto.response.ResidentAssigned;
import com.crdev.connect_rural_api.business.cooperation.usecases.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/{communityKey}/cooperations")
@RequiredArgsConstructor
@Slf4j
public class CooperationController {
    private final GetCooperationListUseCase getCooperationListUC;
    private final GetCooperationPaginatedUseCase getCooperationPaginatedUC;
    private final GetCooperationByKeyUseCase getCooperationByKeyUC;
    private final GetCooperationDetailByKeyUseCase getCooperationDetailByKeyUC;
    private final CreateCooperationUseCase createCooperationUC;
    private final UpdateCooperationUseCase updateCooperationUC;
    private final DeleteCooperationUseCase deleteCooperationUC;
    private final MarkAsPaidUseCase markAsPaidUC;
    private final MarkAsUnpaidUseCase markAsUnpaidUC;
    private final MarkAllAsPaidUseCase markAllAsPaidUC;
    private final CloseCooperationUseCase closeCooperationUC;
    private final ReopenCooperationUseCase reopenCooperationUC;

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

    @GetMapping("/{cooperationKey}/detail")
    public ResponseEntity<CooperationDetailResponseDto> getDetailByKey(
            @PathVariable String communityKey,
            @PathVariable String cooperationKey) {
        return ResponseEntity.ok(getCooperationDetailByKeyUC.execute(communityKey, cooperationKey));
    }

    @PostMapping
    public ResponseEntity<CooperationSummaryResponseDto> create(@PathVariable String communityKey,
                                                                @Valid @RequestBody CreateCooperationRequestDto request) {
        log.info("Cooperation create requested: communityKey={}, name={}", communityKey, request.getName());
        return ResponseEntity.status(201).body(
                createCooperationUC.execute(communityKey,request)
        );

    }

    @PatchMapping("/{cooperationKey}")
    public ResponseEntity<CooperationSummaryResponseDto> update(@PathVariable String communityKey,
                                                      @PathVariable String cooperationKey,
                                                      @Valid @RequestBody CreateCooperationRequestDto updateRequest) {
        log.info("Cooperation update requested: communityKey={}, cooperationKey={}", communityKey, cooperationKey);
        return ResponseEntity.ok(updateCooperationUC.execute(communityKey,cooperationKey, updateRequest));
    }


    @DeleteMapping("/{cooperationKey}")
    public ResponseEntity<?> delete(@PathVariable String communityKey,
                                    @PathVariable String cooperationKey) {
        log.info("Cooperation delete requested: communityKey={}, cooperationKey={}", communityKey, cooperationKey);
        deleteCooperationUC.execute(communityKey, cooperationKey);
        return ResponseEntity.noContent().build();
    }

    // ── Pagos ─────────────────────────────────────────────────────────────────

    @PatchMapping("/{cooperationKey}/residents/{residentKey}/pay")
    public ResponseEntity<ResidentAssigned> markAsPaid(
            @PathVariable String communityKey,
            @PathVariable String cooperationKey,
            @PathVariable String residentKey,
            @RequestBody(required = false) MarkAsPaidRequestDto request) {
        MarkAsPaidRequestDto body = request != null ? request : new MarkAsPaidRequestDto();
        return ResponseEntity.ok(
                markAsPaidUC.execute(communityKey, cooperationKey, residentKey, body.getAmountPaid(), body.getPaidAt())
        );
    }

    @PatchMapping("/{cooperationKey}/residents/{residentKey}/unpay")
    public ResponseEntity<ResidentAssigned> markAsUnpaid(
            @PathVariable String communityKey,
            @PathVariable String cooperationKey,
            @PathVariable String residentKey) {
        return ResponseEntity.ok(markAsUnpaidUC.execute(communityKey, cooperationKey, residentKey));
    }

    @PatchMapping("/{cooperationKey}/residents/pay-all")
    public ResponseEntity<?> markAllAsPaid(
            @PathVariable String communityKey,
            @PathVariable String cooperationKey) {
        int updated = markAllAsPaidUC.execute(communityKey, cooperationKey);
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    // ── Estado ────────────────────────────────────────────────────────────────

    @PatchMapping("/{cooperationKey}/close")
    public ResponseEntity<CooperationSummaryResponseDto> close(
            @PathVariable String communityKey,
            @PathVariable String cooperationKey) {
        log.info("Cooperation close requested: communityKey={}, cooperationKey={}", communityKey, cooperationKey);
        return ResponseEntity.ok(closeCooperationUC.execute(communityKey, cooperationKey));
    }

    @PatchMapping("/{cooperationKey}/reopen")
    public ResponseEntity<CooperationSummaryResponseDto> reopen(
            @PathVariable String communityKey,
            @PathVariable String cooperationKey) {
        log.info("Cooperation reopen requested: communityKey={}, cooperationKey={}", communityKey, cooperationKey);
        return ResponseEntity.ok(reopenCooperationUC.execute(communityKey, cooperationKey));
    }
}
