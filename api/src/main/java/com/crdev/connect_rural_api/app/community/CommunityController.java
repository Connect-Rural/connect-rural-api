package com.crdev.connect_rural_api.app.community;

import com.crdev.connect_rural_api.app.community.dto.CommunityAdminResponse;
import com.crdev.connect_rural_api.app.community.dto.CommunityFilterRequest;
import com.crdev.connect_rural_api.app.community.dto.CommunityPageResponse;
import com.crdev.connect_rural_api.app.community.dto.CommunityResponse;
import com.crdev.connect_rural_api.app.community.dto.CreateCommunityRequest;
import com.crdev.connect_rural_api.service.community.CommunityService;
import com.crdev.connect_rural_api.service.community.dto.Community;
import com.crdev.connect_rural_api.service.shared.PagedResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Adaptador HTTP: traduce DTO ↔ modelo de dominio y delega en los servicios.
 */
@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
@Slf4j
public class CommunityController {

    private final CommunityService communityService;
    private final CommunityApiMapper mapper;

    @PostMapping
    public ResponseEntity<CommunityResponse> create(@Valid @RequestBody CreateCommunityRequest request) {
        log.info("Community create requested: name={}", request.getName());
        Community created = communityService.create(mapper.toDomain(request));
        return ResponseEntity.status(201).body(mapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<CommunityAdminResponse>> list() {
        return ResponseEntity.ok(mapper.toAdminResponseList(communityService.getList()));
    }

    @GetMapping("/{key}")
    public ResponseEntity<CommunityAdminResponse> getByKey(@PathVariable String key) {
        return ResponseEntity.ok(mapper.toAdminResponse(communityService.getByKey(key)));
    }

    @GetMapping("/paginated")
    public ResponseEntity<CommunityPageResponse> getPaginated(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        CommunityFilterRequest filter = new CommunityFilterRequest(keyword, page, size);
        PagedResult<Community> result = communityService.getPaginated(
                mapper.toQuery(filter), mapper.toPageParams(filter));
        return ResponseEntity.ok(new CommunityPageResponse(
                mapper.toAdminResponseList(result.items()),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()));
    }

    @PatchMapping("/{key}")
    public ResponseEntity<CommunityAdminResponse> update(@PathVariable String key,
                                                          @Valid @RequestBody CreateCommunityRequest request) {
        log.info("Community update requested: key={}", key);
        Community updated = communityService.update(key, mapper.toDomain(request));
        return ResponseEntity.ok(mapper.toAdminResponse(updated));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        log.info("Community delete requested: key={}", key);
        communityService.delete(key);
        return ResponseEntity.noContent().build();
    }

}
