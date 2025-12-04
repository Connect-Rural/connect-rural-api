package com.crdev.connect_rural_api.app.community;

import com.crdev.connect_rural_api.app.community.dto.request.CreateCommunityDto;
import com.crdev.connect_rural_api.app.community.dto.response.CommunityAdminResponseDto;
import com.crdev.connect_rural_api.app.community.dto.response.CommunityPaginatedResponseDto;
import com.crdev.connect_rural_api.app.community.dto.response.CommunityResponseDto;
import com.crdev.connect_rural_api.app.community.dto.request.CommunityFilterDto;
import com.crdev.connect_rural_api.business.community.usecases.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {
    private final GetCommunityListUseCase communityListUseCase;
    private final GetCommunityPaginatedUseCase communityPaginatedUseCase;
    private final GetCommunityByKeyUseCase getCommunityByKeyUC;
    private final CreateCommunityUseCase createCommunityUseCase;
    private final UpdateCommunityUseCase updateCommunityUC;
    private final DeleteCommunityUseCase deleteCommunityUC;

    @PostMapping
    public ResponseEntity<CommunityResponseDto> create(@Valid @RequestBody CreateCommunityDto request) {
        return ResponseEntity.status(201).body(
                createCommunityUseCase.execute(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<CommunityAdminResponseDto>> list() {
        List<CommunityAdminResponseDto> response = communityListUseCase.execute();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{key}")
    public ResponseEntity<CommunityAdminResponseDto> getByKey(@PathVariable String key) {
        return ResponseEntity.ok(getCommunityByKeyUC.execute(key));
    }

    @GetMapping("/paginated")
    public ResponseEntity<CommunityPaginatedResponseDto> getPaginated(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size){
        CommunityFilterDto filter =
                new CommunityFilterDto(keyword, page, size);
        return ResponseEntity.ok(
                communityPaginatedUseCase.execute(filter)
        );
    }

    @PatchMapping("/{key}")
    public ResponseEntity<CommunityAdminResponseDto> updateCommunity(@PathVariable String key, @Valid @RequestBody CreateCommunityDto updateRequest) {
        return ResponseEntity.ok(updateCommunityUC.execute(key, updateRequest));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<?> deleteCommunity(@PathVariable String key) {
        deleteCommunityUC.execute(key);
        return ResponseEntity.noContent().build();
    }
}
