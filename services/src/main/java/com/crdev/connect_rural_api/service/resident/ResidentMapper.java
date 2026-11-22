package com.crdev.connect_rural_api.service.resident;

import com.crdev.connect_rural_api.model.ResidentEntity;
import com.crdev.connect_rural_api.repository.resident.SimpleResident;
import com.crdev.connect_rural_api.service.resident.dto.Resident;
import com.crdev.connect_rural_api.service.resident.dto.SimpleResidentCommand;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResidentMapper {

    /**
     * Mapea desde la entidad JPA {@link ResidentEntity} al modelo de dominio
     * {@link Resident}.
     */
    public Resident toDomain(ResidentEntity entity) {
        if (entity == null) {
            return null;
        }

        return Resident.builder()
                .key(entity.getKey())
                .communityKey(entity.getCommunityKey())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .birthDate(entity.getBirthDate())
                .phoneNumber(entity.getPhoneNumber())
                .email(entity.getEmail())
                .address(entity.getAddress())
                .addressReference(entity.getAddressReference())
                .joinedAt(entity.getJoinedAt())
                .active(Boolean.TRUE.equals(entity.getActive()))
                .build();
    }

    /**
     * Mapea desde  el modelo de dominio {@link Resident}
     * a la entidad JPA {@link ResidentEntity} .
     */
    public ResidentEntity toEntity(Resident resident) {
        if (resident == null) {
            return null;
        }

        ResidentEntity entity = new ResidentEntity();

        entity.setKey(resident.getKey());
        entity.setCommunityKey(resident.getCommunityKey());
        entity.setFirstName(resident.getFirstName());
        entity.setLastName(resident.getLastName());
        entity.setBirthDate(resident.getBirthDate());
        entity.setPhoneNumber(resident.getPhoneNumber());
        entity.setEmail(resident.getEmail());
        entity.setAddress(resident.getAddress());
        entity.setAddressReference(resident.getAddressReference());
        entity.setJoinedAt(resident.getJoinedAt());
        entity.setActive(resident.getActive());

        return entity;
    }

    public void updateEntity(Resident resident, ResidentEntity entity) {
        if (resident == null || entity == null) {
            return;
        }

        entity.setFirstName(resident.getFirstName());
        entity.setLastName(resident.getLastName());
        entity.setBirthDate(resident.getBirthDate());
        entity.setPhoneNumber(resident.getPhoneNumber());
        entity.setEmail(resident.getEmail());
        entity.setAddress(resident.getAddress());
        entity.setAddressReference(resident.getAddressReference());
        entity.setJoinedAt(resident.getJoinedAt());
        entity.setActive(resident.getActive());

        // No modificar key, communityKey, createdAt ni updatedAt.
    }

    public List<Resident> toDomainList(List<ResidentEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toDomain)
                .toList();
    }

    public List<ResidentEntity> toEntityList(List<Resident> residents) {
        if (residents == null) {
            return List.of();
        }

        return residents.stream()
                .map(this::toEntity)
                .toList();
    }

    public SimpleResidentCommand toSimpleResidentCommand(SimpleResident simpleResident){
        if (simpleResident == null) return null;
        String fullName = simpleResident.getFirstName() + " " + simpleResident.getLastName();
        return  SimpleResidentCommand.builder()
                .key(simpleResident.getKey())
                .communityKey(simpleResident.getCommunityKey())
                .fullName(fullName)
                .build();

    }
}