package com.crdev.connect_rural_api.app.resident;

import com.crdev.connect_rural_api.app.resident.dto.CreateResidentRequest;
import com.crdev.connect_rural_api.app.resident.dto.ResidentDetailResponse;
import com.crdev.connect_rural_api.app.resident.dto.ResidentFilterRequest;
import com.crdev.connect_rural_api.app.resident.dto.ResidentPageResponse;
import com.crdev.connect_rural_api.app.resident.dto.ResidentResponse;
import com.crdev.connect_rural_api.app.resident.dto.SimpleResidentResponse;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/{communityKey}/residents")
@RequiredArgsConstructor
@Slf4j
public class ResidentController {

    private final ResidentService residentService;

    @GetMapping
    public ResponseEntity<List<ResidentResponse>> list(@PathVariable String communityKey) {
        return ResponseEntity.ok(residentService.listByCommunity(communityKey));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ResidentPageResponse> getPaginated(
            @PathVariable String communityKey,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(residentService.getPaginated(communityKey,
                new ResidentFilterRequest(keyword, page, size)));
    }

    @GetMapping("/catalog")
    public ResponseEntity<List<SimpleResidentResponse>> getCatalog(@PathVariable String communityKey) {
        return ResponseEntity.ok(residentService.getCatalog(communityKey));
    }

    @GetMapping("/{residentKey}")
    public ResponseEntity<ResidentDetailResponse> getByKey(@PathVariable String communityKey,
                                                            @PathVariable String residentKey) {
        return ResponseEntity.ok(residentService.getByKey(communityKey, residentKey));
    }

    @PostMapping
    public ResponseEntity<ResidentResponse> create(@PathVariable String communityKey,
                                                    @Valid @RequestBody CreateResidentRequest request) {
        log.info("Resident create requested: communityKey={}", communityKey);
        return ResponseEntity.status(201).body(residentService.create(communityKey, request));
    }

    @PatchMapping("/{residentKey}")
    public ResponseEntity<ResidentResponse> update(@PathVariable String communityKey,
                                                    @PathVariable String residentKey,
                                                    @Valid @RequestBody CreateResidentRequest request) {
        log.info("Resident update requested: communityKey={}, residentKey={}", communityKey, residentKey);
        return ResponseEntity.ok(residentService.update(communityKey, residentKey, request));
    }

    @DeleteMapping("/{residentKey}")
    public ResponseEntity<Void> delete(@PathVariable String communityKey,
                                       @PathVariable String residentKey) {
        log.info("Resident delete requested: communityKey={}, residentKey={}", communityKey, residentKey);
        residentService.delete(communityKey, residentKey);
        return ResponseEntity.noContent().build();
    }
}
