package com.crdev.connect_rural_api.app.webhook.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WhatsappWebhookPayloadDto {

    @JsonProperty("object")
    private String object;

    @JsonProperty("entry")
    private List<WhatsappEntryDto> entry;
}
