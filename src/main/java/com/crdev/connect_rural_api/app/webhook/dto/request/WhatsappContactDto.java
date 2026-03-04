package com.crdev.connect_rural_api.app.webhook.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WhatsappContactDto {

    @JsonProperty("profile")
    private WhatsappProfileDto profile;

    @JsonProperty("wa_id")
    private String waId;

    @Data
    public static class WhatsappProfileDto {
        @JsonProperty("name")
        private String name;
    }
}
