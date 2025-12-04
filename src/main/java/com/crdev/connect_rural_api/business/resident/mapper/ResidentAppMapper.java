package com.crdev.connect_rural_api.business.resident.mapper;

import com.crdev.connect_rural_api.app.resident.dto.response.ResidentResponseDto;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ResidentAppMapper {

    public ResidentResponseDto toResponse(ResidentEntity entity){
        if (entity == null) return null;
        String address = Stream.of(entity.getAddress(), entity.getAddressReference())
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
        return new ResidentResponseDto(
                entity.getKey().toString(),
                entity.getCommunityKey().toString(),
                entity.getFirstName(),
                entity.getLastName(),
                address,
                entity.getJoinedAt(),
                entity.getActive()

        );
    }

    public List<ResidentResponseDto> toResponseList(List<ResidentEntity> entities){
        if (entities == null) return null;
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
