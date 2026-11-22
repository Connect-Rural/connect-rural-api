package com.crdev.connect_rural_api.app.resident;

import com.crdev.connect_rural_api.app.resident.dto.CreateResidentRequest;
import com.crdev.connect_rural_api.app.resident.dto.ResidentResponse;
import com.crdev.connect_rural_api.app.resident.dto.SimpleResidentResponse;
import com.crdev.connect_rural_api.app.shared.dto.PageResponse;
import com.crdev.connect_rural_api.service.resident.ResidentService;
import com.crdev.connect_rural_api.service.resident.dto.ResidentQuery;
import com.crdev.connect_rural_api.service.shared.PageParams;
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
    private final ResidentApiMapper mapper;

    @GetMapping
    public ResponseEntity<List<ResidentResponse>> list(@PathVariable String communityKey) {
        return ResponseEntity.ok(
                mapper.toResponseList(residentService.listByCommunity(communityKey))
        );
    }

    @GetMapping("/paginated")
    public ResponseEntity<PageResponse<ResidentResponse>> getPaginated(
            @PathVariable String communityKey,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {


        ResidentQuery query = new ResidentQuery(keyword);
        PageParams params = PageParams.of(page, size);

        PageResponse<ResidentResponse> pagedResidents = PageResponse.from(
                residentService.getPaginated(communityKey,query,params),
                mapper::toResponse
        );
        return ResponseEntity.ok(pagedResidents);
    }

    @GetMapping("/catalog")
    public ResponseEntity<List<SimpleResidentResponse>> getCatalog(@PathVariable String communityKey) {
        return ResponseEntity.ok(mapper.toSimpleResidentResponseList(residentService.getCatalog(communityKey)));
    }

    @GetMapping("/{residentKey}")
    public ResponseEntity<ResidentResponse> getByKey(@PathVariable String communityKey,
                                                            @PathVariable String residentKey) {
        return ResponseEntity.ok(mapper.toResponse(residentService.getByKey(communityKey, residentKey)));
    }

    @PostMapping
    public ResponseEntity<ResidentResponse> create(@PathVariable String communityKey,
                                                    @Valid @RequestBody CreateResidentRequest request) {
        log.info("Resident create requested: communityKey={}", communityKey);


        return ResponseEntity.status(201).body(
                mapper.toResponse(
                        residentService.create(communityKey, mapper.toDraft(request) ))
                );
    }

    @PatchMapping("/{residentKey}")
    public ResponseEntity<ResidentResponse> update(@PathVariable String communityKey,
                                                    @PathVariable String residentKey,
                                                    @Valid @RequestBody CreateResidentRequest request) {
        log.info("Resident update requested: communityKey={}, residentKey={}", communityKey, residentKey);

        return ResponseEntity.ok(mapper.toResponse(residentService.update(communityKey, residentKey, mapper.toDraft(request))));
    }

    @DeleteMapping("/{residentKey}")
    public ResponseEntity<Void> delete(@PathVariable String communityKey,
                                       @PathVariable String residentKey) {
        log.info("Resident delete requested: communityKey={}, residentKey={}", communityKey, residentKey);
        residentService.delete(communityKey, residentKey);
        return ResponseEntity.noContent().build();
    }
}
