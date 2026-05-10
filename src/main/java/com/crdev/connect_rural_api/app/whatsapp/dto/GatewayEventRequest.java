package com.crdev.connect_rural_api.app.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GatewayEventRequest {

    @JsonProperty("tenantKey")
    private String tenantKey;

    @JsonProperty("event")
    private String event;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("message")
    private GatewayMessageRequest message;

    @JsonProperty("status")
    private GatewayStatusRequest status;
}
