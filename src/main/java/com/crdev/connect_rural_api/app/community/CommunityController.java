package com.crdev.connect_rural_api.app.community;

import com.crdev.connect_rural_api.app.community.dto.CommunityPaginatedResponseDto;
import com.crdev.connect_rural_api.app.community.dto.CommunityResponseDto;
import com.crdev.connect_rural_api.app.community.dto.request.CommunityFilterRequestDto;
import com.crdev.connect_rural_api.business.community.usecases.GetCommunityListUseCase;
import com.crdev.connect_rural_api.business.community.usecases.GetCommunityPaginatedUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {
    private final GetCommunityListUseCase communityListUseCase;
    private final GetCommunityPaginatedUseCase communityPaginatedUseCase;

    @GetMapping
    public ResponseEntity<List<CommunityResponseDto>> list() {
        List<CommunityResponseDto> response = communityListUseCase.execute();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/paginated")
    public ResponseEntity<CommunityPaginatedResponseDto> getPaginated(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size){
        CommunityFilterRequestDto filter =
                new CommunityFilterRequestDto(keyword, page, size);
        return ResponseEntity.ok(
                communityPaginatedUseCase.execute(filter)
        );
    }
}
