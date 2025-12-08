package com.crdev.connect_rural_api.business.resident.mapper;

import com.crdev.connect_rural_api.app.resident.dto.request.CreateResidentDto;
import com.crdev.connect_rural_api.app.resident.dto.response.ResidentDetailResponseDto;
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
                entity.getEmail(),
                entity.getPhoneNumber(),
                entity.getBirthDate(),
                entity.getJoinedAt(),
                entity.getActive()

        );
    }

    public ResidentDetailResponseDto toResponseDetail(ResidentEntity entity){
        if (entity == null) return null;

        return new ResidentDetailResponseDto(
                entity.getKey().toString(),
                entity.getCommunityKey().toString(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getBirthDate(),
                entity.getPhoneNumber(),
                entity.getEmail(),
                entity.getAddress(),
                entity.getAddressReference(),
                entity.getJoinedAt()
        );
    }



    public List<ResidentResponseDto> toResponseList(List<ResidentEntity> entities){
        if (entities == null) return null;
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ResidentEntity updateEntityFromDto(
            CreateResidentDto dto, ResidentEntity entity){
        if (dto == null || entity == null) return entity;

        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setBirthDate(dto.getBirthDate());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setEmail(dto.getEmail());
        entity.setAddress(dto.getAddress());
        entity.setAddressReference( dto.getAddressReference());
        entity.setJoinedAt(dto.getJoinedAt());

        return entity;

    }
}
