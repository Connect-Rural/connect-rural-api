package com.crdev.connect_rural_api.app.resident.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateResidentDto {
    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;
    private String lastName;
    @NotNull(message = "La fecha de nacimiento es importante")
    @Past
    private LocalDate birthDate;
    private String phoneNumber;
    private String email;
    private String address;
    private String addressReference;
    @NotNull(message = "La fecha que en que se une a la communidad es necesaria")
    @PastOrPresent
    private LocalDate joinedAt;
}
