package com.crdev.connect_rural_api.app.webhook.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WhatsappEntryDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("changes")
    private List<WhatsappChangeDto> changes;
}
