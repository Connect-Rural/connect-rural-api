package com.crdev.connect_rural_api.service.resident.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Resident {
    UUID key;
    UUID communityKey;
    String firstName;
    String lastName;
    LocalDate birthDate;
    String phoneNumber;
    String email;
    String address;
    String addressReference;
    LocalDate joinedAt;
    Boolean active;




    /**
     * Crea un residente nuevo a partir de un borrador (campos editables).
     * Asigna communityKey (del path), defaults de negocio (active=true,
     * joinedAt=hoy si no viene). La key la genera JPA al persistir.
     */

    public static Resident newResident(UUID communityKey, Resident draft) {
        return Resident.builder()
                .communityKey(communityKey)  //setea el communityKey
                .firstName(draft.getFirstName())
                .lastName(draft.getLastName())
                .birthDate(draft.getBirthDate())
                .phoneNumber(draft.getPhoneNumber())
                .email(draft.getEmail())
                .address(draft.getAddress())
                .addressReference(draft.getAddressReference())
                .joinedAt(draft.getJoinedAt() != null ? draft.getJoinedAt() : LocalDate.now())
                .active(true)// se crea como active
                .build();

    }

    public Resident applyChanges(Resident draft) {
        return toBuilder()
                .firstName(draft.getFirstName())
                .lastName(draft.getLastName())
                .birthDate(draft.getBirthDate())
                .phoneNumber(draft.getPhoneNumber())
                .email(draft.getEmail())
                .address(draft.getAddress())
                .addressReference(draft.getAddressReference())
                .joinedAt(draft.getJoinedAt())
                .build();
    }

    public Resident deactivate() {
        return toBuilder()
                .active(false)
                .build();
    }

    public Resident activate() {
        return toBuilder()
                .active(true)
                .build();
    }


}