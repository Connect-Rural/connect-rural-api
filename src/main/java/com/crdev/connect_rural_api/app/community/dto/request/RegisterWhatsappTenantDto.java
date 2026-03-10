package com.crdev.connect_rural_api.app.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RegisterWhatsappTenantDto {

    @NotBlank(message = "El phoneNumberId es obligatorio")
    private String phoneNumberId;

    @NotBlank(message = "El accessToken es obligatorio")
    private String accessToken;

    // Si no se especifica, se permite solo TEXT por defecto
    private List<String> allowedMessageTypes;
}
