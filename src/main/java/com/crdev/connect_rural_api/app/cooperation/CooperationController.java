package com.crdev.connect_rural_api.app.cooperation;

import com.crdev.connect_rural_api.app.cooperation.dto.*;
import com.crdev.connect_rural_api.business.cooperation.CooperationService;
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

    private final CooperationService cooperationService;

    @GetMapping
    public ResponseEntity<List<CooperationSummaryResponse>> list(@PathVariable String communityKey) {
        return ResponseEntity.ok(cooperationService.getList(communityKey));
    }

    @GetMapping("/paginated")
    public ResponseEntity<CooperationPageResponse> getPaginated(
            @PathVariable String communityKey,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(cooperationService.getPaginated(communityKey,
                new CooperationFilterRequest(keyword, page, size)));
    }

    @GetMapping("/{cooperationKey}")
    public ResponseEntity<CooperationResponse> getByKey(@PathVariable String communityKey,
                                                         @PathVariable String cooperationKey) {
        return ResponseEntity.ok(cooperationService.getByKey(communityKey, cooperationKey));
    }

    @GetMapping("/{cooperationKey}/detail")
    public ResponseEntity<CooperationDetailResponse> getDetail(@PathVariable String communityKey,
                                                                @PathVariable String cooperationKey) {
        return ResponseEntity.ok(cooperationService.getDetail(communityKey, cooperationKey));
    }

    @PostMapping
    public ResponseEntity<CooperationSummaryResponse> create(@PathVariable String communityKey,
                                                              @Valid @RequestBody CreateCooperationRequest request) {
        log.info("Cooperation create requested: communityKey={}, name={}", communityKey, request.getName());
        return ResponseEntity.status(201).body(cooperationService.create(communityKey, request));
    }

    @PatchMapping("/{cooperationKey}")
    public ResponseEntity<CooperationSummaryResponse> update(@PathVariable String communityKey,
                                                              @PathVariable String cooperationKey,
                                                              @Valid @RequestBody CreateCooperationRequest request) {
        log.info("Cooperation update requested: communityKey={}, cooperationKey={}", communityKey, cooperationKey);
        return ResponseEntity.ok(cooperationService.update(communityKey, cooperationKey, request));
    }

    @DeleteMapping("/{cooperationKey}")
    public ResponseEntity<Void> delete(@PathVariable String communityKey,
                                        @PathVariable String cooperationKey) {
        log.info("Cooperation delete requested: communityKey={}, cooperationKey={}", communityKey, cooperationKey);
        cooperationService.delete(communityKey, cooperationKey);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{cooperationKey}/residents/{residentKey}/pay")
    public ResponseEntity<ResidentAssigned> markAsPaid(
            @PathVariable String communityKey,
            @PathVariable String cooperationKey,
            @PathVariable String residentKey,
            @RequestBody(required = false) MarkAsPaidRequest request) {
        MarkAsPaidRequest body = request != null ? request : new MarkAsPaidRequest();
        return ResponseEntity.ok(cooperationService.markAsPaid(
                communityKey, cooperationKey, residentKey, body.getAmountPaid(), body.getPaidAt()));
    }

    @PatchMapping("/{cooperationKey}/residents/{residentKey}/unpay")
    public ResponseEntity<ResidentAssigned> markAsUnpaid(
            @PathVariable String communityKey,
            @PathVariable String cooperationKey,
            @PathVariable String residentKey) {
        return ResponseEntity.ok(cooperationService.markAsUnpaid(communityKey, cooperationKey, residentKey));
    }

    @PatchMapping("/{cooperationKey}/residents/pay-all")
    public ResponseEntity<Map<String, Integer>> markAllAsPaid(
            @PathVariable String communityKey,
            @PathVariable String cooperationKey) {
        int updated = cooperationService.markAllAsPaid(communityKey, cooperationKey);
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    @PatchMapping("/{cooperationKey}/close")
    public ResponseEntity<CooperationSummaryResponse> close(
            @PathVariable String communityKey,
            @PathVariable String cooperationKey) {
        log.info("Cooperation close requested: communityKey={}, cooperationKey={}", communityKey, cooperationKey);
        return ResponseEntity.ok(cooperationService.close(communityKey, cooperationKey));
    }

    @PatchMapping("/{cooperationKey}/reopen")
    public ResponseEntity<CooperationSummaryResponse> reopen(
            @PathVariable String communityKey,
            @PathVariable String cooperationKey) {
        log.info("Cooperation reopen requested: communityKey={}, cooperationKey={}", communityKey, cooperationKey);
        return ResponseEntity.ok(cooperationService.reopen(communityKey, cooperationKey));
    }
}
