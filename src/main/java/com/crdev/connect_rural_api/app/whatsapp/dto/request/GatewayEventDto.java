package com.crdev.connect_rural_api.app.whatsapp.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GatewayEventDto {

    @JsonProperty("tenantKey")
    private String tenantKey;

    @JsonProperty("event")
    private String event;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("message")
    private GatewayMessageDto message;

    @JsonProperty("status")
    private GatewayStatusDto status;
}
