package com.crdev.connect_rural_api.app.resident;

import com.crdev.connect_rural_api.app.community.dto.request.CommunityFilterDto;
import com.crdev.connect_rural_api.app.community.dto.response.CommunityPaginatedResponseDto;
import com.crdev.connect_rural_api.app.resident.dto.request.ResidentFilterDto;
import com.crdev.connect_rural_api.app.resident.dto.response.ResidentPaginatedResponseDto;
import com.crdev.connect_rural_api.app.resident.dto.response.ResidentResponseDto;
import com.crdev.connect_rural_api.business.resident.usecases.GetResidentListByCommunityKeyUseCase;
import com.crdev.connect_rural_api.business.resident.usecases.GetResidentPaginatedUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/{communityKey}/residents")
@RequiredArgsConstructor
public class ResidentController {
    private final GetResidentListByCommunityKeyUseCase getResidentListByCommunityKeyUC;
    private final GetResidentPaginatedUseCase getResidentPaginatedUC;

    @GetMapping
    public ResponseEntity<List<ResidentResponseDto>> list(@PathVariable String communityKey) {

        return ResponseEntity.ok(getResidentListByCommunityKeyUC.execute(communityKey));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ResidentPaginatedResponseDto> getPaginated(
            @PathVariable String communityKey,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size){
        ResidentFilterDto filter =
                new ResidentFilterDto(keyword, page, size);
        return ResponseEntity.ok(
                getResidentPaginatedUC.execute(communityKey,filter)
        );
    }

}
