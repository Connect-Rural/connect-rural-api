package com.crdev.connect_rural_api.app.community;

import com.crdev.connect_rural_api.app.community.dto.request.CreateCommunityDto;
import com.crdev.connect_rural_api.app.community.dto.response.CommunityAdminResponseDto;
import com.crdev.connect_rural_api.app.community.dto.response.CommunityPaginatedResponseDto;
import com.crdev.connect_rural_api.app.community.dto.response.CommunityResponseDto;
import com.crdev.connect_rural_api.app.community.dto.request.CommunityFilterDto;
import com.crdev.connect_rural_api.business.community.usecases.CreateCommunityUseCase;
import com.crdev.connect_rural_api.business.community.usecases.GetCommunityListUseCase;
import com.crdev.connect_rural_api.business.community.usecases.GetCommunityPaginatedUseCase;
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
    private final CreateCommunityUseCase createCommunityUseCase;

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
}
