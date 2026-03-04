package com.crdev.connect_rural_api.app.webhook.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendWhatsappMessageDto {

    @NotBlank
    private String to;

    @NotBlank
    private String message;
}
