package com.crdev.connect_rural_api.app.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RegisterWhatsappTenantRequest {

    @NotBlank(message = "El phoneNumberId es obligatorio")
    private String phoneNumberId;

    @NotBlank(message = "El accessToken es obligatorio")
    private String accessToken;

    private List<String> allowedMessageTypes;
}
