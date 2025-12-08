package com.crdev.connect_rural_api.app.resident;

import com.crdev.connect_rural_api.app.community.dto.request.CreateCommunityDto;
import com.crdev.connect_rural_api.app.community.dto.response.CommunityAdminResponseDto;
import com.crdev.connect_rural_api.app.resident.dto.request.CreateResidentDto;
import com.crdev.connect_rural_api.app.resident.dto.request.ResidentFilterDto;
import com.crdev.connect_rural_api.app.resident.dto.response.ResidentDetailResponseDto;
import com.crdev.connect_rural_api.app.resident.dto.response.ResidentPaginatedResponseDto;
import com.crdev.connect_rural_api.app.resident.dto.response.ResidentResponseDto;
import com.crdev.connect_rural_api.business.resident.usecases.*;
import jakarta.validation.Valid;
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
    private final GetResidentByKeyUseCase getResidentByKeyUC;
    private final CreateResidentUseCase createResidentUC;
    private final UpdateResidentUseCase updateResidentUC;
    private final DeleteResidentUseCase deleteResidentUC;

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

    @GetMapping("/{residentKey}")
    public ResponseEntity<ResidentDetailResponseDto>getByKey(@PathVariable String communityKey, @PathVariable String residentKey) {
        return ResponseEntity.ok(getResidentByKeyUC.execute(communityKey, residentKey));
    }

    @PostMapping
    public ResponseEntity<ResidentResponseDto> create(  @PathVariable String communityKey,
                                                        @Valid @RequestBody CreateResidentDto request) {
        return ResponseEntity.status(201).body(
                createResidentUC.execute(communityKey,request)
        );
    }

    @PatchMapping("/{residentKey}")
    public ResponseEntity<ResidentResponseDto> update(@PathVariable String communityKey,
                                                      @PathVariable String residentKey,
                                                      @Valid @RequestBody CreateResidentDto updateRequest) {
        return ResponseEntity.ok(updateResidentUC.execute(communityKey,residentKey, updateRequest));
    }

    @DeleteMapping("/{residentKey}")
    public ResponseEntity<?> delete(@PathVariable String communityKey,
                                    @PathVariable String residentKey) {
        deleteResidentUC.execute(communityKey, residentKey);
        return ResponseEntity.noContent().build();
    }
}
    