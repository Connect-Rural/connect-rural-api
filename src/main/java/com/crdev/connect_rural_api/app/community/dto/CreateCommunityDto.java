package com.crdev.connect_rural_api.app.community.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommunityDto {
    @NotBlank(message = "El nombre de la comunidad es obligatorio")
    String name;
    String description;
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe ser un correo válido")
    String adminEmail;
    @NotBlank(message = "El número de teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos")
    String adminPhone;
    String logoUrl;
    String subscriptionPlan = "FREE";
}

