package com.crdev.connect_rural_api.app.webhook.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WhatsappStatusDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("recipient_id")
    private String recipientId;
}
