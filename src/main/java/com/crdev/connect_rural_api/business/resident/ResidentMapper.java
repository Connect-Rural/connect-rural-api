package com.crdev.connect_rural_api.business.resident;

import com.crdev.connect_rural_api.app.resident.dto.CreateResidentRequest;
import com.crdev.connect_rural_api.app.resident.dto.ResidentDetailResponse;
import com.crdev.connect_rural_api.app.resident.dto.ResidentResponse;
import com.crdev.connect_rural_api.app.resident.dto.SimpleResidentResponse;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import com.crdev.connect_rural_api.data.resident.SimpleResident;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ResidentMapper {

    public ResidentResponse toResponse(ResidentEntity entity) {
        if (entity == null) return null;
        String address = Stream.of(entity.getAddress(), entity.getAddressReference())
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
        return new ResidentResponse(
                entity.getKey().toString(),
                entity.getCommunityKey().toString(),
                entity.getFirstName(),
                entity.getLastName(),
                address,
                entity.getEmail(),
                entity.getPhoneNumber(),
                entity.getBirthDate(),
                entity.getJoinedAt(),
                entity.getActive()
        );
    }

    public ResidentDetailResponse toResponseDetail(ResidentEntity entity) {
        if (entity == null) return null;
        return new ResidentDetailResponse(
                entity.getKey().toString(),
                entity.getCommunityKey().toString(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getBirthDate(),
                entity.getPhoneNumber(),
                entity.getEmail(),
                entity.getAddress(),
                entity.getAddressReference(),
                entity.getJoinedAt(),
                entity.getActive()
        );
    }

    public List<ResidentResponse> toResponseList(List<ResidentEntity> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ResidentEntity updateEntityFromRequest(CreateResidentRequest request, ResidentEntity entity) {
        if (request == null || entity == null) return entity;
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setBirthDate(request.getBirthDate());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setEmail(request.getEmail());
        entity.setAddress(request.getAddress());
        entity.setAddressReference(request.getAddressReference());
        entity.setJoinedAt(request.getJoinedAt());
        return entity;
    }

    public SimpleResidentResponse toSimpleResident(SimpleResident data) {
        if (data == null) return null;
        String fullName = data.getFirstName() + " " + data.getLastName();
        return new SimpleResidentResponse(
                data.getKey().toString(),
                data.getCommunityKey().toString(),
                fullName
        );
    }
}
