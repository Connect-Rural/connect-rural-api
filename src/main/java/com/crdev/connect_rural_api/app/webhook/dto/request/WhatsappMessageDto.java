package com.crdev.connect_rural_api.app.webhook.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WhatsappMessageDto {

    @JsonProperty("from")
    private String from;

    @JsonProperty("id")
    private String id;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("type")
    private String type;

    @JsonProperty("text")
    private WhatsappTextDto text;

    @Data
    public static class WhatsappTextDto {
        @JsonProperty("body")
        private String body;
    }
}
