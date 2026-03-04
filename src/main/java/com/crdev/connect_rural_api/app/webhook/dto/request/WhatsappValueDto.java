package com.crdev.connect_rural_api.app.webhook.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WhatsappValueDto {

    @JsonProperty("messaging_product")
    private String messagingProduct;

    @JsonProperty("metadata")
    private WhatsappMetadataDto metadata;

    @JsonProperty("contacts")
    private List<WhatsappContactDto> contacts;

    @JsonProperty("messages")
    private List<WhatsappMessageDto> messages;

    @JsonProperty("statuses")
    private List<WhatsappStatusDto> statuses;
}
