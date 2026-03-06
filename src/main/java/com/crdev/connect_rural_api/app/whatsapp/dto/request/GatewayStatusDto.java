package com.crdev.connect_rural_api.app.whatsapp.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GatewayStatusDto {

    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("recipient")
    private String recipient;

    @JsonProperty("updatedAt")
    private String updatedAt;
}
