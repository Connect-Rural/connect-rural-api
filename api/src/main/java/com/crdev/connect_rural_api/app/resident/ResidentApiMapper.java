package com.crdev.connect_rural_api.app.resident;


import com.crdev.connect_rural_api.app.resident.dto.CreateResidentRequest;
import com.crdev.connect_rural_api.app.resident.dto.ResidentResponse;
import com.crdev.connect_rural_api.app.resident.dto.SimpleResidentResponse;
import com.crdev.connect_rural_api.service.resident.dto.Resident;
import com.crdev.connect_rural_api.service.resident.dto.SimpleResidentCommand;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapea entre el modelo de dominio {@link Resident} y los DTOs HTTP.
 *
 * Vive en app/ (adaptador) para que business/ no dependa de los DTO de
 * entrada/salida. El mapeo Entity ↔ Community es responsabilidad de data/.
 */
@Component
public class ResidentApiMapper {

    public ResidentResponse toResponse(Resident resident) {
        if (resident == null) return null;

        return ResidentResponse.builder()
                .key(resident.getKey() == null ? null : resident.getKey().toString())
                .communityKey(resident.getCommunityKey().toString())
                .firstName(resident.getFirstName())
                .lastName(resident.getLastName())
                .address(resident.getAddress())
                .email(resident.getEmail())
                .phoneNumber(resident.getPhoneNumber())
                .birthDate(resident.getBirthDate())
                .joinedAt(resident.getJoinedAt())
                .active(resident.getActive())
                .build();
    }

    public List<ResidentResponse> toResponseList(List<Resident> residents) {
        if (residents == null) return null;
        return residents.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * DTO de entrada → modelo de dominio (borrador para create/update).
     *
     * adminEmail/adminPhone vienen validados en el request pero no se
     * persisten: aún no existe entidad de administrador.
     */
    public Resident toDraft(CreateResidentRequest request) {
        if (request == null) return null;

        return Resident.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .birthDate(request.getBirthDate())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .address(request.getAddress())
                .addressReference(request.getAddressReference())
                .joinedAt(request.getJoinedAt())
                .build();
    }

    /**
     *  modelo de dominio → DTO de entrada
     * Habitante con campos minimos
     */
  public SimpleResidentResponse toSimpleResidentResponse(SimpleResidentCommand simpleResidentCommand){
      if (simpleResidentCommand == null) return null;
      SimpleResidentResponse simpleResident = new SimpleResidentResponse();
      simpleResident.setCommunityKey(simpleResidentCommand.getCommunityKey().toString());
      simpleResident.setKey(simpleResidentCommand.getKey().toString());
      simpleResident.setName(simpleResidentCommand.getFullName());

      return simpleResident;

  }

    public List<SimpleResidentResponse> toSimpleResidentResponseList(List<SimpleResidentCommand> simpleResidents) {
        if (simpleResidents == null) return null;
        return simpleResidents.stream()
                .map(this::toSimpleResidentResponse)
                .collect(Collectors.toList());
    }


    /**
     * LocalDateTime (dominio) → Date (contrato HTTP actual, se serializa como
     * epoch millis). No cambiar sin coordinar con el frontend.
     */
    private Date toDate(LocalDateTime dateTime) {
        return dateTime == null ? null : Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
