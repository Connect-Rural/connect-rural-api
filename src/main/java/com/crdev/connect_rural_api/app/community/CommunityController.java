package com.crdev.connect_rural_api.app.community;

import com.crdev.connect_rural_api.app.community.dto.CommunityAdminResponse;
import com.crdev.connect_rural_api.app.community.dto.CommunityFilterRequest;
import com.crdev.connect_rural_api.app.community.dto.CommunityPageResponse;
import com.crdev.connect_rural_api.app.community.dto.CommunityResponse;
import com.crdev.connect_rural_api.app.community.dto.CreateCommunityRequest;
import com.crdev.connect_rural_api.app.community.dto.RegisterWhatsappTenantRequest;
import com.crdev.connect_rural_api.business.community.CommunityService;
import com.crdev.connect_rural_api.business.whatsapp.WhatsappService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
@Slf4j
public class CommunityController {

    private final CommunityService communityService;
    private final WhatsappService whatsappService;

    @PostMapping
    public ResponseEntity<CommunityResponse> create(@Valid @RequestBody CreateCommunityRequest request) {
        log.info("Community create requested: name={}", request.getName());
        return ResponseEntity.status(201).body(communityService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<CommunityAdminResponse>> list() {
        return ResponseEntity.ok(communityService.getList());
    }

    @GetMapping("/{key}")
    public ResponseEntity<CommunityAdminResponse> getByKey(@PathVariable String key) {
        return ResponseEntity.ok(communityService.getByKey(key));
    }

    @GetMapping("/paginated")
    public ResponseEntity<CommunityPageResponse> getPaginated(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(communityService.getPaginated(new CommunityFilterRequest(keyword, page, size)));
    }

    @PatchMapping("/{key}")
    public ResponseEntity<CommunityAdminResponse> update(@PathVariable String key,
                                                          @Valid @RequestBody CreateCommunityRequest request) {
        log.info("Community update requested: key={}", key);
        return ResponseEntity.ok(communityService.update(key, request));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        log.info("Community delete requested: key={}", key);
        communityService.delete(key);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{key}/whatsapp")
    public ResponseEntity<Map<String, UUID>> registerWhatsapp(
            @PathVariable String key,
            @Valid @RequestBody RegisterWhatsappTenantRequest request) {
        log.info("Whatsapp tenant register requested: communityKey={}", key);
        UUID appKey = whatsappService.registerTenant(key, request);
        return ResponseEntity.status(201).body(Map.of("appKey", appKey));
    }

    @DeleteMapping("/{key}/whatsapp")
    public ResponseEntity<Void> unlinkWhatsapp(@PathVariable String key) {
        log.info("Whatsapp tenant unlink requested: communityKey={}", key);
        whatsappService.unlinkTenant(key);
        return ResponseEntity.noContent().build();
    }
}
